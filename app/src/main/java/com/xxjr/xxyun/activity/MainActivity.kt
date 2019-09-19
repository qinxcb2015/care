package com.xxjr.xxyun.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton
import com.fangxu.allangleexpandablebutton.ButtonData
import com.fangxu.allangleexpandablebutton.ButtonEventListener
import com.lzy.okgo.model.Response
import com.tbruyelle.rxpermissions2.RxPermissions
import com.xxjr.animallibrary.effects.Effectstype
import com.xxjr.xxkd.utils.ReadPhoneInfoUtil
import com.xxjr.xxyun.R
import com.xxjr.xxyun.app.MyApp
import com.xxjr.xxyun.bean.CallRecordBean
import com.xxjr.xxyun.bean.ContactsSeviceEvent
import com.xxjr.xxyun.bean.NettyBean
import com.xxjr.xxyun.callrecord.CallRecord
import com.xxjr.xxyun.connstant.Conn
import com.xxjr.xxyun.connstant.Urls
import com.xxjr.xxyun.db.CallSQLiteOpenHelper
import com.xxjr.xxyun.db.MyContextWrapper
import com.xxjr.xxyun.netty.CmdEnum
import com.xxjr.xxyun.netty.MyNettyClient
import com.xxjr.xxyun.service.LocalContactsService
import com.xxjr.xxyun.util.*
import com.xxjr.xxyun.utils.CommUtil
import com.xxjr.xxyun.utils.MeasureUtil
import com.xxjr.xxyun.utils.TextUtil
import com.xxjr.xxyun.utils.network.MyOkGo
import com.xxjr.xxyun.utils.network.callback.DialogCallback
import com.xxjr.xxyun.widge.dialog.AnimalDialog
import com.xxjr.xxyun.widge.dialog.ContentViewDialog
import com.xxjr.xxyun.widge.dialog.LoadingDialog
import com.xxjr.xxyun.widge.glideTransfrom.GlideCircleImageview
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : BaseActivity() {
    lateinit var callRecord: CallRecord
    var count: Int = 0
    var grantArray = arrayOf(0, 0, 0, 0, 0, 0)
    lateinit var mDbHelper: CallSQLiteOpenHelper
    lateinit var mDbContext: MyContextWrapper

    companion object {
        val UP_AOTU_CONTACTS_FLAG = 0x888
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_main)
        mDbContext = MyContextWrapper(this, Conn.DB_DIR)
        mDbHelper = CallSQLiteOpenHelper(mDbContext)

        initView()
        initListener()
        openContactsServer()
        initCallRecord()
    }

    /**
     * 接收netty信息
     */
    @SuppressLint("MissingPermission")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun accectNettyMsg(nettyBean: NettyBean) {
        if (nettyBean.isOnLineFlag) {
            main_btn_netty_status.text = "在线"
            main_ll_netty_outline.visibility = View.GONE
            val msgMap = nettyBean.getrMap()
            val cmdName = TextUtil.getString(msgMap["cmdName"], "0001")
            val telNumber = TextUtil.getString(msgMap["telephone"], "0000")
            if (cmdName == "0004") {
                val cmdMap: MutableMap<String, Any> = HashMap()
                cmdMap.put("cmdName", "0004")
                cmdMap.put("success", "true")
                MyNettyClient.getInstance().sendData(cmdMap)
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telNumber))
                startActivity(intent)
            }
        } else {
            main_btn_netty_status.text = "离线"
            main_ll_netty_outline.visibility = View.VISIBLE
        }
    }

    /**
     * 接收录音状态
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun receiveRecord(recordBean: CallRecordBean) {
        if (recordBean.isRecordSuccess) {
            val sqldb = mDbHelper.getWritableDatabase()
            val cv = ContentValues()
            cv.put(CallSQLiteOpenHelper.MyCallTb.NAME, TextUtil.getTextToString(MyApp.userInfo?.get("realName")))
            cv.put(CallSQLiteOpenHelper.MyCallTb.FILENAME, recordBean.audiofile.name)
            cv.put(CallSQLiteOpenHelper.MyCallTb.RULEKEY, recordBean.encodeRules)
            cv.put(CallSQLiteOpenHelper.MyCallTb.stepFlag, 0)
            sqldb.insert(CallSQLiteOpenHelper.MyCallTb.tableName, null, cv)
            UpRecordUtil.getInstance(this).upTencentCloud(recordBean.audiofile)
        }
    }


    override fun onResume() {
        super.onResume()
        initData()
        if (CommMath.isLogin(this)) {
            if (MyApp.NETTY_IP.isNullOrEmpty()) {
                getIp()
            } else {
                openNettyClient()
            }
            callRecord.startCallReceiver()
        } else {
            callRecord.stopCallReceiver()
        }
    }

    override fun onStart() {
        super.onStart()
        checkPemission()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        closeContactsServer()
        callRecord.stopCallReceiver()
    }

    /**
     * 校验权限
     */
    private fun checkPemission() {
        count = 0
        RxPermissions(this).requestEach(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO)
                .subscribe { permission ->
                    if (permission.granted) {
                        grantArray[count] = 0
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        grantArray[count] = 1
                    } else {
                        grantArray[count] = 2
                    }
                    count++
                    if (count == grantArray.size) {
                        grantArray.forEach {
                            if (it == 1) {
                                refuse()
                                return@subscribe
                            }
                        }
                        grantArray.forEach {
                            if (it == 2) {
                                ContentViewDialog.refusePermissionForever(this@MainActivity, getString(R.string.setting_grant), null)
                                return@subscribe
                            }
                        }
                        CommMath.getDeviceId(this@MainActivity)
                        getOnLineNewestTime()
                    }
                }
    }

    /**
     * 拒绝之后的对话框
     */
    private fun refuse() {
        var dialogView: View = ContentViewDialog
                .setMyDialog2Listener(object : ContentViewDialog.MyDialog2Listener {
                    override fun left() {
                        AnimalDialog.getInstance(this@MainActivity).dismissDialog()
                        finish()
                    }

                    override fun right() {
                        AnimalDialog.getInstance(this@MainActivity).dismissDialog()
                        checkPemission()
                    }
                })
                .notice2(this, getString(R.string.init_grantTitle), getString(R.string.init_grantContent),
                        getString(R.string.cancle), getString(R.string.sure))
        AnimalDialog.getInstance(this@MainActivity)
                .showDialog()
                .setDialogView(dialogView)
                .withEffect(Effectstype.Fadein)
                .setCanceledOnTouchOutside(false)
                .setCancelable(false)
    }

    /**
     * 开启通话服务
     */
    private fun openContactsServer() {
        val mLocalService = Intent(this, LocalContactsService::class.java)
        startService(mLocalService)
    }

    /**
     * 关闭通话服务
     */
    private fun closeContactsServer() {
        val stopIntent = Intent(this, LocalContactsService::class.java)
        stopService(stopIntent)
    }

    private fun getIp(isShowDialog: Boolean = false) {
        MyOkGo.postCust(Urls.GET_NETTY_IP)
                ?.execute(object : DialogCallback<Map<String, Any>>(this, isShowDialog) {
                    override fun onSuccessMap(rMap: Map<String, Any>) {
                        val attrMap: Map<String, Any> = rMap.get("attr") as Map<String, Any>
                        val mIp: String? = attrMap["ip"] as String?
                        val port = TextUtil.getInt(attrMap["port"], -1)
                        MyApp.NETTY_IP = mIp
                        MyApp.NETTY_PORT = port
                        openNettyClient()
                    }

                    override fun onFinish() {
                        super.onFinish()
                        if (MyApp.NETTY_IP.isNullOrEmpty()) {
                            Observable.timer(20L, TimeUnit.SECONDS).subscribe(object : Consumer<Long> {
                                override fun accept(t: Long) {
                                    getIp()
                                }
                            })
                        }
                    }
                })
    }

    /**
     * 开启长连接相关
     */
    private fun openNettyClient() {
        Thread(Runnable {
            val msgMap: MutableMap<String, Any> = HashMap()
            msgMap.put("cmdName", CmdEnum.CMD_0001.cmdName)
            MyNettyClient.getInstance().sendData(msgMap)

        }).start()
    }

    /**
     * 初始化录音
     */
    private fun initCallRecord() {
        val sysModel = SystemUtil.getSystemModel()
        var sourceType = MediaRecorder.AudioSource.VOICE_COMMUNICATION
        if (Conn.TEL_MODEL_MHA_AL00 == sysModel) {//REMOTE_SUBMIX 能收到较小的声音
            sourceType = MediaRecorder.AudioSource.VOICE_RECOGNITION
        } else if (sysModel.contains("Redmi 4A")){
            sourceType = MediaRecorder.AudioSource.VOICE_RECOGNITION
        }
        else if (sysModel.contains("Redmi")) {
            sourceType = MediaRecorder.AudioSource.VOICE_CALL
        }
        callRecord = CallRecord.Builder(this)
                .setRecordFileName("${MyApp.userInfo?.get("telephone")}")
                .setRecordDirName(Conn.RECORD_DIR)
                .setRecordDirPath(Environment.getExternalStorageDirectory().path)
                .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                .setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                .setAudioSource(sourceType)
                .setShowSeed(true)
                .build()
        callRecord.startCallReceiver()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false)
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initView() {
        val h2 = MeasureUtil.getHeight(main_eBtn_upContacts)
        val h3 = MeasureUtil.rotiaDesignHeight(this, main_iv_beij, null)
        val lp1 = RelativeLayout.LayoutParams(MyApp.screenWidth, h3)
        main_iv_beij.layoutParams = lp1
        main_iv_beij.scaleType = ImageView.ScaleType.FIT_XY
        val lp2 = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        lp2.topMargin = h3 - h2 / 2
        lp2.addRule(RelativeLayout.CENTER_HORIZONTAL)
        main_eBtn_upContacts.layoutParams = lp2
        main_eBtn_upContacts.setBurBackground(false)
        installButton90to180()
    }

    private fun initData() {
        main_tv_name.text = TextUtil.getString(MyApp.userInfo?.get("realName"), "未登录")
        main_tv_tel.text = TextUtil.getString(MyApp.userInfo?.get("hideTelephone"), "")
        if (MyApp.synTime != -1L) {
            main_tv_time.text = TimeUtils.timeStamp2Date(MyApp.synTime)
        }
        Glide.with(this)
                .load(TextUtil.getTextToString(MyApp.userInfo?.get("userImage")))
                .transform(GlideCircleImageview(this))
                .placeholder(R.mipmap.toux)
                .into(main_iv_head)


    }

    private fun installButton90to180() {
        val buttonDatas = ArrayList<ButtonData>()
        val drawable = intArrayOf(R.mipmap.jia, R.mipmap.saom, R.mipmap.thjl/*, R.mipmap.thly*/)
        for (i in 0..2) {
            val buttonData: ButtonData
            if (i == 0) {
                buttonData = ButtonData.buildIconButton(this, drawable[i], 0f)
            } else {
                buttonData = ButtonData.buildIconButton(this, drawable[i], 0f)
            }
            buttonDatas.add(buttonData)
        }
        main_eBtn_upContacts.buttonDatas = buttonDatas
        telAdapter()
        expandListener(main_eBtn_upContacts)
    }

    /**
     * DLI_AL10_864134034466708
     * 针对这个型号的适配
     */
    private fun telAdapter() {
        if (TelAdapterUtil.isDLI_AL10_864134034466708(this)) {
            main_eBtn_upContacts.setBurBackground(false)
        }
    }

    private fun expandListener(button: AllAngleExpandableButton) {
        button.setButtonEventListener(object : ButtonEventListener {
            override fun onButtonClicked(index: Int) {
                if (CommMath.isLogin(this@MainActivity)) {
                    when (index) {
                        1 -> {
                            startActivity(Intent(this@MainActivity, QRCodeActivity::class.java))
                        }
                        2 -> {
                            upContacts()
                        }
                    }
                }


            }

            override fun onExpand() {
            }

            override fun onCollapse() {
            }
        })
    }

    fun initListener() {
        main_ll_info.setOnClickListener {
            CommMath.isLogin(this)
        }
        main_ll_set.setOnClickListener {
            if (CommMath.isLogin(this)) {
                startActivity(Intent(this, SettingActivity::class.java))
            }
        }
        // 重新netty建立
        main_ll_netty_outline.setOnClickListener {
            if (CommMath.isLogin(this)) {
                if (MyApp.NETTY_IP.isNullOrEmpty()) {
                    getIp()
                } else {
                    LoadingDialog.getInstance(this).createLoadingDialog("正在重连")
                    openNettyClient()
                    Observable.timer(5L, TimeUnit.SECONDS).subscribe(object : Consumer<Long> {
                        override fun accept(t: Long) {
                            LoadingDialog.getInstance(this@MainActivity).closeDialog()
                        }
                    })
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    private fun updataTime(event: ContactsSeviceEvent) {
        main_tv_time.text = event.updateTime
    }

    /**
     * 获取线上上传的最新时间
     */
    private fun getOnLineNewestTime() {
        MyOkGo.postBusi(Urls.GET_NEWEST_TIME)
                ?.execute(object : DialogCallback<Map<String, Any>>(this, false) {
                    override fun onSuccessMap(rMap: Map<String, Any>) {
                        val attrMap: Map<String, Any> = rMap["attr"] as Map<String, Any>
                        val curUpLoadTime = TextUtil.getTextToString(attrMap["curUpLoadTime"])
                        val firstStartCallTime = TextUtil.getTextToString(attrMap["firstStartCallTime"])
                        if (!TextUtils.isEmpty(curUpLoadTime)) {
                            MyApp.synTime = TimeUtils.date2TimeStamp(curUpLoadTime)
                            MyApp.lastTime = if (firstStartCallTime.isNullOrEmpty()) -1L else TimeUtils.date2TimeStamp(firstStartCallTime)
                            ReadPhoneInfoUtil.spNewestTime(this@MainActivity)
                            main_tv_time.text = curUpLoadTime
                        } else {
                            main_tv_time.text = "未知"
                        }
                    }

                    override fun onError(response: Response<Map<String, Any>>) {
                        main_tv_time.text = "未知"
                    }
                })
    }

    /**
     * 上传通讯录
     */
    fun upContacts() {
        UpRecordUtil.getInstance(this@MainActivity).upAllRecord()
        LoadingDialog.getInstance(this).createLoadingDialog("同步中")
        Observable.create<String> { e ->
            e.onNext(ReadPhoneInfoUtil.getCallInfos(this))
            e.onComplete()
        }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String> {
                    override fun onError(e: Throwable) {
                        CommUtil.ToastShow(this@MainActivity, getString(R.string.notice_error_readContacts))
                        LoadingDialog.getInstance(this@MainActivity).closeDialog()
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onComplete() {
                    }

                    override fun onNext(contactsStr: String) {
                        CommUtil.Log("contacts->", contactsStr)
                        var str = contactsStr.replace("+86","")
                        if (contactsStr.isEmpty()) {
                            CommUtil.ToastShow(this@MainActivity, getString(R.string.notice_no_contactsUp))
                            LoadingDialog.getInstance(this@MainActivity).closeDialog()
                            return
                        }
                        CommUtil.Log("是否正录音",MyApp.isUpCall.toString())
                        if (MyApp.isUpCall) {
                            MyApp.isUpCall = false
                            LoadingDialog.getInstance(this@MainActivity).closeDialog()
                            return
                        }
                        MyOkGo.postBusi(Urls.UP_CONTACTS)
                                ?.params("callRecordJson", contactsStr.replace("+86",""))
                                ?.execute(object : DialogCallback<Map<String, Any>>(this@MainActivity, false) {
                                    override fun onSuccessMap(rMap: Map<String, Any>) {
                                        val attrMap: Map<String, Any> = rMap["attr"] as Map<String, Any>
                                        val curUpLoadTime = TextUtil.getTextToString(attrMap["curUpLoadTime"])
                                        val firstStartCallTime = TextUtil.getTextToString(attrMap["firstStartCallTime"])
                                        if (!TextUtils.isEmpty(curUpLoadTime)) {
                                            MyApp.synTime = TimeUtils.date2TimeStamp(curUpLoadTime)
                                            MyApp.lastTime = if (firstStartCallTime.isEmpty()) -1L else TimeUtils.date2TimeStamp(firstStartCallTime)
                                            main_tv_time.text = curUpLoadTime
                                            ReadPhoneInfoUtil.spNewestTime(this@MainActivity)

                                        }

                                    }

                                    override fun onFinish() {
                                        super.onFinish()
                                        LoadingDialog.getInstance(this@MainActivity).closeDialog()
                                        MyApp.setUpCalling()
                                    }
                                })
                    }

                })
    }

}
