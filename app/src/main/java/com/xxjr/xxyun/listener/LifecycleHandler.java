package com.xxjr.xxyun.listener;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.xxjr.xxyun.util.CommMath;
import com.xxjr.xxyun.utils.CommUtil;

/**
 * Created by wqg on 2017/12/13.
 */

public class LifecycleHandler  implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        CommUtil.INSTANCE.Log("后台","onActivityCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        CommUtil.INSTANCE.Log("后台","onActivityStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        CommUtil.INSTANCE.Log("后台","onActivityResumed");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        CommUtil.INSTANCE.Log("后台","onActivityPaused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        CommUtil.INSTANCE.Log("后台","onActivityStopped");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        CommUtil.INSTANCE.Log("后台","onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        CommUtil.INSTANCE.Log("后台","onActivityDestroyed");
    }
}
