package com.xxjr.xxyun.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.lzy.okgo.utils.HttpUtils.runOnUiThread
import com.xxjr.xxyun.BuildConfig
import com.xxjr.xxyun.R


/**
 * Created by sty on 2017/8/1.
 */
object CommUtil {

    fun Log(key: String, value: String) {
        if (BuildConfig.DEBUG) {
            Log.e(key, value)
        }
    }

    fun ToastShow(context: Context, msg: String) {
        // 判断是在子线程，还是主线程
        if ("main" == Thread.currentThread().name) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        } else {
            // 子线程
            runOnUiThread(Runnable { Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() })
        }
    }



    // 判断是否打开了通知监听权限
    fun isEnabled(mActivity: Activity): Boolean {
        var pkgName = mActivity.packageName;
        var flat = Settings.Secure.getString(mActivity.contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names: List<String> = flat.split(":")
            names.forEach {
                val cn = ComponentName.unflattenFromString(it)
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true
                    }
                }
            }
        }
        return false
    }

}