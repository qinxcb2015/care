package com.xxjr.xxyun.netty;

import android.os.Handler;
import android.os.Message;

import com.xxjr.xxyun.app.MyApp;
import com.xxjr.xxyun.bean.NettyBean;
import com.xxjr.xxyun.util.CommMath;
import com.xxjr.xxyun.utils.CommUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.ddq.common.util.JsonUtil;
import org.greenrobot.eventbus.EventBus;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class MyNettyClientHandler extends ChannelHandlerAdapter {

    private ChannelHandlerContext mContextSocket;
    private Map<String, Object> sendMap;//发送消息
    private int sendMsgTimes = 20;


    public MyNettyClientHandler(Map<String, Object> sendMap) {
        this.sendMap = sendMap;
    }


    //活跃状态 建立
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        mContextSocket = ctx;//获取连接处理对象
        if (sendMap != null) {
            String sendMsg = JsonUtil.getInstance().object2JSON(sendMap);
            ctx.writeAndFlush(sendMsg + "_$");
            MyNettyClient.sendMap = null;
        }
        super.channelActive(ctx);
    }

    //接收服务端数据
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        CommUtil.INSTANCE.Log("channelRead 客户端接收数据", msg.toString());
        if (!msg.toString().isEmpty()) {
            try {
                Map<String, Object> mMap = JsonUtil.getInstance().json2Object(msg.toString(), LinkedHashMap.class);
                EventBus.getDefault().post(new NettyBean(mMap));
            } catch (Exception ex) {
                CommUtil.INSTANCE.Log("channelRead", "解析异常");
            }

        }
    }


    //发送心跳
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE: //读取空闲
                    super.userEventTriggered(ctx, evt);
                    break;
                case WRITER_IDLE: //写空闲
                    if (ctx.isRemoved() || !ctx.channel().isActive()) {
                        MyNettyClient.getInstance().doConnect();//进行重连
                    } else {//发送心跳包
                        CommUtil.INSTANCE.Log("userEventTriggered", "发送心跳");
                        sendHead();
                    }
                    break;
                case ALL_IDLE:
                    super.userEventTriggered(ctx, evt);
                    break;

                default:
                    break;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        CommUtil.INSTANCE.Log("channelInactive", "不活跃");
        super.channelInactive(ctx);
        MyNettyClient.getInstance().reaseResource();
        EventBus.getDefault().post(new NettyBean(false));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        CommUtil.INSTANCE.Log("handlerRemoved", "客户端被移除");
        super.handlerRemoved(ctx);
        if (CommMath.INSTANCE.isLogin()) {
            Observable.timer(sendMsgTimes, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    sendHead();
                }
            });
        }
    }

    public ChannelHandlerContext getmContextSocket() {
        return mContextSocket;
    }

    public void setmContextSocket(ChannelHandlerContext mContextSocket) {
        this.mContextSocket = mContextSocket;
    }

    public void sendHead() {
        Map<String, Object> msgMap = new HashMap();
        msgMap.put("cmdName", CmdEnum.CMD_0001.getCmdName());
        MyNettyClient.getInstance().sendData(msgMap);
    }
}
