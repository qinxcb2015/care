package com.xxjr.xxyun.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import com.lzy.okgo.model.Response
import com.umeng.socialize.utils.DeviceConfig.getMac
import com.xxjr.xxyun.activity.LoginActivity
import com.xxjr.xxyun.app.MyApp
import com.xxjr.xxyun.connstant.Conn
import com.xxjr.xxyun.connstant.Urls
import com.xxjr.xxyun.utils.network.MyOkGo
import com.xxjr.xxyun.utils.network.callback.DialogCallback
import org.ddq.common.util.JsonUtil
import java.util.ArrayList

/**
 * Created by wqg on 2017/11/21.
 */
object CommMath {

    /**
     * 存储用户的 UID  USER_INFO
     */
    fun putUidAndInfo(activity: Activity, uid: String, userInfoMap: Map<String, Any>) {
        val userInfo = JsonUtil.getInstance().object2JSON(userInfoMap)
        MyApp.uid = uid
        MyApp.userInfo = userInfoMap
        MyApp.mTel = MyApp.userInfo?.get("telephone")?.toString()
        val sharedPrefUtil = SharedPrefUtil(activity, Conn.SP_USER_NAME)
        sharedPrefUtil.putString(Conn.SP_USER_UID, uid)
        sharedPrefUtil.putString(Conn.SP_USER_INFO, userInfo)
        sharedPrefUtil.commit()
    }

    /**
     * 获取用户信息
     * @param activity
     * @param listener  用户信息的监听
     * @param showDialog 0-加载对话框 1-是否需要跳转登录  2-错误对话框 默认都是false
     */
    fun getUserInfo(activity: Activity, listener: UserInfoListener?, vararg showDialog: Boolean?) {
        MyOkGo.postCust(Urls.CUST_INFO)
                ?.execute(object : DialogCallback<Map<String, Any>>(activity, if (showDialog != null && showDialog.size >= 1) showDialog[0] as Boolean else false) {
                    override fun onSuccessMap(rMap: Map<String, Any>) {
                        val rowsList: List<Map<String, Any>> = rMap["rows"] as List<Map<String, Any>>
                        MyApp.userInfo = rowsList[0]
                        MyApp.mTel = MyApp.userInfo?.get("telephone")?.toString()
                        listener?.success()
                    }

                    override fun onError(response: Response<Map<String, Any>>) {
                        super.onError(response)
                        listener?.error()
                    }
                }.setNeedLogin(if (showDialog != null && showDialog.size >= 2) showDialog[1] as Boolean else false)
                        .setShowErrorToast(if (showDialog != null && showDialog.size >= 3) showDialog[2] as Boolean else false))
    }

    /*
     * 用户信息监听
     */
    interface UserInfoListener {
        fun success()
        fun error()
    }

    /**
     * 判断是否登录
     */
    fun isLogin(activity: Activity): Boolean {
        if (TextUtils.isEmpty(MyApp.signId) || TextUtils.isEmpty(MyApp.uid) || MyApp.userInfo == null) {
            activity.startActivity(Intent(activity, LoginActivity::class.java))
            activity.finish()
            return false
        }
        return true
    }

    /**
     * 不需要跳转登录
     */
    fun isLogin(): Boolean {
        if (TextUtils.isEmpty(MyApp.signId) || TextUtils.isEmpty(MyApp.uid) || MyApp.userInfo == null) {
            return false
        }
        return true
    }


    /**
     * 获取设备ID
     */
    @SuppressLint("MissingPermission")
    fun getDeviceId(activity: Activity) {
        if (!SystemUtil.getIMEI(activity).isNullOrEmpty()) {
            MyApp.UUID = SystemUtil.getIMEI(activity)
        } else {
            MyApp.UUID = SystemUtil.getDeviceBrand() + SystemUtil.getSystemVersion() + getMac(activity)
        }
    }

    //本方法判断自己些的一个Service-->com.android.controlAddFunctions.PhoneService是否已经运行
    fun isWorkedService(context: Context, className: String): Boolean {
        val myManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningService = myManager.getRunningServices(30) as ArrayList<ActivityManager.RunningServiceInfo>
        for (i in runningService.indices) {
            if (runningService[i].service.className.toString().contains(className)) {
                return true
            }
        }
        return false
    }

    /**
     * 跳转应用授权
     */
    fun getAppDetailSettingIntent(activity: Activity, requestCode: Int) {
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            localIntent.data = Uri.fromParts("package", activity.packageName, null)
        } else {
            localIntent.action = Intent.ACTION_VIEW
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.packageName)
        }
        activity.startActivityForResult(localIntent, requestCode)
        activity.finish()
    }

}