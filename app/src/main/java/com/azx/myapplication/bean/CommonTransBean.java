package com.azx.myapplication.bean;

public class CommonTransBean {

    public int eventType;
    public Object[] objs;
    public boolean mUrgentMsg;

    public CommonTransBean(int eventType, Object... objs) {
        this.eventType = eventType;
        this.objs = objs;
    }
}
