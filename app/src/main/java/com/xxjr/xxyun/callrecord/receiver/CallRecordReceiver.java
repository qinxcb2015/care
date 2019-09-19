package com.xxjr.xxyun.callrecord.receiver;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.xxjr.xxkd.utils.ReadPhoneInfoUtil;
import com.xxjr.xxyun.app.MyApp;
import com.xxjr.xxyun.bean.CallRecordBean;
import com.xxjr.xxyun.callrecord.CallRecord;
import com.xxjr.xxyun.callrecord.helper.PrefsHelper;
import com.xxjr.xxyun.connstant.Conn;
import com.xxjr.xxyun.util.DESUtil;
import com.xxjr.xxyun.util.FileUtil;
import com.xxjr.xxyun.util.RandomCharData;
import com.xxjr.xxyun.util.SharedPrefUtil;
import com.xxjr.xxyun.utils.TextUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by aykutasil on 19.10.2016.
 */
public class CallRecordReceiver extends PhoneCallReceiver {


    private static final String TAG = CallRecordReceiver.class.getSimpleName();

    public static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    public static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    public static final String EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER";

    protected CallRecord callRecord;
    private MediaRecorder recorder;
    private File audiofile;
    private boolean isRecordStarted = false;
    private String encodeRules;
    private String suffix;
    private String mLocalTime;

    public CallRecordReceiver(CallRecord callRecord) {
        this.callRecord = callRecord;
    }

    @Override
    protected void onIncomingCallReceived(Context context, String number, Date start) {

    }

    @Override
    protected void onIncomingCallAnswered(Context context, String number, Date start) {
        startRecord(context, "incoming", number);
    }

    @Override
    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
        stopRecord(context, number);
    }

    @Override
    protected void onOutgoingCallStarted(Context context, String number, Date start) {
        startRecord(context, "outgoing", number);
    }

    @Override
    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {
        // todo 停止录音
        stopRecord(context, number);
    }

    @Override
    protected void onMissedCall(Context context, String number, Date start) {

    }

    // Derived classes could override these to respond to specific events of interest
    protected void onRecordingStarted(Context context, CallRecord callRecord, File audioFile) {
    }

    protected void onRecordingFinished(Context context, CallRecord callRecord, File audioFile) {
    }

    private void startRecord(Context context, String seed, String phoneNumber) {
        try {
            MyApp.Companion.setRecording(true);

            boolean isSaveFile = PrefsHelper.readPrefBool(context, CallRecord.PREF_SAVE_FILE);
            Log.i(TAG, "isSaveFile: " + isSaveFile);

            // dosya kayıt edilsin mi?
            if (!isSaveFile) {
                return;
            }

//            String file_name = PrefsHelper.readPrefString(context, CallRecord.PREF_FILE_NAME);
            String dir_path = PrefsHelper.readPrefString(context, CallRecord.PREF_DIR_PATH);
            String dir_name = PrefsHelper.readPrefString(context, CallRecord.PREF_DIR_NAME);
            boolean show_seed = PrefsHelper.readPrefBool(context, CallRecord.PREF_SHOW_SEED);
            boolean show_phone_number = PrefsHelper.readPrefBool(context, CallRecord.PREF_SHOW_PHONE_NUMBER);
            int output_format = PrefsHelper.readPrefInt(context, CallRecord.PREF_OUTPUT_FORMAT);
            int audio_source = PrefsHelper.readPrefInt(context, CallRecord.PREF_AUDIO_SOURCE);
            int audio_encoder = PrefsHelper.readPrefInt(context, CallRecord.PREF_AUDIO_ENCODER);
            File sampleDir = new File(dir_path + "/" + dir_name);
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }
            StringBuilder fileNameBuilder = new StringBuilder();
            phoneNumber = show_phone_number ? phoneNumber : "unkown";
            mLocalTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
            if (show_seed) {
                fileNameBuilder.append(MyApp.Companion.getMTel() + "_" + phoneNumber + "_" + mLocalTime);
            } else {
                fileNameBuilder.append(phoneNumber + "_" + MyApp.Companion.getMTel() + "_" + mLocalTime);
            }
            encodeRules = RandomCharData.createRandomCharData(10);
            String file_name = fileNameBuilder.toString().replace(" ", "");
            suffix = "";
            switch (output_format) {
                case MediaRecorder.OutputFormat.AMR_NB: {
                    suffix = ".mp3";
                    break;
                }
                case MediaRecorder.OutputFormat.AMR_WB: {
                    suffix = ".amr";
                    break;
                }
                case MediaRecorder.OutputFormat.MPEG_4: {
                    suffix = ".m4a";
                    break;
                }
                case MediaRecorder.OutputFormat.THREE_GPP: {
                    suffix = ".3gp";
                    break;
                }
                default: {
                    suffix = ".amr";
                    break;
                }
            }

            audiofile = new File(sampleDir, file_name + suffix);

            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
            }

            try {
                recorder = new MediaRecorder();
                recorder.setAudioSource(audio_source);
                recorder.setOutputFormat(output_format);
                recorder.setAudioEncoder(audio_encoder);
                recorder.setOutputFile(audiofile.getAbsolutePath());

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mediaRecorder, int i, int i1) {
                    Log.e("CallRecord", i + "");
                    Log.e("CallRecord", i1 + "");
                }
            });

            recorder.prepare();
            recorder.start();

            isRecordStarted = true;
            onRecordingStarted(context, callRecord, audiofile);

            Log.e(TAG, "record start");
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            boolean isDelete = audiofile.delete();
            Log.e(TAG, "record start 失败，删除" + isDelete);
            MyApp.Companion.setRecording(false);
            recorder = null;

            CallRecordBean callRecordBean = new CallRecordBean();
            callRecordBean.setRecordSuccess(false);
            EventBus.getDefault().post(callRecordBean);
        } catch (Exception ex) {
            Toast.makeText(context, "暂不支持该手机录音,请反馈给客服人员", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecord(Context context, String phoneNumber) {
        try {
            if (recorder != null && isRecordStarted) {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
                MyApp.Companion.setRecording(false);

                isRecordStarted = false;
                onRecordingFinished(context, callRecord, audiofile);
                Map<String, Object> telMap = ReadPhoneInfoUtil.INSTANCE.getCallTime(context, phoneNumber);
                String date = TextUtil.INSTANCE.getTextToString(telMap.get("date"));
                String number = TextUtil.INSTANCE.getTextToString(telMap.get("number"));
                int duration = TextUtil.INSTANCE.getInt(telMap.get("duration"), 0);

                if (duration > 0) {
                    String oldName = audiofile.getName();
                    String newPath = oldName.replace(suffix, "").replace(phoneNumber, number).replace(mLocalTime, date);

                    newPath = DESUtil.encode(encodeRules, newPath) + suffix;
                    newPath = Environment.getExternalStorageDirectory().getPath() + File.separator + Conn.INSTANCE.getRECORD_DIR() + File.separator + newPath;

                    //todo
                    FileUtil.renameFile(audiofile.getPath(), newPath);
                    audiofile = new File(newPath);
                    boolean fl = audiofile.exists();

                    CallRecordBean callRecordBean = new CallRecordBean();
                    callRecordBean.setRecordSuccess(true);
                    callRecordBean.setAudiofile(audiofile);
                    callRecordBean.setEncodeRules(encodeRules);
                    EventBus.getDefault().post(callRecordBean);
                } else {
                    audiofile.delete();
                }
                Log.i(TAG, "record stop");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
