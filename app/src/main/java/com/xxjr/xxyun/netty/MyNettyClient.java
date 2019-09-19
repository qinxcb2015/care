package com.xxjr.xxyun.netty;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.xxjr.xxyun.app.MyApp;
import com.xxjr.xxyun.connstant.Conn;
import com.xxjr.xxyun.connstant.Urls;
import com.xxjr.xxyun.utils.CommUtil;

import org.ddq.common.util.JsonUtil;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MyNettyClient {
    private int sendMsgTimes = 20;
    private Bootstrap bootstrap;//客户端
    private EventLoopGroup workLoopGroup;//工作组
    private MyNettyClientHandler nettyClientHandler;//客户端处理器
    public static Map<String, Object> sendMap = null;//待发送数据
    private ChannelFuture mChannelFuture = null;
    private Channel clientChannel = null;
    private volatile static MyNettyClient myNettyClient;//实例对象


    //获取单例实例
    public static MyNettyClient getInstance() {
        if (myNettyClient == null) {
            synchronized (MyNettyClient.class) {
                if (myNettyClient == null) {
                    myNettyClient = new MyNettyClient();
                }
            }
        }

        return myNettyClient;
    }

    /**
     * 释放资源
     */
    public void reaseResource() {
        try {
            if (nettyClientHandler != null && nettyClientHandler.getmContextSocket() != null) {
                nettyClientHandler.getmContextSocket().close().sync();
                nettyClientHandler.setmContextSocket(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommUtil.INSTANCE.Log("nettyclient = nettyClientHandler", "释放资源失败。。。。");
        }

        try {
            if (clientChannel != null) {
                clientChannel.close().sync();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommUtil.INSTANCE.Log("nettyclient = clientChannel", "释放资源失败。。。。");
        }

        try {
            if (workLoopGroup != null) {
                workLoopGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommUtil.INSTANCE.Log("nettyclient = workLoopGroup", "释放资源失败。。。。");
        }

    }

    private void initSocket() {
        reaseResource();//释放资源

        workLoopGroup = new NioEventLoopGroup();

        try {
            bootstrap = new Bootstrap();
            bootstrap.group(workLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);
            bootstrap.handler(new ChildChannelHandler());
        } catch (Exception e) {
            e.printStackTrace();
            if (workLoopGroup != null)
                workLoopGroup.shutdownGracefully();
        }
    }


    /***
     * 连接服务器
     */
    public Channel doConnect() throws InterruptedException {
        //初始化参数
        initSocket();

        //连接
        try {
            mChannelFuture = bootstrap.connect(MyApp.Companion.getNETTY_IP(), MyApp.Companion.getNETTY_PORT()).sync();
            clientChannel = mChannelFuture.channel();
            if (!mChannelFuture.isSuccess()) {
                System.out.println("----------客户端进行重新连接--------------");
                //连接不上则尝试重连
                mChannelFuture.channel().eventLoop().schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doConnect();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, 10, TimeUnit.SECONDS);
            }
            mChannelFuture.channel().closeFuture().sync();
            return clientChannel;

        } catch (Exception ex) {
            ex.printStackTrace();
            CommUtil.INSTANCE.Log("连接服务器失败", "ex2");
            Observable.timer(sendMsgTimes, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    doConnect();
                }
            });
        } finally {
            if (workLoopGroup != null) {
                try {
                    workLoopGroup.shutdownGracefully();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    CommUtil.INSTANCE.Log("连接服务器失败", ex.getMessage());
                }

            }
        }
        return clientChannel;
    }

    //内部类
    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            nettyClientHandler = new MyNettyClientHandler(sendMap);

            //设置编码及解码
            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("_$".getBytes())));
            ch.pipeline().addLast("decoder", new StringDecoder());
            ch.pipeline().addLast("encoder", new StringEncoder());

            //绑定心跳,对应事件userEventTriggered
            ch.pipeline().addLast("ping", new IdleStateHandler(0, 20, 0, TimeUnit.SECONDS));
            //绑定处理类
            ch.pipeline().addLast(nettyClientHandler);
        }

    }


    /***
     * 发送数据
     * @param sendMsg
     */
    public void sendData(Map<String, Object> sendMsg) {
        //判断当前是否连接，已连接则直接发送，未连接则先进行连接
        if (nettyClientHandler == null || nettyClientHandler.getmContextSocket() == null) {
            System.out.println("----------客户端进行连接---------");
            sendMsg.put("UUID", MyApp.Companion.getUUID() );
            sendMsg.put("signId", MyApp.Companion.getSignId());
            sendMap = sendMsg;
            try {
                clientChannel = MyNettyClient.getInstance().doConnect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            sendMsg.put("UUID", MyApp.Companion.getUUID() );
            sendMsg.put("signId", MyApp.Companion.getSignId());
            sendMap = sendMsg;
            String sendMsg1 = JsonUtil.getInstance().object2JSON(sendMap);
            clientChannel.writeAndFlush(sendMsg1 + "_$");
        }

    }
}
