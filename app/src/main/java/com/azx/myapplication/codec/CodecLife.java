package com.azx.myapplication.codec;


import com.azx.myapplication.bean.CodecConfigureBean;
import com.azx.myapplication.utils.MyLog;

public class CodecLife implements BaseCodec {

    private String tag;
    private OnCodecLifeListener onCodecLifeListener;

    private volatile boolean codecOpened;
    private volatile boolean sizeConfiged;
    private volatile boolean codecDestory;
    private volatile boolean paused;

    private final Object lock = new Object();

    private int width, height;

    public CodecLife(String tag, OnCodecLifeListener onCodecLifeListener) {
        this.onCodecLifeListener = onCodecLifeListener;
        this.tag = tag;
    }

    public int getCodecWidth() {
        return width;
    }

    public int getCodecHeight() {
        return height;
    }

    public Object getLock() {
        return lock;
    }

    public boolean isCodecOpened() {
        return codecOpened;
    }

    public boolean isPaused() {
        return paused;
    }

    private int tryOpenCodec() {
        CodecConfigureBean bean;
        synchronized (lock) {
            boolean startCheck = startCheck();
            if (!startCheck) {
                return -1;
            }

            boolean openCheck = onCodecLifeListener.onSyncCodecStartCheck();
            if (!openCheck) {
                return -2;
            }
            bean = onCodecLifeListener.onSyncCodecStartConfigure(width, height);
        }

        CodecConfigureBean codecConfigureBean = onCodecLifeListener.onCodecConfiguring(bean);
        if (codecConfigureBean == null) {
            return -3;
        }

        synchronized (lock) {
            if (codecDestory) {
                onCodecLifeListener.onSyncCodecReleasing(codecConfigureBean);
                return 0;
            }

            boolean start = onCodecLifeListener.onSyncCodecStart(codecConfigureBean);
            if (!start) {
                onCodecLifeListener.onSyncCodecReleasing(codecConfigureBean);
                return -4;
            }
            onCodecLifeListener.onSyncCodecAssignment(codecConfigureBean);
        }
        onCodecLifeListener.onCodecStartFinish(codecConfigureBean);
        return 0;
    }

    private boolean startCheck() {
        if (!codecOpened) { // 外部还没调用open接口打开编码器
            logE("config check failed! not open!");
            return false;
        }

        if (!sizeConfiged) { // 编码器参数都未发生改变或还没设置，无法或没必要config
            logE("config check failed! size or params not setting or changed!");
            return false;
        }

        if (codecDestory) { // 编码器已销毁，不执行任何操作
            logE("config check failed! already destory!");
            return false;
        }
        return true;
    }

    @Override
    public boolean open(CodecConfigureBean bean) {
        synchronized (lock) {
            if (codecOpened) {
                return true;
            }
            codecOpened = true;
            if (width != bean.width || height != bean.height) {
                width = bean.width;
                height = bean.height;
                log("Recv video size : " + width + " * " + height);
                sizeConfiged = true;
            }
        }

        int result = tryOpenCodec();
        log("Start encodec result! " + result);
        return result == 0;
    }

    @Override
    public boolean restart(CodecConfigureBean bean) {
        CodecConfigureBean releaseBean;
        synchronized (lock) {
            if (codecDestory) {
                return false;
            }

            if (width != bean.width || height != bean.height) {
                width = bean.width;
                height = bean.height;
                log("Recv video size : " + width + " * " + height);
                sizeConfiged = true;
            }
            releaseBean = onCodecLifeListener.onSyncCodecPrepareRelease();
        }
        onCodecLifeListener.onCodecReleasing(releaseBean);
        int result = tryOpenCodec();
        log("Restart encodec result! " + result);
        return result == 0;
    }

    @Override
    public boolean pause() {
        synchronized (lock) {
            if (paused) {
                return true;
            }
            paused = true;
            log("Pause codec... " + this);
        }
        return true;
    }

    @Override
    public boolean resume() {
        synchronized (lock) {
            if (!paused) {
                return true;
            }
            paused = false;
            log("Resume codec... " + this);
        }
        return true;
    }

    @Override
    public void release() {
        CodecConfigureBean bean;
        synchronized (lock) {
            if (codecDestory) {
                return;
            }

            log("Release codec... " + this);
            codecDestory = true;
            bean = onCodecLifeListener.onSyncCodecPrepareRelease();
        }
        onCodecLifeListener.onCodecReleasing(bean);
    }

    private void log(String msg) {
        MyLog.lp(tag, msg);
    }

    private void logE(String msg) {
        MyLog.lpe(tag, msg);
    }
}
