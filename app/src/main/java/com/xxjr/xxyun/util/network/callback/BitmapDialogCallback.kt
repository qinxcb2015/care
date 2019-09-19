package com.xxjr.xxyun.utils.network.callback

import android.app.Activity
import android.graphics.Bitmap

import com.lzy.okgo.callback.BitmapCallback
import com.lzy.okgo.request.base.Request
import com.xxjr.xxyun.widge.dialog.LoadingDialog

import okhttp3.Response

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2016/1/14
 * 描    述：请求图图片的时候显示对话框
 * 修订历史：
 * ================================================
 */
abstract class BitmapDialogCallback() : BitmapCallback(1000, 1000) {

    private var activity: Activity? = null
    private var isShowDialog = true
    /**
     * loading提示语
     */
    private var loadingDes = "请求中..."
    /**
     * 对话框返回键是否取消
     */
    private var isLoadingCancelable = false
    /**
     * 是否关闭对话框
     */
    private var isAutoCloseDialog: Boolean = true

    constructor(activity: Activity) : this() {
        this.activity = activity
    }

    override fun onStart(request: Request<Bitmap, out Request<*, *>>?) {
        if (isShowDialog && !LoadingDialog.getInstance(activity!!).isShowDialog()) {
            LoadingDialog.getInstance(activity!!).setCancelable(isCancleable = isLoadingCancelable).createLoadingDialog(loadingDes)
        }
    }

    override fun onFinish() {
        dismissLoadingDialog()

    }

    protected fun dismissLoadingDialog() {
        if (isShowDialog && isAutoCloseDialog) {
            LoadingDialog.getInstance(activity!!).closeDialog()
            isShowDialog = false
        }
    }


    fun setLoadingDes(loadingDes: String): BitmapDialogCallback {
        this.loadingDes = loadingDes
        return this
    }

    fun setShowDialog(showDialog: Boolean): BitmapDialogCallback {
        isShowDialog = showDialog
        return this
    }

    fun setLoadingCancelable(isLoadingCancelable: Boolean): BitmapDialogCallback {
        this.isLoadingCancelable = isLoadingCancelable
        return this
    }

    fun setIsAutoCloseDialog(isAutoCloseDialog: Boolean): BitmapDialogCallback {
        this.isAutoCloseDialog = isAutoCloseDialog
        return this
    }
}
