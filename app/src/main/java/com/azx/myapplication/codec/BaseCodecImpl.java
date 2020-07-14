package com.azx.myapplication.codec;


import com.azx.myapplication.bean.CodecConfigureBean;
import com.azx.myapplication.utils.MyLog;

public class BaseCodecImpl implements BaseCodec {

    protected static final String TAG = "BaseCodecImpl";
    protected CodecLife codecLife;

    @Override
    public boolean open(CodecConfigureBean bean) {
        return codecLife.open(bean);
    }

    @Override
    public boolean restart(CodecConfigureBean bean) {
        return codecLife.restart(bean);
    }

    @Override
    public boolean pause() {
        return codecLife.pause();
    }

    @Override
    public boolean resume() {
        return codecLife.resume();
    }

    @Override
    public void release() {
        codecLife.release();
    }

    protected void log(String msg) {
        MyLog.lp(TAG, msg);
    }

    protected void logd(String msg) {
        MyLog.lpd(TAG, msg);
    }

    protected void logE(String msg) {
        MyLog.lpe(TAG, msg);
    }
}
