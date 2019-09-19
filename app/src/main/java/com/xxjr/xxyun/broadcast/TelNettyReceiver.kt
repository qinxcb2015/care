package com.xxjr.xxyun.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Created by wqg on 2018/1/12.
 */

class TelNettyReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {

        val number = intent.getStringExtra("tel")
        //用intent启动拨打电话
        val intent1 = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number))
        context.startActivity(intent1)



    }
}
