package com.xxjr.xxyun.util;


import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.common.Region;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.qcloud.core.network.QCloudProgressListener;
import com.tencent.qcloud.core.network.auth.LocalCredentialProvider;
import com.xxjr.xxyun.BuildConfig;
import com.xxjr.xxyun.connstant.Conn;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by wqg on 2018/1/17.
 */

public class TencentCloud {
    String appid = "10058268";
    String region = Region.AP_Shanghai.getRegion();

    String secretId = "AKID32rV2ZRIONE6sXeLk6JLDv35yhFgwAqP";
    String secretKey = "yrcmqmY8Zr24400teIpBQVUIwQip2CpP";
    long keyDuration = 600; //SecretKey 的有效时间，单位秒
    private Context context;
    private CosXmlServiceConfig serviceConfig;
    private CosXmlService cosXmlService;

    private static TencentCloud single=null;
    //静态工厂方法
    public static TencentCloud getInstance(Context context) {
        if (single == null) {
            single = new TencentCloud(context);
        }
        return single;
    }

    private TencentCloud() {}

    private TencentCloud(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        //创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
        serviceConfig = new CosXmlServiceConfig.Builder()
                .setAppidAndRegion(appid, region)
                .setDebuggable(true)
                .setConnectionTimeout(45000)
                .setSocketTimeout(30000)
                .build();

        //创建获取签名类
        LocalCredentialProvider localCredentialProvider = new LocalCredentialProvider(secretId, secretKey, keyDuration);
        cosXmlService = new CosXmlService(context.getApplicationContext(), this.serviceConfig, localCredentialProvider);
    }

    /**
     * 单文件上传
     * @param file
     * @param listener
     */
    public void singleFile(final File file, final TencentCloudListener listener) {
        String day = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
        if (BuildConfig.DEBUG){
            day = "a-local-test";
        }
        String bucket = "head"; // cos v5 的 bucket格式为：xxx-appid, 如 test-1253960454
        String cosPath = "/wav3gp/"+ day +"/" + file.getName(); //格式如 cosPath = "/test.txt";
        String srcPath = Environment.getExternalStorageDirectory().getPath() + "/"+ Conn.INSTANCE.getRECORD_DIR()+"/" + file.getName();
        long signDuration = 60*60; //签名的有效期，单位为秒

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, cosPath, srcPath);

        putObjectRequest.setSign(signDuration, null, null);

        /*设置进度显示
          实现 QCloudProgressListener.onProgress(long progress, long max)方法，
          progress 已上传的大小， max 表示文件的总大小
        */
        putObjectRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                float result = (float) (progress * 100.0 / max);
                Log.e("TEST", "progress =" + (long) result + "%");
            }
        });

        //使用异步回调上传：sdk 为对象存储各项服务提供异步回调操作方法
        cosXmlService.putObjectAsync(putObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                String fileUrl =result.accessUrl.replace("http://head-10058268.cos.ap-shanghai.myqcloud.com","https://static.xxjr.com");
                if (listener != null) listener.onSuccess(fileUrl);
//                file.delete();
                Log.e("TEST", "success =" + result.accessUrl);
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException clientException, CosXmlServiceException serviceException) {
                if (listener != null) listener.onFail();
                String errorMsg = clientException != null ? clientException.toString() : serviceException.toString();
                Log.e("TEST", errorMsg);
            }
        });
    }

    public interface TencentCloudListener{
        void onSuccess(String fileUrl);
        void onFail();

    }
}
