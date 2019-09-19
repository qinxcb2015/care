package com.xxjr.xxyun.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.lzy.okgo.model.Response
import com.tbruyelle.rxpermissions2.RxPermissions
import com.umeng.socialize.UMAuthListener
import com.xxjr.xxyun.R
import com.xxjr.xxyun.connstant.Urls
import com.xxjr.xxyun.util.CommMath
import com.xxjr.xxyun.util.TimeWatchAuthCodo
import com.xxjr.xxyun.utils.SetTitleBar
import com.xxjr.xxyun.utils.network.MyOkGo
import com.xxjr.xxyun.utils.network.callback.DialogCallback
import com.xxjr.xxyun.widge.dialog.ContentViewDialog
import kotlinx.android.synthetic.main.activity_login.*
import java.util.HashMap
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.bean.SHARE_MEDIA
import com.xxjr.xxyun.utils.CommUtil
import com.umeng.socialize.UMShareConfig
import com.xxjr.xxyun.utils.TextUtil


/**
 * Created by wqg on 2017/3/27.14:09
 */

class LoginActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        SetTitleBar.setTitleText(this, null, getString(R.string.title_login), null)
        initListener()
    }

    fun initListener() {
        login_tv_dxLoginRandom.setOnClickListener {
            //获取验证码
            if (login_tv_dxLoginRandom!!.text.toString().trim().length > 11) {
                Toast.makeText(applicationContext, getString(R.string.notice_write_tel), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val page = System.currentTimeMillis()
            ContentViewDialog.picCode(this, page, object : ContentViewDialog.MyResult2Listener {
                override fun left(leftContent: Any?) {

                }

                override fun right(rightContent: Any?) {
                    sendDxLoginRandom(rightContent.toString(), page.toString())
                }
            })
        }
        login_btn_fastlogin.setOnClickListener {
            ksLogin()
        }
        login_ll_wxlogin.setOnClickListener {
            umengWxAuth()
        }
    }

    /**
     * 发送短信快捷登陆
     */
    private fun sendDxLoginRandom(picCode: String, page: String) {
        if (login_tv_dxLoginRandom!!.text.toString().trim().length > 11) {
            Toast.makeText(applicationContext, getString(R.string.notice_write_tel), Toast.LENGTH_SHORT).show()
            return
        }
        val map = HashMap<String, String>()
        map.put("telephone", login_et_tel!!.text.toString())
        map.put("imageCode", picCode)
        map.put("page", page)
        MyOkGo.postCust(Urls.LOGIN_SEND_RANDOM)
                ?.params(map)
                ?.execute(object : DialogCallback<Map<String, Any>>(this) {
                    override fun onSuccessMap(rMap: Map<String, Any>) {
                        Toast.makeText(this@LoginActivity, rMap["message"]?.toString(), Toast.LENGTH_SHORT).show()
                        setWaitTxt()
                    }
                })

    }

    /**
     * 定时器
     */
    private fun setWaitTxt() {
        val timewatch = TimeWatchAuthCodo(60000, 1000, this, login_tv_dxLoginRandom)
        timewatch.start()
    }

    /**
     * 快速登录
     */
    private fun ksLogin() {
        val map = HashMap<String, String>()
        if (login_tv_dxLoginRandom!!.text.toString().trim().length > 11) {
            Toast.makeText(applicationContext, getString(R.string.notice_write_tel), Toast.LENGTH_SHORT).show()
            return
        } else if (login_et_dxRandom!!.text.toString().isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.notice_write_random), Toast.LENGTH_SHORT).show()
            return
        }
        map.put("telephone", login_et_tel!!.text.toString().trim())
        map.put("randomNo", login_et_dxRandom!!.text.toString().trim())
        MyOkGo.postCust(Urls.KJ_LOGIN)
                ?.params(map)
                ?.execute(object : DialogCallback<Map<String, Any>>(this) {
                    override fun onSuccessMap(rMap: Map<String, Any>) {
                        val rowsLsist = rMap["rows"] as List<Map<String, Any>>
                        val attrMap = rMap["attr"] as Map<String, Any>
                        val uid = attrMap["customerId"].toString()
                        CommMath.putUidAndInfo(this@LoginActivity, uid, rowsLsist[0])
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                })
    }

    /**
     * 友盟微信登录
     */
    private fun umengWxAuth() {
        UMShareAPI.get(this).setShareConfig(UMShareConfig().isNeedAuthOnGetUserInfo(true))
        UMShareAPI.get(this).getPlatformInfo(this, SHARE_MEDIA.WEIXIN, object : UMAuthListener {
            override fun onStart(share_media: SHARE_MEDIA) {
            }

            override fun onComplete(share_media: SHARE_MEDIA, i: Int, data: Map<String, String>) {
                Toast.makeText(applicationContext, getString(R.string.wechat_success), Toast.LENGTH_SHORT).show()
                val unionid = data["unionid"]
                wxLogin(unionid ?: "")
            }

            override fun onError(share_media: SHARE_MEDIA, i: Int, throwable: Throwable) {
                CommUtil.ToastShow(this@LoginActivity, getString(R.string.wechat_fail))
            }

            override fun onCancel(share_media: SHARE_MEDIA, i: Int) {
                CommUtil.ToastShow(this@LoginActivity, getString(R.string.wechat_cancel))
            }
        })
    }

    /**
     * 微信登录
     */
    private fun wxLogin(unionid: String) {
        val map = HashMap<String, String>()
        map.put("unionid", unionid)
        MyOkGo.postCust(Urls.WX_LOGIN)
                ?.params("unionid", unionid)
                ?.execute(object : DialogCallback<Map<String, Any>>(this) {
                    override fun onSuccessMap(rMap: Map<String, Any>) {
                        val rowsLsist = rMap["rows"] as List<Map<String, Any>>
                        val attrMap = rMap["attr"] as Map<String, Any>
                        val uid = TextUtil.getTextToString(attrMap["customerId"])
                        if (rowsLsist.isEmpty()) {
                            CommUtil.ToastShow(this@LoginActivity, "用户信息为空，请联系客服人员")
                        } else {
                            CommMath.putUidAndInfo(this@LoginActivity, uid, rowsLsist[0])
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    }

                    override fun onError(response: Response<Map<String, Any>>) {
                        super.onError(response)

                    }

                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
    }


}
