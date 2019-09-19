package com.xxjr.xxyun.utils

import android.app.Activity
import kotlinx.android.synthetic.main.title_normal.*

/**
 * Created by admin on 2017/8/9.13:52
 */
object SetTitleBar {

    interface OnClickBackListener{
        fun onBack()
    }

    /**
     * 并且设置点击返回事件了
     * @param activity
     * *
     * @param title
     */
    fun setTitleText(activity: Activity, color: Int?, title: String, colorTitle: Int?,onClickBackListener : OnClickBackListener ?=null) {
        activity.title_view.layoutParams.height = MeasureUtil.appStatusBarHeight(activity)
        activity.title_tv.text = title
        activity.title_ll_finish.setOnClickListener {
            if (onClickBackListener != null){
                onClickBackListener.onBack()
            }else {
                activity.finish()
            }
        }
        if (color != null) {
            activity.title_ll.setBackgroundColor(color)
        }
        if (colorTitle != null) {
            activity.title_tv.setTextColor(colorTitle)
        }
    }

}