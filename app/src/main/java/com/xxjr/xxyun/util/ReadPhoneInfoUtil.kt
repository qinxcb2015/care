package com.xxjr.xxkd.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import com.xxjr.xxyun.app.MyApp
import com.xxjr.xxyun.connstant.Conn
import com.xxjr.xxyun.util.SharedPrefUtil
import org.ddq.common.util.JsonUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * Created by sty on 2017/9/15.
 */

object ReadPhoneInfoUtil {

    /**
     * 获取通话记录
     * @param context 上下文。通话记录需要从系统的【通话应用】中的内容提供者中获取，内容提供者需要上下文。通话记录保存在联系人数据库中：data/data/com.android.provider.contacts/databases/contacts2.db库中的calls表。
     * @return 包含所有通话记录的一个集合
     */
    @SuppressLint("WrongConstant", "MissingPermission")
    fun getCallInfos(context: Context): String {
        val resolver = context.contentResolver
        // uri的写法需要查看源码JB\packages\providers\ContactsProvider\AndroidManifest.xml中内容提供者的授权
        // 从清单文件可知该提供者是CallLogProvider，且通话记录相关操作被封装到了Calls类中
        val uri = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(CallLog.Calls.NUMBER, // 号码
                CallLog.Calls.DATE, // 日期
                CallLog.Calls.TYPE, // 类型：来电、去电、未接
                CallLog.Calls.CACHED_NAME, //姓名
                CallLog.Calls.DURATION  //通话时长

        )
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dNow = Date()
        val calendar = Calendar.getInstance() //得到日历
        calendar.time = dNow
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val dBefore =  calendar.time  //得到7天内的时间
        val dureTime: Long = if (MyApp.lastTime != -1L) MyApp.lastTime + 1000 else dBefore.time
        val cursor = resolver.query(uri, projection, "(type=? or type=?) and date > ?", arrayOf("1", "2", "$dureTime"), "date DESC")
        val telMap = LinkedHashMap<String, List<Map<String, Any>>>()
        while (cursor!!.moveToNext()) {
            val map = HashMap<String, Any>()
            map.put("number", cursor.getString(0))
            val date = Date(cursor.getLong(1))
            map.put("date", sdf.format(date))
            map.put("type", cursor.getInt(2))  // 1-来电   2-去电   3-未接   4-拒接
            map.put("name", cursor.getString(3))  // 姓名
            map.put("duration", cursor.getString(4))  // 通话时长
            if (telMap[cursor.getString(0)] == null) {
                val list = ArrayList<Map<String, Any>>()
                list.add(map)
                telMap.put(cursor.getString(0), list)
            } else {
                val mapList: ArrayList<Map<String, Any>> = telMap[cursor.getString(0)] as ArrayList<Map<String, Any>>
                mapList.add(map)
                telMap.put(cursor.getString(0), mapList)
            }
        }
        cursor.close()
        return if (telMap.isEmpty()) "" else JsonUtil.getInstance().object2JSON(telMap)
    }

    @SuppressLint("MissingPermission")
    fun getCallTime(context: Context, telNumeber:String?): Map<String,Any> {
        val resolver = context.contentResolver
        // uri的写法需要查看源码JB\packages\providers\ContactsProvider\AndroidManifest.xml中内容提供者的授权
        // 从清单文件可知该提供者是CallLogProvider，且通话记录相关操作被封装到了Calls类中
        val uri = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(CallLog.Calls.NUMBER, // 号码
                CallLog.Calls.DATE, // 日期
                CallLog.Calls.TYPE, // 类型：来电、去电、未接
                CallLog.Calls.CACHED_NAME, //姓名
                CallLog.Calls.DURATION  //通话时长

        )
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var cursor : Cursor ?= null
        if (telNumeber.isNullOrEmpty()) {
            cursor =  resolver.query(uri, projection, null, null, "date DESC")
        }else{
            cursor =  resolver.query(uri, projection, "number=?", arrayOf(telNumeber), "date DESC")
        }
        val telMap = LinkedHashMap<String, String>()
        while (cursor!!.moveToNext()) {
            telMap.put("number", cursor.getString(0).replace("+86",""))
            val date = Date(cursor.getLong(1))
            telMap.put("date", sdf.format(date))
            telMap.put("name", cursor.getString(3))  // 姓名
            telMap.put("duration", cursor.getString(4))  // 通话时长
            break
        }
        cursor.close()
        return telMap
    }

    /**
     * 存储最新的上传时间
     */
    fun spNewestTime(context: Context){
        val sp = SharedPrefUtil(context, Conn.SP_UP_CONTACTS)
        sp.putLong(Conn.SP_UP_CONTACTS_SYN_TIME, MyApp.synTime)
        sp.putLong(Conn.SP_UP_CONTACTS_LAST_TIME, MyApp.lastTime)
        sp.commit()
    }


}
