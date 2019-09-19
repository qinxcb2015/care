package com.xxjr.xxyun.bean;

import java.io.File;

/**
 * Created by wqg on 2018/1/17.
 */

public class CallRecordBean {

    /**
     * 录音成功 失败
     */
    private boolean recordSuccess;

    /**
     * 加密规则
     */
    private String encodeRules;
    /**
     * 录音文件
     */
    private File audiofile;


    public boolean isRecordSuccess() {
        return recordSuccess;
    }

    public void setRecordSuccess(boolean recordSuccess) {
        this.recordSuccess = recordSuccess;
    }

    public File getAudiofile() {
        return audiofile;
    }

    public void setAudiofile(File audiofile) {
        this.audiofile = audiofile;
    }

    public String getEncodeRules() {
        return encodeRules;
    }

    public void setEncodeRules(String encodeRules) {
        this.encodeRules = encodeRules;
    }
}
