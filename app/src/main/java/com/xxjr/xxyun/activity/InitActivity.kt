package com.xxjr.xxyun.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.view.View
import com.tbruyelle.rxpermissions2.RxPermissions
import com.xxjr.animallibrary.effects.Effectstype
import com.xxjr.xxyun.R
import com.xxjr.xxyun.app.MyApp
import com.xxjr.xxyun.connstant.Conn
import com.xxjr.xxyun.util.CommMath
import com.xxjr.xxyun.util.SharedPrefUtil
import com.xxjr.xxyun.widge.dialog.AnimalDialog
import com.xxjr.xxyun.widge.dialog.ContentViewDialog
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class InitActivity : AppCompatActivity() {
    var count: Int = 0
    var grantArray = arrayOf(0, 0, 0, 0, 0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)
        checkPemission()
    }

    /**
     * 校验权限
     */
    private fun checkPemission() {
        count = 0
        RxPermissions(this@InitActivity).requestEach(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO)
                .subscribe { permission ->
                    if (permission.granted) {
                        grantArray[count] = 0
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        grantArray[count] = 1
                    } else {
                        grantArray[count] = 2
                    }
                    count++
                    if (count == grantArray.size) {
                        grantArray.forEach {
                            if (it == 1) {
                                refuse()
                                return@subscribe
                            }
                        }
                        grantArray.forEach {
                            if (it == 2) {
                                ContentViewDialog.refusePermissionForever(this@InitActivity, getString(R.string.setting_grant), null)
                                return@subscribe
                            }
                        }
                        CommMath.getDeviceId(this@InitActivity)
                        getUserInfo()
                    }
                }
    }

    /**
     * 拒绝之后的对话框
     */
    private fun refuse() {
        var dialogView: View = ContentViewDialog
                .setMyDialog2Listener(object : ContentViewDialog.MyDialog2Listener {
                    override fun left() {
                        AnimalDialog.getInstance(this@InitActivity).dismissDialog()
                        finish()
                    }

                    override fun right() {
                        AnimalDialog.getInstance(this@InitActivity).dismissDialog()
                        checkPemission()
                    }
                })
                .notice2(this, getString(R.string.init_grantTitle), getString(R.string.init_grantContent),
                        getString(R.string.cancle), getString(R.string.sure))
        AnimalDialog.getInstance(this@InitActivity)
                .showDialog()
                .setDialogView(dialogView)
                .withEffect(Effectstype.Fadein)
                .setCanceledOnTouchOutside(false)
                .setCancelable(false)
    }


    /**
     * 获取用户信息
     */
    private fun getUserInfo() {
        val sp = SharedPrefUtil(this, Conn.SP_USER_NAME)
        MyApp.uid = sp.getString(Conn.SP_USER_UID, null)
        MyApp.signId = sp.getString(Conn.SP_USER_SIGNID, null)
        CommMath.getUserInfo(this, object : CommMath.UserInfoListener {
            override fun success() {
                toActivity(false)
            }

            override fun error() {
                toActivity(true)
            }

        }, false)

    }

    /**
     * 是否存有用户信息
     * 跳转 登录 或者 主页
     */
    private fun toActivity(toLogin: Boolean) {
        Observable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long> {

                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {
                        if (toLogin) {
                            startActivity(Intent(this@InitActivity, LoginActivity::class.java))
                        } else {
                            startActivity(Intent(this@InitActivity, MainActivity::class.java))
                        }
                        finish()
                    }

                    override fun onNext(t: Long) {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }
                })
    }

}
