package com.xxjr.xxyun.utils

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import java.util.regex.Pattern

/**
 * Created by Administrator on 2016/4/14.
 */
object TextUtil {

    fun changColor(text: String, color: Int, start: Int, end: Int): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)
        val blackSpan = ForegroundColorSpan(color)
        builder.setSpan(blackSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return builder
    }

    fun getTextToString(`object`: Any?): String {
        try {
            return `object`?.toString()?.trim { it <= ' ' } ?: ""
        } catch (e: Exception) {
            return ""
        }
    }

    fun getInt(`object`: Any?, defaultInt: Int): Int {
        var value: Int = defaultInt
        try {
            var t1: Double = if (`object` == null) defaultInt.toDouble() else java.lang.Double.parseDouble(`object`.toString())
            value = t1.toInt()
        } catch (e: Exception) {
            value = defaultInt
        }

        return value
    }

    fun getDouble(`object`: Any?, defaultDouble: Double): Double {
        var value = defaultDouble
        try {
            value = if (`object` == null) defaultDouble else java.lang.Double.parseDouble(`object`.toString())
            java.text.DecimalFormat("#.00").format(value)
        } catch (e: Exception) {
            value = defaultDouble
        }

        return value
    }

    fun getString(`object`: Any?, defaultStr: String): String {
        try {
            return `object`?.toString() ?: defaultStr
        } catch (e: Exception) {
            return defaultStr
        }
    }

    /**
     * 判断map是否为空
     */
    fun <T,V>isEmptyMap(mMap:Map<T,V>?):Boolean{
        return mMap == null || mMap.isEmpty()
    }

    /**
     * 判断list是否为空
     */
    fun <T>isEmptyList(mList:List<T>):Boolean{
        return mList == null || mList.isEmpty()
    }

    /**
     * 获取 boolean值类型 ，

     * @param object
     * *
     * @param defaultBoolean
     * *
     * @return
     */
    fun getBoolean(`object`: Any?, defaultBoolean: Boolean): Boolean {
        return if (`object` == null) defaultBoolean else java.lang.Boolean.parseBoolean(`object`.toString())
    }


    /**
     * tv 部分字体颜色改变

     * @param tv
     * *
     * @param color
     */
    fun setTextviewParkColor(tv: TextView, color: Int, star: Int, end: Int) {
        val builder = SpannableStringBuilder(tv.text.toString())
        val redSpan = ForegroundColorSpan(color)
        builder.setSpan(redSpan, star, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv.text = builder
    }

    /**
     * tv 部分字体颜色改变

     * @param tv
     */
    fun setTextviewParkColor(tv: TextView, color1: Int, color2: Int, star1: Int, end1: Int, star2: Int, end2: Int) {
        val builder = SpannableStringBuilder(tv.text.toString())
        val redSpan1 = ForegroundColorSpan(color1)
        val redSpan2 = ForegroundColorSpan(color2)
        builder.setSpan(redSpan1, star1, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(redSpan2, star2, end2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv.text = builder
    }

    /**
     * 判断只有中文
     * @param str
     * @return
     */
    fun isContainChinese(str: String): Boolean {
        for (i in 0..str.length - 1) {
            val p = Pattern.compile("[\u4e00-\u9fa5]")
            val m = p.matcher(str.substring(i, i + 1))
            if (!m.find()) {
                return false
            }
        }
        return true
    }

    /**
     * tv设置drawable
     */
    fun setTvDrawable(tv: TextView, resources: Resources, leftRes: Int?, topRes: Int?, rightRes: Int?, bottomRes: Int?) {
        var leftdrawable : Drawable ?= null
        var topdrawable : Drawable ?= null
        var rightdrawable : Drawable ?= null
        var bottomdrawable : Drawable ?= null
        if (leftRes != null) {
            leftdrawable = ResourcesCompat.getDrawable(resources, leftRes, null)
            /// 这一步必须要做,否则不会显示.
            leftdrawable?.setBounds(0, 0, leftdrawable.getMinimumWidth(), leftdrawable.getMinimumHeight());
        }
        if (topRes != null) {
            topdrawable = ResourcesCompat.getDrawable(resources, topRes, null)
            topdrawable?.setBounds(0, 0, topdrawable.getMinimumWidth(), topdrawable.getMinimumHeight());
        }
        if (rightRes != null) {
            rightdrawable = ResourcesCompat.getDrawable(resources, rightRes, null)
            rightdrawable?.setBounds(0, 0, rightdrawable.getMinimumWidth(), rightdrawable.getMinimumHeight());
        }
        if (bottomRes != null) {
            bottomdrawable = ResourcesCompat.getDrawable(resources, bottomRes, null)
            bottomdrawable?.setBounds(0, 0, bottomdrawable.getMinimumWidth(), bottomdrawable.getMinimumHeight());
        }



        tv.setCompoundDrawables(leftdrawable, topdrawable, rightdrawable, bottomdrawable)
    }


}
