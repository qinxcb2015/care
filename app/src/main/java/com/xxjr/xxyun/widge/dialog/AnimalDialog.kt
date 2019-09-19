package com.xxjr.xxyun.widge.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import com.xxjr.animallibrary.effects.Effectstype
import com.xxjr.xxyun.R

/**
 * Created by sty on 2017/8/29.
 */

class AnimalDialog : AlertDialog.Builder {
    private var type: Effectstype? = null
    private var mDuration = -1
    private var dialog: Dialog? = null
    private var dismissListener: DismissListener? = null

    private constructor(context: Context) : super(context) {
        init()
    }

    private constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        init()
    }


    fun showDialog(): AnimalDialog {
        dialog!!.show()
        return this
    }

    fun dismissDialog(): AnimalDialog {
        dialog?.dismiss()
        dialog == null
        instance = null
        return this
    }


    private fun init() {
        dialog = this.create()
        val params = dialog!!.window!!.attributes
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params.gravity = Gravity.CENTER
        dialog!!.window!!.attributes = params
        dialog!!.setOnShowListener {
            if (type == null) {
                type = Effectstype.Slidetop
            }
            start(type!!)
        }
        dialog!!.setOnDismissListener {
            dismissListener?.dismissDialog()
            dismissDialog()
        }
    }

    /**
     * 设置view到对话框
     */
    fun setDialogView(view: View): AnimalDialog {
        dialog!!.window!!.setContentView(view)
        return this
    }

    /*
     * 开始动画的
     */
    private fun start(type: Effectstype) {
        val animator = type.animator
        if (mDuration != -1) {
            animator.setDuration(Math.abs(mDuration).toLong())
        }
        animator.start(dialog!!.window!!.decorView)
    }

    /**
     * 设置时间
     */
    fun setDuration(duration: Int): AnimalDialog {
        this.mDuration = duration
        return this
    }

    fun setCanceledOnTouchOutside(flag: Boolean): AnimalDialog {
        this.dialog!!.setCanceledOnTouchOutside(flag)
        return this
    }

    override fun setCancelable(flag: Boolean): AnimalDialog {
        this.dialog!!.setCancelable(flag)
        return this
    }

    /**
     * 针对editext的软键盘弹出
     */
    fun canInputkeyboard():AnimalDialog{
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        return this
    }

    fun withEffect(type: Effectstype): AnimalDialog {
        this.type = type
        return this
    }

    fun setDialogListener(dismissListener: DismissListener) : AnimalDialog{
        this.dismissListener = dismissListener
        return this
    }

    /**
     * 设置位置，默认是居中的
     */
    fun setGravity(gravity: Int):  AnimalDialog{
        dialog!!.window!!.attributes.gravity =  gravity
        return this
    }

    companion object {
        private var instance: AnimalDialog? = null
        private var tmpContext: Context? = null

        fun getInstance(context: Context): AnimalDialog {
            if (instance == null || tmpContext != context) {
                synchronized(AnimalDialog::class.java) {
                    if (instance == null || tmpContext != context) {
                        instance = AnimalDialog(context, R.style.animalDialog)
                    }
                }
            }
            tmpContext = context
            return instance!!
        }
    }

    interface DismissListener {
        fun dismissDialog()
    }

}
