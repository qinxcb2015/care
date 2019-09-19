package com.xxjr.xxyun.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wqg on 2017/11/22.
 */

public class TimeUtils {

    /**
     * 时间戳转换成日期格式字符串
     * @param seconds 精确到秒的字符串
     * @return
     */
    public static String timeStamp2Date(Long seconds) throws ParseException {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(seconds);
        return date.toString();
    }
    /**
     * 日期格式字符串转换成时间戳
     * @param date_str 字符串日期
     * @return
     */
    public static Long date2TimeStamp(String date_str) throws ParseException {
        //Date或者String转化为时间戳
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = format.parse(date_str);
        return date.getTime();
    }
}
