package com.xxjr.xxyun.app

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.WindowManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheEntity
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.cookie.CookieJarImpl
import com.lzy.okgo.cookie.store.DBCookieStore
import com.lzy.okgo.https.HttpsUtils
import com.lzy.okgo.interceptor.HttpLoggingInterceptor
import com.lzy.okgo.model.HttpParams
import com.umeng.socialize.PlatformConfig
import com.xxjr.xxyun.BuildConfig
import com.xxjr.xxyun.connstant.Conn
import com.xxjr.xxyun.listener.LifecycleHandler
import com.xxjr.xxyun.netty.MyNettyClient
import com.xxjr.xxyun.util.SharedPrefUtil
import com.xxjr.xxyun.util.crash.CrashHandler
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import java.util.logging.Level

/**
 * Created by wqg on 2017/11/20.
 */
class MyApp : Application() {

    companion object {
        var uid: String? = null
        var signId: String? = null
        var UUID: String? = null
        var userInfo: Map<*, *>? = null
        var mTel: String? = null
        var designWidth = 640
        var designHeight = 1136
        var screenWidth: Int = 0
        var screenHeight: Int = 0
        var density: Float = 0.toFloat()
        var versionName = ""
        var versionCode = 0
        /**同步时间**/
        var synTime: Long = -1L
        /**最后一条记录的时间**/
        var lastTime: Long = -1L
        /**定时时间 5分钟**/
        val timing: Long = if (BuildConfig.DEBUG) 20 * 1000 else 5 * 60 * 1000
        /**是否正在录音**/
        var isRecording: Boolean = false
        /**是否正在上传通讯录 3秒内不让重复上传通讯录**/
        var isUpCall: Boolean = false

        var NETTY_IP: String? = null
        var NETTY_PORT: Int = -1
        lateinit var mAppContext: Context

        /**
         * 重置 用户所有的信息
         */
        fun resetUserInfo() {
            uid = null
            signId = null
            userInfo = null
            synTime = -1L
            lastTime = -1L
            NETTY_IP = ""
            NETTY_PORT = -1
            val sp = SharedPrefUtil(mAppContext, Conn.SP_USER_NAME)
            sp.clear()
            val spContacts = SharedPrefUtil(mAppContext, Conn.SP_UP_CONTACTS)
            spContacts.clear()
            MyNettyClient.getInstance().reaseResource()
        }

        fun setUpCalling() {
            isUpCall = true
            Log.e("打印  ", isUpCall.toString())
            Observable.timer(2L, TimeUnit.SECONDS).subscribe(object : Consumer<Long> {
                override fun accept(t: Long) {
                    isUpCall = false
                    Log.e("打印 是否正在录音 ", isUpCall.toString())
                }
            })
        }
    }

    override fun onCreate() {
        super.onCreate()
        initData()
        getDPI()
        registerActivityLifecycleCallbacks(LifecycleHandler())
        getVersionCodeAndName()
        initOkGo()

        com.umeng.socialize.utils.Log.LOG = true//LogCat中观察友盟日志
        PlatformConfig.setWeixin("wxa0a25731de29fa1d", "275d420c82f26c9e6b706b7379218d9e")

        if (BuildConfig.DEBUG) {
            CrashHandler.getInstance().init(this);
        } else {
            CrashHandler.getInstance().init(this)
        }
    }

    private fun initData() {
        mAppContext = this
        val timeSp = SharedPrefUtil(mAppContext, Conn.SP_UP_CONTACTS)
        synTime = timeSp.getLong(Conn.SP_UP_CONTACTS_SYN_TIME, -1L)
        lastTime = timeSp.getLong(Conn.SP_UP_CONTACTS_LAST_TIME, -1L)
    }

    private fun getDPI() {
        val dm = resources.displayMetrics
        density = dm.density
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        screenWidth = wm.defaultDisplay.width
        screenHeight = wm.defaultDisplay.height
    }

    fun getVersionCodeAndName() {
        try {
            // ---get the package info---
            val pm = packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            versionName = pi.versionName.replace("-debug", "")
            versionCode = pi.versionCode
            //  多渠道打包之后可能会出现  -360等
            val indexOf = versionName.indexOf("-")//  没有返回-1
            if (indexOf != -1) {
                versionName = versionName.substring(0, indexOf)
            }
        } catch (e: Exception) {
            Log.e("VersionInfo", "Exception", e)
        }

    }

    /**
     * 初始化okgo
     */
    private fun initOkGo() {
        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
//        val headers = HttpHeaders()
//        headers.put("signId", "commonHeaderValue1")    //header不支持中文，不允许有特殊字符
//        headers.put("commonHeaderKey2", "commonHeaderValue2")
        val params = HttpParams()
//        params.put("signId",signId)     //param支持中文,直接传,不要自己编码
//        params.put("UUID",UUID)
        //----------------------------------------------------------------------------------------//

        val builder = OkHttpClient.Builder()
        //log相关
        val loggingInterceptor = HttpLoggingInterceptor("OkGo")
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY)        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setColorLevel(Level.WARNING)                               //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor)                                 //添加OkGo默认debug日志
        //第三方的开源库，使用通知显示当前请求的log，不过在做文件下载的时候，这个库好像有问题，对文件判断不准确
        //builder.addInterceptor(new ChuckInterceptor(this));

        //超时时间设置，默认60秒
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)      //全局的读取超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)     //全局的写入超时时间
        builder.connectTimeout(10000, TimeUnit.MILLISECONDS)   //全局的连接超时时间

        //自动管理cookie（或者叫session的保持），以下几种任选其一就行
        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));            //使用sp保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(CookieJarImpl(DBCookieStore(this)))              //使用数据库保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));            //使用内存保持cookie，app退出后，cookie消失

        //https相关设置，以下几种方案根据需要自己设置
        //方法一：信任所有证书,不安全有风险
        val sslParams1 = HttpsUtils.getSslSocketFactory()
        //方法二：自定义信任规则，校验服务端证书
//        val sslParams2 = HttpsUtils.getSslSocketFactory(SafeTrustManager())
        //方法三：使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams3 = HttpsUtils.getSslSocketFactory(getAssets().open("srca.cer"));
        //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams4 = HttpsUtils.getSslSocketFactory(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"));
        builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager)
        //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
//        builder.hostnameVerifier(SafeHostnameVerifier())

        // 其他统一的配置
        // 详细说明看GitHub文档：https://github.com/jeasonlzy/
        /** 不使用缓存  NO_CACHE*/
        /** 请求网络失败后，读取缓存  REQUEST_FAILED_READ_CACHE*/
        /** 如果缓存不存在才请求网络，否则使用缓存  IF_NONE_CACHE_REQUEST*/
        /** 先使用缓存，不管是否存在，仍然请求网络  FIRST_CACHE_THEN_REQUEST*/
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3)                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
//                .addCommonHeaders(headers)                      //全局公共头
                .addCommonParams(params)                       //全局公共参数
    }


}