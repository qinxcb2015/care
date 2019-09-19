package com.xxjr.xxyun.bean;

import java.util.Map;

/**
 * Created by wqg on 2018/1/12.
 */

public class NettyBean {

    private Map<String,Object> rMap ;
    private boolean onLineFlag = true;

    public NettyBean(Map<String, Object> rMap) {
        this.rMap = rMap;
    }

    public NettyBean( boolean onLineFlag) {
        this.onLineFlag = onLineFlag;
    }

    public boolean isOnLineFlag() {
        return onLineFlag;
    }

    public void setOnLineFlag(boolean onLineFlag) {
        this.onLineFlag = onLineFlag;
    }

    public Map<String, Object> getrMap() {
        return rMap;
    }

    public void setrMap(Map<String, Object> rMap) {
        this.rMap = rMap;
    }
}
