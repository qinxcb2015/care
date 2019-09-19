package com.xxjr.xxyun.utils.network.callback

import android.app.ProgressDialog
import android.content.Context
import android.text.TextUtils
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.xxjr.xxyun.app.MyApp
import com.xxjr.xxyun.connstant.Conn
import com.xxjr.xxyun.util.SharedPrefUtil
import com.xxjr.xxyun.util.network.callback.JsonCallback
import com.xxjr.xxyun.utils.CommUtil
import com.xxjr.xxyun.utils.TextUtil
import com.xxjr.xxyun.widge.dialog.LoadingDialog

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2016/1/14
 * 描    述：对于网络请求是否需要弹出进度对话框
 * 修订历史：
 * ================================================
 */
abstract class DialogCallback<T> : JsonCallback<T> {

    private var dialog: ProgressDialog? = null
    private var context: Context? = null
    /**
     * loading提示语
     */
    private var loadingDes =  "请求中..."
    /**
     * 错误时候是否提示Toast
     */
    private var isShowErrorToast: Boolean = true
    /**
     * 错误内容
     */
    private var errorMsg: String? = null
    /**
     * 异常内容
     */
    private var exceptionMsg: String? = null
    /**
     * 是否需要登录  true默认跳转登录  false 不跳转
     */
    private var needLogin: Boolean = true
    /**
     * 是否需要加载Loading对话框
     */
    private var isShowDialog: Boolean = true

    /**
     * 是否关闭对话框
     */
    private var isAutoCloseDialog: Boolean = true

    /**
     * 是否加载成功标志位
     */
    private var isLoadSuccess: Boolean = true

    /**
     * 错误代码
     * 99 未登录
     */
    private var errorCode: Any? = -1
    /**
     * 在success == false  的时候是否要map
     */
    private var isNeedMap = false

    /**
     * 对话框返回键是否取消
     */
    private var isLoadingCancelable = false


    constructor(context1: Context) : super() {
        this.context = context1
    }

    constructor(context1: Context, isShowDialog: Boolean) {
        this.context = context1
        this.isShowDialog = isShowDialog
    }

    override fun onStart(request: Request<T, out Request<*, *>>?) {
        if (isShowDialog && !LoadingDialog.getInstance(context!!).isShowDialog()) {
            LoadingDialog.getInstance(context!!).setCancelable(isCancleable = isLoadingCancelable).createLoadingDialog(loadingDes)
        }
    }

    override fun onSuccess(response: Response<T>) {
        getSignId(response)
        val url = response.rawResponse.networkResponse()?.request()?.url()?.toString()
        val body = response.body()
        val rMap: Map<String, Any>
        CommUtil.Log("response-$url ", body.toString())
        try {
            rMap = body as Map<String, Any>
            // 未登录
            errorCode = TextUtil.getInt(rMap["errorCode"], -1)
            if (isNeedMap) return
            if (errorCode == 99) {
                errorMsg = TextUtil.getTextToString(rMap.get("message"))
                SharedPrefUtil(context, Conn.SP_USER_NAME).clear()
                MyApp.resetUserInfo()
                onError(response)
                return
            }
            if (!TextUtil.getBoolean(rMap["success"],false)) {
                errorMsg = TextUtil.getTextToString(rMap["message"])
                onError(response)
                return
            }
        } catch (ex: Exception) {
            exceptionMsg = ex.message
            onError(response)
            return
        }
        onSuccessMap(rMap)
    }

    /*
     * 获取signID
     */
    private fun getSignId(response: Response<T>) {
        val headers = response.headers()
        val signId = headers.get("signId")
        if (!TextUtils.isEmpty(signId) && signId!!.contains("noApp")){
            SharedPrefUtil(context, Conn.SP_USER_NAME).clear()
            MyApp.resetUserInfo()
        }

        if (!TextUtils.isEmpty(signId)) {
            if (MyApp.signId !== signId) {
                MyApp.signId = signId
                putUserLoginSp()
            }
        }

    }

    /**
     * 存储用户的 UID  sigId
     * 200 内 存一次
     */
    fun putUserLoginSp() {
        val sharedPrefUtil = SharedPrefUtil(context, Conn.SP_USER_NAME)
        sharedPrefUtil.putString(Conn.SP_USER_SIGNID, MyApp.signId)
        sharedPrefUtil.commit()
    }


    override fun onCacheSuccess(response: Response<T>?) {
        if (response != null) {
            super.onCacheSuccess(response)
        }
        onCacheSuccessMap(response!!.body() as Map<String, Any>)

    }

    open fun onCacheSuccessMap(rMap: Map<String, Any>) {
        onSuccessMap(rMap)
    }

    abstract fun onSuccessMap(rMap: Map<String, Any>)


    override fun onFinish() {
        dismissLoadingDialog()

    }

    protected fun dismissLoadingDialog() {
        if (isShowDialog && isAutoCloseDialog) {
            LoadingDialog.getInstance(context!!).closeDialog()
            isShowDialog = false
        }
    }

    override fun onError(response: Response<T>) {
        super.onError(response)
        isLoadSuccess = false
        if (isShowErrorToast) {
            if (errorMsg != null) {
                CommUtil.ToastShow(context!!, "error: " + errorMsg!!)
                CommUtil.Log("onError：", "e: " + errorMsg!!)
            } else if (exceptionMsg != null) {
                CommUtil.ToastShow(context!!, "ex: " + exceptionMsg)
                CommUtil.Log("onError：", "ex: " + exceptionMsg)
            } else {
                CommUtil.ToastShow(context!!, "sevice: " + response.exception?.message)
                CommUtil.Log("onError：", "s: " + response.exception?.message)
            }
        }
    }

    fun setLoadingDes(loadingDes:String): DialogCallback<T> {
        this.loadingDes = loadingDes
        return this
    }

    fun setNeedLogin(needLoginFlag: Boolean): DialogCallback<T> {
        this.needLogin = needLoginFlag
        return this
    }

    fun setShowDialog(isShowDialog: Boolean): DialogCallback<T> {
        this.isShowDialog = isShowDialog
        return this
    }

    fun setIsAutoCloseDialog(isNeedCloseDialog: Boolean): DialogCallback<T> {
        this.isAutoCloseDialog = isNeedCloseDialog
        return this
    }

    fun setShowErrorToast(isShowErrorToast: Boolean): DialogCallback<T> {
        this.isShowErrorToast = isShowErrorToast
        return this
    }

    fun setLoadingCancelable(isLoadingCancelable: Boolean): DialogCallback<T> {
        this.isLoadingCancelable = isLoadingCancelable
        return this
    }

    fun isLoadSuccess(): Boolean {
        return isLoadSuccess
    }

    fun getErrorCode():Any?{
        return errorCode
    }

    fun getErrorMsg():String? {
        return errorMsg
    }

    }
