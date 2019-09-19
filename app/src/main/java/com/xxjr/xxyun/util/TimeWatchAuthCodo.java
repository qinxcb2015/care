package com.xxjr.xxyun.util;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Button;

import com.xxjr.xxyun.R;


/**
 * Created by sty on 2016/8/1.
 * 短信验证码定时器
 */
public class TimeWatchAuthCodo extends CountDownTimer {
    private Context context;
    private Button yzmBtn;

    public TimeWatchAuthCodo(long millisInFuture, long countDownInterval, Context context, Button yzmBtn) {
        super(millisInFuture, countDownInterval);
        this.context = context;
        this.yzmBtn = yzmBtn;
    }

    @Override
    public void onFinish() {// 计时完毕
        yzmBtn.setText(context.getResources().getString(R.string.time_get_yanzheng));
        yzmBtn.setClickable(true);
    }

    @Override
    public void onTick(long millisUntilFinished) {// 计时过程
        yzmBtn.setClickable(false);//防止重复点击
        yzmBtn.setText(millisUntilFinished / 1000 + "s");
    }
}
