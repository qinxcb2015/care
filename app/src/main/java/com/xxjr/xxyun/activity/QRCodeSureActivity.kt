package com.xxjr.xxyun.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.lzy.okgo.model.Response
import com.xxjr.xxyun.R
import com.xxjr.xxyun.bean.NettyBean
import com.xxjr.xxyun.connstant.Urls
import com.xxjr.xxyun.netty.MyNettyClient
import com.xxjr.xxyun.utils.CommUtil
import com.xxjr.xxyun.utils.SetTitleBar
import com.xxjr.xxyun.utils.TextUtil
import com.xxjr.xxyun.utils.network.MyOkGo
import com.xxjr.xxyun.utils.network.callback.DialogCallback
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_qrcode_sure.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.reactivestreams.Subscription
import java.util.HashMap
import java.util.concurrent.TimeUnit

class QRCodeSureActivity : BaseActivity() {

    private var message: String? = null
    private var sessionId: String? = null
    var countTime = 30
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_qrcode_sure)
        SetTitleBar.setTitleText(this,null,getString(R.string.title_qrcode_login),null)
        message = intent.getStringExtra("message")
        sessionId = intent.getStringExtra("sessionId")
        qrcode_ll_noticeLogin.text = message?:qrcode_ll_noticeLogin.text
        qrcode_tv_notice.text = getString(R.string.wait_PC_login)+"$countTime..."

    }

    /**
     * 接收netty信息
     */
    @SuppressLint("MissingPermission")
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun accectNettyMsg(nettyBean: NettyBean) {
        val msgMap = nettyBean.getrMap()
        val cmdName = TextUtil.getString(msgMap["cmdName"], "0001")
        if (cmdName == "0003") {
            CommUtil.ToastShow(this, "PC端登录成功")
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
    fun qrcodeOnSure(v :View){
        upLogin()
    }

    fun qrcodeOnCancel(v :View){
        finish()
    }

    /**
     * 扫码登录
     */
    private fun upLogin() {
        MyOkGo.postSys(Urls.QRCODE_LOGIN)
                ?.params("sessionId", sessionId)
                ?.execute(object : DialogCallback<Map<String, Any>>(this) {
                    override fun onSuccessMap(rMap: Map<String, Any>) {
                        qrcode_ll_notice.visibility = View.GONE
                        qrcode_tv_notice.visibility = View.VISIBLE

                        timing()
                    }

                })
    }

    private fun timing(){
        val mObservable : Observable<Long> = Observable.interval(0, 1, TimeUnit.SECONDS)
        mObservable.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    countTime --
                    if (countTime <0 )  finish()
                    qrcode_tv_notice.text = getString(R.string.wait_PC_login)+"  $countTime s..."

                }

    }
}
