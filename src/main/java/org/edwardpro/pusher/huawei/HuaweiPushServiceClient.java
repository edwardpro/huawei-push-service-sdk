package org.edwardpro.pusher.huawei;

import nsp.NSPClient;
import nsp.OAuth2Client;
import nsp.support.common.AccessToken;
import org.edwardpro.pusher.huawei.domains.HuaweiPushRequest;
import org.edwardpro.pusher.huawei.domains.PushRet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by edwardpro on 9/15/15.
 */
public class HuaweiPushServiceClient {

    private static final Logger logger = LoggerFactory.getLogger("huawei-pusher");

    protected OAuth2Client oauth2Client;

    protected NSPClient client;

    private static final String OAUTH2_PASS = "123456";
    private static final String OAUTH2_PATH = "/mykeystorebj.jks";

    private static final int MAX_CO = 10;

    private int threadNum;
    private String appId;
    private String appSecere;

    private HuaweiPushServiceEvent event;
    //初始化后把它打开
    protected boolean isAsync = false;
    protected int msgQueueLength = 10000;

    private Queue<HuaweiPushRequest> msgQueue;

    private Thread workThread;

    public HuaweiPushServiceClient(int threadNum, String appId, String appSecere) {
        this.threadNum = threadNum;
        this.appId = appId;
        this.appSecere = appSecere;
        if (threadNum <= 0) {
            threadNum = MAX_CO;
        }
    }

    public HuaweiPushServiceClient init() throws Exception {
        oauth2Client = new OAuth2Client();
        try {
            oauth2Client.initKeyStoreStream(HuaweiPushServiceClient.class.getResource("/mykeystorebj.jks").openStream(), "123456");
        } catch (Exception e) {
            logger.error("error in init PushClient", e);
        }
        AccessToken access_token = oauth2Client.getAccessToken("client_credentials", appId, appSecere);
        logger.warn("access token : {} ,expires time[access token 过期时间]: {} ", access_token.getAccess_token(), access_token.getExpires_in());
        client = new NSPClient(access_token.getAccess_token());
        client.initHttpConnections(threadNum, threadNum);//设置每个路由的连接数和最大连接数
        client.initKeyStoreStream(HuaweiPushServiceClient.class.getResource("/mykeystorebj.jks").openStream(), "123456");//如果访问https必须导入证书流和密码
        return this;
    }

    public HuaweiPushServiceClient initAsyncMode(HuaweiPushServiceEvent event, int msgQueueLength) throws Exception {
        this.event = event;
        this.msgQueueLength = msgQueueLength;
        isAsync = true;
        msgQueue = new LinkedBlockingQueue<HuaweiPushRequest>(this.msgQueueLength);
        this.init();
        //启动异步发送线程
        workThread = new Thread(new WorkThread());
        workThread.setDaemon(true);
        workThread.start();
        return this;
    }

    public PushRet sendSigle(HuaweiPushRequest request) throws Exception {
        client.setTimeout(1500, 1500);
        assert checkDate(false, request);
        PushRet resp = client.call("openpush.message.single_send", request.getRequestMap(), PushRet.class);
        if (logger.isDebugEnabled()) {
            logger.warn("send:{},ret:{}", request.getRequestMap(), resp);
        }
        System.err.println("单发接口消息响应:" + resp.getResultcode() + ",message:" + resp.getMessage());
        return resp;
    }

    public PushRet sendBathch(HuaweiPushRequest request) throws Exception {
        client.setTimeout(3000, 5000);
        assert checkDate(true, request);
        PushRet resp = client.call("openpush.message.batch_send", request.getRequestMap(), PushRet.class);
        if (logger.isDebugEnabled()) {
            logger.warn("send:{},ret:{}", request.getRequestMap(), resp);
        }
        return resp;
    }

    private boolean checkDate(boolean isBatch, HuaweiPushRequest request) throws Exception {

        if (!isBatch) {
            if (!request.getRequestMap().containsKey(HuaweiPushRequest.HWPushRequestKey.deviceToken.getKey())) {
                throw new Exception("EMPTY TARGET");
            }
        } else {
            if (!request.getRequestMap().containsKey(HuaweiPushRequest.HWPushRequestKey.deviceTokenList.getKey())) {
                throw new Exception("EMPTY TARGET");
            }
        }

        if (!request.getRequestMap().containsKey(HuaweiPushRequest.HWPushRequestKey.message.getKey())) {
            throw new Exception("EMPTY MESSAGE BODY");
        }
        return true;
    }

    /**
     * 异步模式下使用这个方法进行发送
     *
     * @param request
     * @throws Exception
     */
    public void addPushMessage(HuaweiPushRequest request) throws Exception {
        if (!isAsync) {
            throw new Exception("PLEASE USE initAsync, BEFORE YOU USE addPushMessage");
        }
        msgQueue.add(request);
    }

    /**
     * 异步模式下的停止方法，否则引起发送数据的丢失
     */
    public List<HuaweiPushRequest> stopAsync() {
        //把队列里的数据全部取出来返回给业务方自己去做处理
        List<HuaweiPushRequest> ret = new ArrayList<HuaweiPushRequest>(this.msgQueueLength);
        HuaweiPushRequest q = null;
        while ((q = msgQueue.poll()) != null) {
            ret.add(q);
        }
        if (workThread.isAlive() && !workThread.isInterrupted()) {
            workThread.interrupt();
        }
        return ret;
    }

    class WorkThread implements Runnable {

        @Override
        public void run() {
            logger.warn("ASYNC THREAD IS RUNNING NOW");
            while (true) {
                HuaweiPushRequest request = msgQueue.poll();
                if (request == null) {
                    continue;
                }
                try {
                    Map<String, Object> data = request.getRequestMap();
                    PushRet ret = null;
                    try {
                        if (data.containsKey(HuaweiPushRequest.HWPushRequestKey.deviceToken.getKey())) {
                            ret = sendSigle(request);
                        } else {
                            ret = sendBathch(request);
                        }
                    } finally {
                        if (event != null) {
                            event.onFinish(ret);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("DROP REQUEST BECAUSEOF SOME DATA ERROR.{}", e.getMessage());
                    continue;
                }
            }
        }
    }
}
