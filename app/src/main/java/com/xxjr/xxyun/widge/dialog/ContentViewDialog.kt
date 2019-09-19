package com.xxjr.xxyun.widge.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.nineoldandroids.animation.ObjectAnimator
import com.xxjr.animallibrary.effects.Effectstype
import com.xxjr.xxyun.R
import com.xxjr.xxyun.connstant.Urls
import com.xxjr.xxyun.util.CommMath
import com.xxjr.xxyun.utils.CommUtil
import com.xxjr.xxyun.utils.MeasureUtil
import com.xxjr.xxyun.utils.network.MyOkGo
import com.xxjr.xxyun.utils.network.callback.BitmapDialogCallback
import kotlinx.android.synthetic.main.comm_dialog_notice1.view.*
import kotlinx.android.synthetic.main.comm_dialog_notice2.view.*
import kotlinx.android.synthetic.main.dialog_pic_code.view.*


/**
 * Created by sty on 2017/8/29.
 */

object ContentViewDialog {

    private var listener: MyDialogListener? = null
    private var listener2: MyDialog2Listener? = null
    private var cameraListener: MyCameraListener? = null
    private var result2Listener: MyResult2Listener? = null


    interface MyDialog2Listener {
        fun left()
        fun right()
    }

    interface MyDialogListener {
        fun bttom()
    }

    interface MyCameraListener {
        fun cameta()
        fun photo()
        fun cancle()
    }

    interface MyResult2Listener{
        fun left(leftContent:Any?)
        fun right(rightContent:Any?)
    }

    fun setMyDialog2Listener(listener2: MyDialog2Listener): ContentViewDialog {
        this.listener2 = listener2
        return this
    }

    fun setMyDialogListener(listener: MyDialogListener): ContentViewDialog {
        this.listener = listener
        return this
    }

    fun setMyCameraListener(cameraListener: MyCameraListener): ContentViewDialog {
        this.cameraListener = cameraListener
        return this
    }


    /**
     * 提示
     * 取消和确定
     */
    fun notice2(context: Context, title: String, content: String, left: String, right: String): View {
        var dialogView: View = LayoutInflater.from(context).inflate(R.layout.comm_dialog_notice2, null)
        dialogView.dialog_title2.text = title
        dialogView.dialog_content2.text = content
        dialogView.dialog_left2.text = left
        dialogView.dialog_right2.text = right
        dialogView.dialog_left2.setOnClickListener {
            if (listener2 != null) {
                listener2!!.left()
            }
        }
        dialogView.dialog_right2.setOnClickListener {
            if (listener2 != null) {
                listener2!!.right()
            }
        }
        return dialogView
    }

    /**
     * 提示 单个
     */
    fun notice1(context: Context, content: String, bottom: String): View {
        var dialogView: View = LayoutInflater.from(context).inflate(R.layout.comm_dialog_notice1, null)
        dialogView.dialog_content1.text = content
        dialogView.dialog_bottom1.text = bottom
        dialogView.dialog_bottom1.setOnClickListener {
            if (listener != null) {
                listener!!.bttom()
            }
        }
        return dialogView
    }

    fun picCode(activity: Activity, page: Long, result2Listener: MyResult2Listener){
        //获取验证码
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_pic_code, null)
        getPicCode(activity,dialogView.dialog_login_iv_loading, dialogView.dialog_login_iv_picCode,page)
        dialogView.dialog_login_iv_picCode.layoutParams.height = MeasureUtil.rotiaDesignHeight(activity, dialogView.dialog_login_iv_picCode, 60)
        dialogView.dialog_login_iv_picCode.layoutParams.width = MeasureUtil.rotiaDesignWidth(activity, dialogView.dialog_login_iv_picCode, 160)

        var animalDialog = AnimalDialog.getInstance(activity)
                .showDialog()
                .setDialogView(dialogView)
                .withEffect(Effectstype.Fadein)
                .setCanceledOnTouchOutside(false)
                .setCancelable(false)
                .canInputkeyboard()
                .setDuration(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            animalDialog.setOnDismissListener {
                AnimalDialog.getInstance(activity).dismissDialog()
                dialogView.dialog_login_iv_picCode.setImageBitmap(null)
            }
        }
        dialogView.dialog_login_ll_refresh.setOnClickListener {
            OkGo.getInstance().cancelTag(Urls.LOGIN_PIC_CODE)
            getPicCode(activity,dialogView.dialog_login_iv_loading, dialogView.dialog_login_iv_picCode,page)
        }
        dialogView.dialog_login_btn_cancel.setOnClickListener {
            AnimalDialog.getInstance(activity).dismissDialog()
            result2Listener.left(null)
        }
        dialogView.dialog_login_btn_sure.setOnClickListener {
            if (dialogView.dialog_login_et_picCode.text.toString().isEmpty()){
                CommUtil.ToastShow(activity,"请输入图形验证码")
            }else {
                AnimalDialog.getInstance(activity).dismissDialog()
                result2Listener.right(dialogView.dialog_login_et_picCode.text)
            }
        }
    }


    /**
     * 永久拒绝 - 并且没有取消按键
     * 用于强制获取
     */
    fun refusePermissionForever(activity: Activity,content: String,requestCode:Int?) {
        var dialogView: View = ContentViewDialog
                .setMyDialogListener(object : ContentViewDialog.MyDialogListener {
                    override fun bttom() {
                        AnimalDialog.getInstance(activity).dismissDialog()
                        CommMath.getAppDetailSettingIntent(activity, requestCode?:0x000)
                    }
                })
                .notice1(activity, content,
                        activity.getString(R.string.I_know))
        AnimalDialog.getInstance(activity)
                .showDialog()
                .setDialogView(dialogView)
                .withEffect(Effectstype.Fadein)
                .setCanceledOnTouchOutside(false)
                .setCancelable(false)
    }

    /**
     * 获取图形验证码
     */
    private fun getPicCode(activity: Activity, loadingIv: ImageView, mPicCode: ImageView, page:Long) {
        val animator = ObjectAnimator.ofFloat(loadingIv, "Rotation", 0f, 360f)
        // 设置持续时间
        animator.duration = 1000;
        val lin = LinearInterpolator()
        animator.interpolator = lin
        animator.start()
        MyOkGo.custBitmap(Urls.LOGIN_PIC_CODE)
                ?.params("page",page)
                ?.execute(object : BitmapDialogCallback(activity) {
                    override fun onSuccess(p0: Response<Bitmap>?) {
                        mPicCode.setImageBitmap(p0?.body())
                    }

                    override fun onError(response: Response<Bitmap>?) {
                        super.onError(response)
                        mPicCode.setImageResource(R.mipmap.pil_code_error)
                    }
                }.setShowDialog(false))
    }



}
