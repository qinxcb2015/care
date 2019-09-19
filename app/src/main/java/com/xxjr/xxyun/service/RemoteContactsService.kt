package com.xxjr.xxyun.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.lzy.okgo.model.Response
import com.xxjr.xxkd.utils.ReadPhoneInfoUtil
import com.xxjr.xxyun.IMyAidlInterface
import com.xxjr.xxyun.activity.MainActivity
import com.xxjr.xxyun.app.MyApp
import com.xxjr.xxyun.bean.ContactsSeviceEvent
import com.xxjr.xxyun.connstant.Urls
import com.xxjr.xxyun.util.CommMath
import com.xxjr.xxyun.util.TimeUtils
import com.xxjr.xxyun.util.UpRecordUtil
import com.xxjr.xxyun.utils.CommUtil
import com.xxjr.xxyun.utils.TextUtil
import com.xxjr.xxyun.utils.network.MyOkGo
import com.xxjr.xxyun.utils.network.callback.DialogCallback
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class RemoteContactsService : Service() {

    private lateinit var conn: MyConn
    private lateinit var binder: MyBinder
    private var errorCount = 0
    val handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg!!.what) {
                MainActivity.UP_AOTU_CONTACTS_FLAG -> {
                    upContacts()
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        conn = MyConn()
        binder = MyBinder()
        Log.e("RemoteContactsService", "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("RemoteContactsService", "onStartCommand")
        Toast.makeText(this, " 远程服务started", Toast.LENGTH_SHORT).show()
        upContactsHandler()
        this.bindService(Intent(this, LocalContactsService::class.java), conn, Context.BIND_IMPORTANT)
        return Service.START_STICKY
    }

    private inner class MyBinder : IMyAidlInterface.Stub() {
        override fun getServiceName(): String {
            Log.e("RemoteContactsService", "IMyAidlInterface  getServiceName")
            return RemoteContactsService::class.java.simpleName
        }
    }

    private inner class MyConn : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.e("RemoteContactsService", "MyConn  onServiceConnected")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.e("RemoteContactsService", "MyConn  onServiceDisconnected")
            Toast.makeText(this@RemoteContactsService, "本地服务killed", Toast.LENGTH_SHORT).show()

            //开启本地服务
            this@RemoteContactsService.startService(Intent(this@RemoteContactsService, LocalContactsService::class.java))
            //绑定本地服务
            this@RemoteContactsService.bindService(Intent(this@RemoteContactsService, LocalContactsService::class.java), conn, Context.BIND_IMPORTANT)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        removeCallback()
        Log.e("RemoteContactsService", " onDestroy")
        if (CommMath.isLogin()) {
            //开启本地服务
            this@RemoteContactsService.startService(Intent(this@RemoteContactsService, LocalContactsService::class.java))
            //绑定本地服务
            this@RemoteContactsService.bindService(Intent(this@RemoteContactsService, LocalContactsService::class.java), conn, Context.BIND_IMPORTANT)
        }

    }

    /**
     * 上传通讯录
     */
    fun upContacts() {
        UpRecordUtil.getInstance(this@RemoteContactsService).upAllRecord()
        Observable.create<String> { e ->
            e.onNext(ReadPhoneInfoUtil.getCallInfos(this))
            e.onComplete()
        }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String> {
                    override fun onError(e: Throwable) {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onComplete() {
                    }

                    override fun onNext(contactsStr: String) {
                        CommUtil.Log("contacts->service ", "remote->通讯录内容")
                        if (contactsStr.isEmpty()) {
                            upContactsHandler()
                        } else if (MyApp.isUpCall) {
                            upContactsHandler()
                            return
                        } else {
                            MyOkGo.postBusi(Urls.UP_CONTACTS)
                                    ?.params("callRecordJson", contactsStr.replace("+86", ""))
                                    ?.execute(object : DialogCallback<Map<String, Any>>(this@RemoteContactsService, false) {
                                        override fun onSuccessMap(rMap: Map<String, Any>) {
                                            val attrMap: Map<String, Any> = rMap["attr"] as Map<String, Any>
                                            val curUpLoadTime = TextUtil.getTextToString(attrMap["curUpLoadTime"])
                                            val firstStartCallTime = TextUtil.getTextToString(attrMap["firstStartCallTime"])
                                            if (!TextUtils.isEmpty(curUpLoadTime)) {
                                                MyApp.synTime = TimeUtils.date2TimeStamp(curUpLoadTime)
                                                MyApp.lastTime = if (firstStartCallTime.isEmpty()) -1L else TimeUtils.date2TimeStamp(firstStartCallTime)
                                                ReadPhoneInfoUtil.spNewestTime(this@RemoteContactsService)
                                                EventBus.getDefault().post(ContactsSeviceEvent(curUpLoadTime))
                                            }
                                            errorCount = 0
                                        }

                                        override fun onError(response: Response<Map<String, Any>>) {
                                            errorCount++
                                        }

                                        override fun onFinish() {
                                            super.onFinish()
                                            if (errorCount == 10) {
                                                removeCallback()
                                            } else {
                                                upContactsHandler()
                                            }
                                            MyApp.setUpCalling()
                                        }
                                    })
                        }
                    }

                })
    }

    fun upContactsHandler() {
        removeCallback()
        handler.sendEmptyMessageDelayed(MainActivity.UP_AOTU_CONTACTS_FLAG, MyApp.timing)
    }

    fun removeCallback() {
        handler.removeMessages(MainActivity.UP_AOTU_CONTACTS_FLAG)
    }
}
