package org.edwardpro.pusher.huawei.domains;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by edwardpro on 9/15/15.
 * <p/>
 * hashMap.put("deviceToken", token);
 * hashMap.put("message", message);
 * hashMap.put("priority", priority);
 * hashMap.put("cacheMode", cacheMode);
 * hashMap.put("msgType", msgType);
 * hashMap.put("requestID", requestID);
 * hashMap.put("expire_time", expire_time);
 */
public class HuaweiPushRequest implements Serializable {


    private static final long serialVersionUID = 706962628205648974L;

    public static final String TIMESTAMP_NORMAL = "yyyy-MM-dd HH:mm:ss";

    private Map<String, Object> pushData = new HashMap<String, Object>(7);

    /**
     * 华为设备ID
     *
     * @param deviceToken
     */
    public HuaweiPushRequest setDeviceToken(String deviceToken) {
        pushData.put(HWPushRequestKey.deviceToken.getKey(), deviceToken);
        return this;
    }

    /**
     * 群发设备IDlist
     * 最多填1000个
     *
     * @param deviceTokenList
     * @return
     */
    public HuaweiPushRequest setBatchList(String[] deviceTokenList) throws Exception {
        if (deviceTokenList == null || deviceTokenList.length > 1000) {
            throw new Exception("INVALID TOKENLIST");
        }
        pushData.put(HWPushRequestKey.deviceTokenList.getKey(), deviceTokenList);
        return this;
    }

    /**
     * 消息体
     *
     * @param message
     */
    public HuaweiPushRequest setMessage(String message) {
        pushData.put(HWPushRequestKey.message.getKey(), message);
        return this;
    }

    /**
     * 优先级
     * //必选
     * //0：高优先级
     * //1：普通优先级
     * //缺省值为1
     *
     * @param priority
     */
    public static final int KEY_PRIORITY_NORMAL = 1;
    public static final int KEY_PRIORITY_HIGHT = 0;

    public HuaweiPushRequest setPriority(int priority) {
        pushData.put(HWPushRequestKey.priority.getKey(), priority);
        return this;
    }

    /**
     * 缓存模式
     * //0：不缓存
     * //1：缓存
     * //  缺省值为0
     *
     * @param cacheMode
     */
    public static final int KEY_CACHEMODE_ON = 1;
    public static final int KEY_CACHEMODE_OFF = 0;

    public HuaweiPushRequest setCacheMode(int cacheMode) {
        pushData.put(HWPushRequestKey.cacheMode.getKey(), cacheMode);
        return this;
    }

    /**
     * 消息类型
     * 消息的顺序，当requestID相同时，只保留msgType最大的那条
     */
    public HuaweiPushRequest setMsgType(int msgType) {
        pushData.put(HWPushRequestKey.msgType.getKey(), msgType);
        return this;
    }

    /**
     * 请求id，对于agoo可以使用msgId，这个有去重作用
     *
     * @param requestID
     */
    public HuaweiPushRequest setRequestID(String requestID) {
        pushData.put(HWPushRequestKey.requestID.getKey(), requestID);
        return this;
    }

    /**
     * 过期时间
     *
     * @param expireTime
     */
    public HuaweiPushRequest setExpireTime(Date expireTime) {
        if (expireTime != null && expireTime.after(new Date())) {
            SimpleDateFormat dataFormat = new SimpleDateFormat(TIMESTAMP_NORMAL);
            pushData.put(HWPushRequestKey.expireTime.getKey(), dataFormat.format(expireTime));
        }
        return this;
    }


    public Map<String, Object> getRequestMap() throws Exception {
        if (!pushData.containsKey(HWPushRequestKey.cacheMode.getKey())) {
            pushData.put(HWPushRequestKey.cacheMode.getKey(), 1);
        }
        if (!pushData.containsKey(HWPushRequestKey.priority.getKey())) {
            pushData.put(HWPushRequestKey.priority.getKey(), 1);
        }
        if (!pushData.containsKey(HWPushRequestKey.priority.getKey())) {
            pushData.put(HWPushRequestKey.priority.getKey(), 1);
        }
        if (!pushData.containsKey(HWPushRequestKey.cacheMode.getKey())) {
            pushData.put(HWPushRequestKey.cacheMode.getKey(), 1);
        }
        if (!pushData.containsKey(HWPushRequestKey.expireTime.getKey())) {
            //默认为当日的23:59:59
            SimpleDateFormat dataFormat = new SimpleDateFormat(TIMESTAMP_NORMAL);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            pushData.put(HWPushRequestKey.expireTime.getKey(), dataFormat.format(cal.getTime()));
        }
        if (!pushData.containsKey(HWPushRequestKey.requestID.getKey())) {
            pushData.put(HWPushRequestKey.requestID.getKey(), UUID.randomUUID());
        }
        return pushData;
    }

    public static enum HWPushRequestKey {
        deviceTokenList("deviceTokenList"), deviceToken("deviceToken"), message("message"), priority("priority"), cacheMode("cacheMode"), msgType("msgType"), requestID("requestID"), expireTime("expire_time");

        private String rKey;

        HWPushRequestKey(String rKey) {
            this.rKey = rKey;
        }

        public String getKey() {
            return rKey;
        }
    }
}
