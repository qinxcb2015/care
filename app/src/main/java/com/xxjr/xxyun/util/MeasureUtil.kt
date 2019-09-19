package com.xxjr.xxyun.utils

import android.app.Activity
import android.content.Context
import android.view.View
import com.xxjr.xxyun.app.MyApp


/**
 * Created by sty on 2017/8/1.
 */

object MeasureUtil {


    /**
     * app状态栏的高度
     */
    fun appStatusBarHeight(mActivity: Activity): Int {
        var statusBarHeight2 = 25
        try {
            val clazz = Class.forName("com.android.internal.R\$dimen")
            val `object` = clazz.newInstance()
            val height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(`object`).toString())
            statusBarHeight2 = mActivity.getResources().getDimensionPixelSize(height)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return statusBarHeight2
    }

    /*
    * 屏幕宽度
    */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * 屏幕高度
     */
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /*
 * 获取控件宽
 */
    fun measureView(view: View) {
        val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(w, h)
    }

    /*
    * 获取控件宽
    */
    fun getWidth(view: View): Int {
        val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(w, h)
        return view.measuredWidth
    }


    /*
    * 获取控件高
    */
    fun getHeight(view: View): Int {
        val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(w, h)
        return view.measuredHeight
    }

    /**
     * 通过屏幕比例获取宽肩要设置的宽度
     * @param referHeight 参照的高度
     */
    fun rotiaWidth(context: Context,  referHeight: Int): Int {
        return getScreenWidth(context) * referHeight / getScreenHeight(context)
    }

    /**
     * 通过屏幕比例获取宽肩要设置的高度
     * @param referWidth 参照的宽度
     */
    fun rotiaHeight(context: Context,  referWidth: Int): Int {
        return getScreenHeight(context) * referWidth / getScreenWidth(context)
    }

    /**
     * 通过设计图比例获取 宽度
     * @param referWidth 参照的宽度
     * @param referHeight 参照的高度
     */
    fun rotiaDesignWidth(context: Context,view: View?,  referWidth: Int?): Int {

        if (referWidth == null) {
            return ((getScreenWidth(context) * 1.0f / MyApp.designWidth) * (getWidth(view!!)  / MyApp.density)  * 2).toInt()
        } else {
            return (getScreenWidth(context) * 1.0f * referWidth / MyApp.designWidth).toInt()
        }
    }

    /**
     * 通过设计图比例获取 高度
     * @param referWidth 参照的宽度
     * @param referHeight 参照的高度
     */
    fun rotiaDesignHeight(context: Context, view: View?, referHeight: Int?): Int {
        if (referHeight == null) {
            return ((getScreenHeight(context) * 1.0f / MyApp.designHeight) * (getHeight(view!!)  / MyApp.density)  * 2).toInt()
        } else {
            return (getScreenHeight(context) * 1.0f * referHeight / MyApp.designHeight).toInt()
        }
    }



}
