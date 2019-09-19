package com.xxjr.xxyun.util;

import android.content.Context;

/**
 * Created by wqg on 2018/1/24.
 */

public class TelAdapterUtil {

    /**
     * 佛山有个 DLI_AL10_864134034466708 型号无法使用高斯模糊
     * @param context
     * @return
     */
    public static boolean isDLI_AL10_864134034466708(Context context){
        if ("DLI_AL10".equals(SystemUtil.getSystemModel()) && "864134034466708".equals(SystemUtil.getIMEI(context))){
            return true;
        }
        return false;
    }
}
