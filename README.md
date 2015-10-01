# PUSH SDK FOR HUAWEI PUSH

## USAGE

```

  new HuaweiPushServiceClient(5, appId, appKey)

  0: CLIENT THREAD NUM YOU WANT
  1: APPID FROM HUAWEI DEVELOPMENT WEBSITE
  2: APPKEY FROM HUAWEI DEVELOPMENT WEBSITE

```


```

        HuaweiPushRequest huaweiPushRequest = new HuaweiPushRequest();
        huaweiPushRequest.setDeviceToken("xxxx");
        huaweiPushRequest.setMessage("test");
        HuaweiPushServiceClient client = (new HuaweiPushServiceClient(5, appId, appKey)).init();
        PushRet pushRet = client.sendSigle(huaweiPushRequest);

```


```
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

```