package com.xxjr.xxyun.connstant

/**
 * Created by wqg on 2017/11/21.
 */
object Urls {
     // 曾 167
    val CUST_URL = "http://192.168.10.130/xxcust"
//        val CUST_URL = "https://newapp.xxjr.com/xxcust";
    val BUSI_URL = "http://192.168.10.130/busi"
//        val BUSI_URL = "https://newapp.xxjr.com/busi";
    val SYS_URL = "http://192.168.10.130/sysAction"
//    val SYS_URL = "https://www.xxjr.com/sysAction"

    val KJ_LOGIN = "/comm/app/xdjl/xxyLogin"//快捷登录  /cust
    val WX_LOGIN = "/comm/app/xdjl/xxyWxLogin"//微信登录  /cust
    val CUST_INFO = "/account/query/getInfo"//用户详情  cust
    val GET_NETTY_IP = "/comm/app/xdjl/queryServerInfo"// 获取ip
    val UP_CONTACTS = "/account/store/call/batchUploadCallRecord"//上传通讯录
    val GET_NEWEST_TIME = "/account/store/call/recentUpLoadTime"// 获取上次服务器最新时间


    // 获取短信验证码的
    val LOGIN_PIC_CODE = "/smsAction/imageCode"//图形验证码+
    val LOGIN_SEND_RANDOM = "/smsAction/newNologin/xxyKjLoginKey"//短信验证码

    val QRCODE_LOGIN = "/confLogin"// 扫码登录
    val RECORD_TO_SERVE = "/account/store/call/uploadCallAudio"//上传录音

}