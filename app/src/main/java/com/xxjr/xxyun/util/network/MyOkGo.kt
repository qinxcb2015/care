package com.xxjr.xxyun.utils.network

import android.graphics.Bitmap
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.request.PostRequest
import com.xxjr.xxyun.app.MyApp
import com.xxjr.xxyun.connstant.Urls
import com.xxjr.xxyun.utils.CommUtil

/**
 * Created by sty on 2017/8/3.
 */

object MyOkGo {

    /**
     * param
     * 0 缓存
     */
    fun postCust(postUrl: String, vararg param: Any?): PostRequest<Map<String, Any>>? {
        if (param.isNotEmpty() && param[0] !is CacheMode) {
            CommUtil.Log("为空", " 不是一个类型")
            return null
        }

        val request: PostRequest<Map<String, Any>>? = OkGo.post<Map<String, Any>>(Urls.CUST_URL+postUrl)
                .cacheKey(postUrl)
                .cacheMode(if (param.isNotEmpty() && param[0] == null) param[0] as CacheMode? else CacheMode.NO_CACHE)  //这里完全同okgo的配置一样
                .tag(this)//手动阀的as9df8as6df846asd
                .params("signId", MyApp.signId)
                .params("UUID", MyApp.UUID)
                .params("appVersion","xxyun"+MyApp.versionName)
        CommUtil.Log("signId",MyApp.signId +",  UUID ->"+MyApp.UUID +",  postUrl->"+postUrl)
        return request
    }

    /**
     * param
     */
    fun postBusi(postUrl: String, vararg param: Any?): PostRequest<Map<String, Any>>? {
        if (param.isNotEmpty() && param[0] !is CacheMode) {
            CommUtil.Log("为空", " 不是一个类型")
            return null
        }

        val request: PostRequest<Map<String, Any>>? = OkGo.post<Map<String, Any>>(Urls.BUSI_URL+postUrl)
                .cacheKey(postUrl)
                .cacheMode(if (param.isNotEmpty() && param[0] == null) param[0] as CacheMode? else CacheMode.NO_CACHE)  //这里完全同okgo的配置一样
                .tag(this)//手动阀的as9df8as6df846asd
                .params("signId",MyApp.signId)
                .params("UUID", MyApp.UUID)
                .params("appVersion","xxyun"+MyApp.versionName)
        CommUtil.Log("signId",MyApp.signId +",  UUID ->"+MyApp.UUID +",  postUrl->"+postUrl)
        return request
    }

    /**
     * param
     */
    fun postSys(postUrl: String, vararg param: Any?): PostRequest<Map<String, Any>>? {
        if (param.isNotEmpty() && param[0] !is CacheMode) {
            CommUtil.Log("为空", " 不是一个类型")
            return null
        }

        val request: PostRequest<Map<String, Any>>? = OkGo.post<Map<String, Any>>(Urls.SYS_URL+postUrl)
                .cacheKey(postUrl)
                .cacheMode(if (param.isNotEmpty() && param[0] == null) param[0] as CacheMode? else CacheMode.NO_CACHE)  //这里完全同okgo的配置一样
                .tag(this)//手动阀的as9df8as6df846asd
                .params("signId",MyApp.signId)
                .params("UUID", MyApp.UUID)
                .params("appVersion","xxyun"+MyApp.versionName)
        CommUtil.Log("signId",MyApp.signId +",  UUID ->"+MyApp.UUID +",  postUrl->"+postUrl)
        return request
    }

    fun postAll(postUrl: String, vararg param: Any?): PostRequest<Map<String, Any>>? {
        if (param.isNotEmpty() && param[0] !is CacheMode) {
            CommUtil.Log("为空", " 不是一个类型")
            return null
        }

        val request: PostRequest<Map<String, Any>>? = OkGo.post<Map<String, Any>>(postUrl)
                .cacheKey(postUrl)
                .cacheMode(if (param.isNotEmpty() && param[0] == null) param[0] as CacheMode? else CacheMode.NO_CACHE)  //这里完全同okgo的配置一样
                .tag(this)//手动阀的as9df8as6df846asd
                .params("signId",MyApp.signId/*"A4B9940FC360CABF765D231493AB122B"*/)
                .params("UUID", MyApp.UUID)
                .params("appVersion","xxyun"+MyApp.versionName)
        CommUtil.Log("signId",MyApp.signId +",  UUID ->"+MyApp.UUID +",  postUrl->"+postUrl)
        return request
    }

    fun custBitmap(postUrl: String): PostRequest<Bitmap>? {
        val request: PostRequest<Bitmap>? =  OkGo.post<Bitmap>(Urls.CUST_URL + postUrl)
                .cacheKey(postUrl)
                .tag(this)//手动阀的as9df8as6df846asd
                .params("signId", MyApp.signId)
                .params("UUID", MyApp.UUID)
                .params("appVersion", "xxyun" + MyApp.versionName)
        CommUtil.Log("signId", MyApp.signId + ",  UUID ->" + MyApp.UUID + ",  postUrl->" + postUrl)
        return request
    }

}
