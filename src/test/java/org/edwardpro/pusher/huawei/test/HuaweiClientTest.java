package org.edwardpro.pusher.huawei.test;

import org.apache.log4j.BasicConfigurator;
import org.edwardpro.pusher.huawei.HuaweiPushServiceClient;
import org.edwardpro.pusher.huawei.HuaweiPushServiceEvent;
import org.edwardpro.pusher.huawei.domains.HuaweiPushRequest;
import org.edwardpro.pusher.huawei.domains.PushRet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by edwardpro on 9/15/15.
 */
public class HuaweiClientTest {


    String appId = "appId";
    String appKey = "appKey";


    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();

    }

    @Test
    public void testSingle() throws Exception {
        HuaweiPushRequest huaweiPushRequest = new HuaweiPushRequest();
        huaweiPushRequest.setDeviceToken("xxxx");
        huaweiPushRequest.setMessage("test");
        HuaweiPushServiceClient client = (new HuaweiPushServiceClient(5, appId, appKey)).init();
        PushRet pushRet = client.sendSigle(huaweiPushRequest);
        Assert.assertTrue(pushRet.getResultcode() >= 0);
    }


    @Test
    public void testAsyncSingle() throws Exception {
        HuaweiPushRequest huaweiPushRequest = new HuaweiPushRequest();
        huaweiPushRequest.setDeviceToken("xxxxx");
        huaweiPushRequest.setMessage("test");
        HuaweiPushServiceClient client = (new HuaweiPushServiceClient(5, appId, appKey)).initAsyncMode(new HuaweiPushServiceEvent() {
            @Override
            public void onFinish(PushRet ret) {
                Assert.assertTrue(ret.getResultcode() >= 0);
            }
        }, 100000);
        client.addPushMessage(huaweiPushRequest);
        //wait for send it over.
        Thread.sleep(100000);
    }
}
