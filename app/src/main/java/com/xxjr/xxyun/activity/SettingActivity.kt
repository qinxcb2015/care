package com.xxjr.xxyun.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.xxjr.xxyun.R
import com.xxjr.xxyun.app.MyApp
import com.xxjr.xxyun.netty.MyNettyClient
import com.xxjr.xxyun.util.SystemUtil
import com.xxjr.xxyun.utils.CommUtil
import com.xxjr.xxyun.utils.SetTitleBar
import com.xxjr.xxyun.utils.TextUtil
import com.xxjr.xxyun.widge.glideTransfrom.GlideCircleImageview
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        SetTitleBar.setTitleText(this,null,getString(R.string.title_set),null)
        initData()
        initListener()
    }

    private fun initData(){
        set_tv_name.text =TextUtil.getString( MyApp.userInfo?.get("realName"),"姓名")
        set_tv_tel.text =TextUtil.getString( MyApp.userInfo?.get("hideTelephone"),"电话")
        Glide.with(this)
                .load(TextUtil.getTextToString(MyApp.userInfo?.get("userImage")))
                .transform(GlideCircleImageview(this))
                .placeholder(R.mipmap.toux)
                .into(set_iv_head)
    }

    override fun onResume() {
        super.onResume()
        val a1 = SystemUtil.getDeviceBrand()
        val a2 = SystemUtil.getSystemModel()
        val a3 = SystemUtil.getSystemVersion()
        set_tv_telInfo.text = SystemUtil.getSystemModel()+" "+ SystemUtil.getSystemVersion()
        CommUtil.Log("getDeviceBrand",SystemUtil.getDeviceBrand())
        CommUtil.Log("getSystemModel",SystemUtil.getSystemModel())
        CommUtil.Log("getSystemVersion",SystemUtil.getSystemVersion())
    }

    private fun initListener(){
        set_tv_changeAccount.setOnClickListener {
            logout()
        }
        set_ll_logout.setOnClickListener {
            logout()
        }
    }

    fun logout(){
        MyApp.resetUserInfo()
        MyNettyClient.getInstance().reaseResource()
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }
}
