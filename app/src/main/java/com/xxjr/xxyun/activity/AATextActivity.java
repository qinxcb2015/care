package com.xxjr.xxyun.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.xxjr.xxyun.R;
import com.xxjr.xxyun.util.DESUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wqg on 2018/1/9.
 */

public class AATextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aa_layout);
    }

    public void onClick1(View view){
//        String a1 =  DESUtil.AESEncode("123456123","5465465468797968ASD5FAS6D5F4A6S5DFASDFASDFASDFA");
//        String aa = DESUtil.AESDncode("123456123",a1);
//        Log.e("解密",aa);
    }

    public void onClick2(View view) throws Exception {
        String a1 = DESUtil.encode("sadfasd653265sdfw","15298967709-138768654411_2017-8-4 51:52:10");
        String aa = DESUtil.decode("sadfasd653265sdfw",a1);
        Log.e("解密",aa);
    }

    public void onClick3(View view){
//        String a1 =  DESUtil.AESEncode("123456123","5465465468797968ASD5FAS6D5F4A6S5DFASDFASDFASDFA");
//        String aa = DESUtil.AESDncode("123456123",a1);
//        Log.e("解密",aa);
    }


}
