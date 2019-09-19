package com.xxjr.xxyun.widge.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.LinearLayout
import com.xxjr.xxyun.R
import kotlinx.android.synthetic.main.dialog_loading.view.*

/**
 * Created by sty on 2017/8/31.
 */

class LoadingDialog {
    private var isCancleable = true
    private constructor()

    private var dialog: Dialog? = null
    fun createLoadingDialog( msg: String): Dialog? {
        if (dialog != null) {
            return null
        }
        val inflater = LayoutInflater.from(tmpContext)
        val v = inflater.inflate(R.layout.dialog_loading, null)// 得到加载view
        v.tipTextView.text = msg// 设置加载信息
        val loadingDialog = Dialog(tmpContext, R.style.MyDialogStyle)// 创建自定义样式dialog
        loadingDialog.setCancelable(true) // 是否可以按“返回键”消失
        loadingDialog.setCanceledOnTouchOutside(false) // 点击加载框以外的区域
        loadingDialog.setContentView(v.dialog_loading_view, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT))// 设置布局
        /**
         * 将显示Dialog的方法封装在这里面
         */
        val window = loadingDialog.window
        val lp = window!!.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.setGravity(Gravity.CENTER)
        window.attributes = lp
        window.setWindowAnimations(R.style.PopWindowAnimStyle)
        loadingDialog.show()
        dialog = loadingDialog
        loadingDialog.setOnDismissListener {
            dialog = null
            instance = null
        }
        return loadingDialog
    }


    fun setCancelable(isCancleable:Boolean):LoadingDialog{
        this.isCancleable = isCancleable
        return this
    }

    /**
     * 判断是否显示
     */
    fun isShowDialog(): Boolean{
        return dialog !=null && dialog!!.isShowing
    }
    /**
     * 关闭dialog
     * http://blog.csdn.net/qq_21376985
     * @param mDialogUtils
     */
    fun closeDialog() {
        if (dialog != null && dialog!!.isShowing) {
            try {
                dialog?.dismiss()
            }catch (ex: Exception){
                ex.printStackTrace()
            }
            dialog = null
            instance = null

        }
    }


    companion object {
        private var instance: LoadingDialog? = null
        private var tmpContext: Context? = null

        fun getInstance(context: Context): LoadingDialog {
            if (instance == null || tmpContext != context) {
                synchronized(LoadingDialog::class.java) {
                    if (instance == null || tmpContext != context) {
                        instance = LoadingDialog()
                    }
                }
            }
            tmpContext = context
            return instance!!
        }
    }
}
