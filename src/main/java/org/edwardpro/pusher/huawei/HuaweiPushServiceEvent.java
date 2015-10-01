package org.edwardpro.pusher.huawei;

import org.edwardpro.pusher.huawei.domains.PushRet;

/**
 * Created by edwardpro on 9/15/15.
 * <p/>
 * 异步模式下使用的回调类，实现方法来拿到所有数据返回值
 */
public interface HuaweiPushServiceEvent {

    public void onFinish(PushRet ret);
}
