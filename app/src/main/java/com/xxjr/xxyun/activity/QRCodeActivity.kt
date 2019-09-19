package com.xxjr.xxyun.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import com.google.zxing.Result
import com.google.zxing.client.android.BaseCaptureActivity
import com.lzy.okgo.model.Response
import com.xxjr.animallibrary.effects.Effectstype
import com.xxjr.xxyun.R
import com.xxjr.xxyun.connstant.Urls
import com.xxjr.xxyun.utils.CommUtil
import com.xxjr.xxyun.utils.TextUtil
import com.xxjr.xxyun.utils.network.MyOkGo
import com.xxjr.xxyun.utils.network.callback.DialogCallback
import com.xxjr.xxyun.widge.dialog.AnimalDialog
import com.xxjr.xxyun.widge.dialog.ContentViewDialog
import kotlinx.android.synthetic.main.activity_qrcode.*

class QRCodeActivity : BaseCaptureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

    }

    override fun onResume() {
        super.onResume()
        autoscanner_view.setCameraManager(cameraManager)
    }


    override fun getSurfaceView(): SurfaceView {
        return preview_view
    }

    override fun dealDecode(rawResult: Result, barcode: Bitmap, scaleFactor: Float) {
        playBeepSoundAndVibrate(true, false)
        if (rawResult.text.isNotEmpty()) {
            loginFlag(rawResult.text)
        } else {
            reScan()
        }

    }

    /**
     * 确认扫码登录
     */
    private fun loginFlag(url: String) {
        MyOkGo.postAll(url)
                ?.execute(object : DialogCallback<Map<String, Any>>(this,false) {
                    override fun onSuccessMap(rMap: Map<String, Any>) {
                        val attrMap: Map<String,Any> = rMap["attr"] as Map<String, Any>
                        val message : String? = attrMap["message"] as String?
                        val sessionId  = TextUtil.getTextToString( attrMap["sessionId"])
                        val intent = Intent(this@QRCodeActivity,QRCodeSureActivity::class.java)
                        intent.putExtra("message",message)
                        intent.putExtra("sessionId",sessionId)
                        startActivity(intent)
                        finish()

                    }

                    override fun onError(response: Response<Map<String, Any>>) {
                        super.onError(response)
                        reScan()
                    }
                })
    }


}
