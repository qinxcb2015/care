package com.xxjr.xxyun.util

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.lzy.okgo.model.Response
import com.xxjr.xxyun.BuildConfig
import com.xxjr.xxyun.app.MyApp
import com.xxjr.xxyun.connstant.Conn
import com.xxjr.xxyun.connstant.Urls
import com.xxjr.xxyun.db.CallSQLiteOpenHelper
import com.xxjr.xxyun.db.MyContextWrapper
import com.xxjr.xxyun.utils.CommUtil
import com.xxjr.xxyun.utils.network.MyOkGo
import com.xxjr.xxyun.utils.network.callback.DialogCallback
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import java.io.File
import java.util.concurrent.TimeUnit
import com.umeng.analytics.pro.db


/**
 * Created by wqg on 2018/1/18.
 */

class UpRecordUtil private constructor() {

    companion object {
        private var instance: UpRecordUtil? = null
        private var tmpContext: Context? = null
        lateinit var mDbHelper: CallSQLiteOpenHelper
        lateinit var mDbContext: MyContextWrapper

        fun getInstance(context: Context): UpRecordUtil {
            if (instance == null || tmpContext != context) {
                synchronized(UpRecordUtil::class.java) {
                    if (instance == null || tmpContext != context) {
                        instance = UpRecordUtil()
                        mDbContext = MyContextWrapper(tmpContext, Conn.DB_DIR)
                        mDbHelper = CallSQLiteOpenHelper(mDbContext)
                    }
                }
            }
            tmpContext = context
//            spRecordd = SharedPrefUtil(tmpContext, Conn.SP_RECORD_NAME)
            return instance!!
        }
    }


    /**
     * 上传腾讯云
     */
    @Synchronized
    fun upTencentCloud(audioFile: File, isAllFileFlag: Boolean = false) {
        TencentCloud.getInstance(MyApp.mAppContext).singleFile(audioFile, object : TencentCloud.TencentCloudListener {
            override fun onSuccess(fileUrl: String?) {
                val sqldb = mDbHelper.getWritableDatabase()
                val cv = ContentValues()
                cv.put(CallSQLiteOpenHelper.MyCallTb.stepFlag, 1)
                cv.put(CallSQLiteOpenHelper.MyCallTb.TENCENT_URL, fileUrl)
                val res = sqldb.update(CallSQLiteOpenHelper.MyCallTb.tableName, cv, "${CallSQLiteOpenHelper.MyCallTb.FILENAME}=?", arrayOf(audioFile.name))
                upFileToServe(fileUrl, audioFile)
            }

            override fun onFail() {
                CommUtil.Log("上传腾讯云失败", "shibai")
                if (audioFile.length() == 0L) {
                    val isDeleteFlag = audioFile.delete()
                }
                if (isAllFileFlag) {
                    fileIndex++
                    upAllRecord()
                }
            }
        })
    }

    /**
     * 上传服务器
     */
    @Synchronized
    private fun upFileToServe(fileUrl: String?, file: File, isAllFileFlag: Boolean = false) {
        if (fileUrl.isNullOrEmpty()) return

        val columns = arrayOf(CallSQLiteOpenHelper.MyCallTb.RULEKEY) //需要查询的列
        val selection = "${CallSQLiteOpenHelper.MyCallTb.FILENAME} = ?" // 选择条件，给null查询所有
        val selectionArgs = arrayOf<String>(file.name)//选择条件参数,会把选择条件中的？替换成这个数组中的值
        var encodeRules = "未知"

        val sqldb = mDbHelper.getWritableDatabase()
        val cursor = sqldb.query(CallSQLiteOpenHelper.MyCallTb.tableName, columns, selection, selectionArgs, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {  //cursor不位空,可以移动到第一行
            encodeRules = cursor.getString(0)
        }
        val fileName = file.name.replace(".mp3", "")
        MyOkGo.postBusi(Urls.RECORD_TO_SERVE)
                ?.params("encodeRules", encodeRules)
                ?.params("url", fileUrl)
                ?.params("fileName", fileName)
                ?.execute(object : DialogCallback<Map<String, Any>>(MyApp.mAppContext, false) {
                    override fun onSuccessMap(rMap: Map<String, Any>) {
                        val isDelete = file.delete()
                        CommUtil.Log("是否删除", isDelete.toString())
                        val cv = ContentValues()
                        cv.put(CallSQLiteOpenHelper.MyCallTb.stepFlag, 2)
                        cv.put(CallSQLiteOpenHelper.MyCallTb.TENCENT_URL, fileUrl)
                        val res = sqldb.update(CallSQLiteOpenHelper.MyCallTb.tableName, cv, "${CallSQLiteOpenHelper.MyCallTb.FILENAME}=?", arrayOf("${fileName}.mp3"))

                        if (isAllFileFlag) {
                            upAllRecord()
                        }
                    }

                    override fun onError(response: Response<Map<String, Any>>) {
                        if (isAllFileFlag) {
                            fileIndex++
                            upAllRecord()
                            Toast.makeText(MyApp.mAppContext, "上传失败", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFinish() {
                        super.onFinish()
                        cursor.close()
                    }

                })
    }

    /**
     * 上传所有文件
     * todo 连续文件上传会导致内存溢出
     */
    var fileSize = 0L
    var fileCount = 0
    val timesSize = if (BuildConfig.DEBUG) 60 * 1000 * 1000 else 10 * 60 * 1000 * 1000
    val maxFileCount = 5
    var starFlag = true
    var fileIndex = 0


    @Synchronized
    fun upAllRecord() {
        if (!starFlag) return
        if (MyApp.isRecording) {
            resetUpServe()
            CommUtil.Log("上传all录音", "没有")
            return
        }
        Thread(Runnable {
            val allRecordFile = File(Environment.getExternalStorageDirectory().path + File.separator + Conn.RECORD_DIR)
            if (allRecordFile.isDirectory) {
                val files = allRecordFile.listFiles()
                if (files != null) {
                    if (fileIndex > files.size - 1) {
                        resetUpServe()
                        return@Runnable
                    }

                    val file = files[fileIndex]
//                    val cloudFileUrl = spRecordd.getString(Conn.RECORD_CLOUDURL_KEY + file.name, null)
                    val columns = arrayOf(CallSQLiteOpenHelper.MyCallTb.TENCENT_URL, CallSQLiteOpenHelper.MyCallTb.stepFlag, CallSQLiteOpenHelper.MyCallTb.FILENAME) //需要查询的列
                    val selection = "${CallSQLiteOpenHelper.MyCallTb.FILENAME}=?" // 选择条件，给null查询所有
                    val selectionArgs = arrayOf<String>(file.name)//选择条件参数,会把选择条件中的？替换成这个数组中的值
                    var cloudFileUrl: String? = null
                    var stepFlag: Int? = null

                    val sqldb = mDbHelper.getWritableDatabase()
                    val cursor = sqldb.query(CallSQLiteOpenHelper.MyCallTb.tableName, columns, selection, selectionArgs, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {  //cursor不位空,可以移动到第一行
                        cloudFileUrl = cursor.getString(0)
                        stepFlag = cursor.getInt(1)
                    }
                    cursor.close()

                    if (fileCount > maxFileCount) {
                        resetUpServe()
                        return@Runnable
                    } else if (fileCount == 0 && file.length() > timesSize) { // 第一个文件大于最大限制
                        // todo 上传一次停止
                        fileCount = maxFileCount

                        if (cloudFileUrl.isNullOrEmpty()) {
                            upTencentCloud(file, true)
                            return@Runnable
                        }
                        upFileToServe(cloudFileUrl, file, true)
                        return@Runnable
                    } else if (fileSize + file.length() > timesSize) {// 叠加文件如果大于限制
                        resetUpServe()
                        return@Runnable
                    }
                    fileCount++
                    fileSize += file.length()
                    CommUtil.Log("上传all录音", "fileCount=$fileCount  fileSize=$fileSize")
                    if (cloudFileUrl.isNullOrEmpty()) {
                        upTencentCloud(file, true)
                        return@Runnable
                    }
                    upFileToServe(cloudFileUrl, file, true)
                    return@Runnable
                }
            }
        }).start()
    }

    @Synchronized
    private fun resetUpServe() {
        fileSize = 0L
        fileCount = 0
        starFlag = false
        Observable.timer(60, TimeUnit.SECONDS).subscribe(object : Consumer<Long> {
            override fun accept(t: Long) {
                starFlag = true
            }
        })
    }
}