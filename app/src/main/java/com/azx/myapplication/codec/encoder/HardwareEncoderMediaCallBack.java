package com.azx.myapplication.codec.encoder;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaFormat;

import com.azx.myapplication.codec.VideoStatus;
import com.azx.myapplication.utils.MyLog;

import java.lang.ref.WeakReference;

@SuppressLint("NewApi")
public class HardwareEncoderMediaCallBack extends MediaCodec.Callback {

    private static final String TAG = "LocalVideoEncoder - HardwareEncoder";
    public int width, height;
    private volatile boolean stopProcess;

    private WeakReference<HardwareEncoder> outReference;

    HardwareEncoderMediaCallBack(HardwareEncoder outReference) {
        this.outReference = new WeakReference<>(outReference);
    }

    void stopProcess() {
        this.stopProcess = true;
        MyLog.lp(TAG, "Stop process encoding..." + this);
    }

    @Override
    public void onInputBufferAvailable(MediaCodec codec, int index) {
        MyLog.lp(TAG, "onInputBufferAvailable..." + index);
    }

    @Override
    public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
        if (stopProcess) {
            return;
        }
        HardwareEncoder hardwareEncoder = outReference.get();
        boolean drop = false;
        if (hardwareEncoder == null || !hardwareEncoder.checkEncoderStatus()) {
            drop = true;
        }

        if (index >= 0) {
            if (drop) {
                MyLog.lpe(TAG, "Drop data to encode ..." + index);
                codec.releaseOutputBuffer(index, false);
            } else {
//                MyLog.lp(TAG, "Process data to encode ..." + index + " | " + this + " | " + System.nanoTime() + " | " + info.presentationTimeUs);
                hardwareEncoder.obtainVideoDatas(codec, info, index, false);
            }
        }
    }

    @Override
    public void onError(MediaCodec codec, MediaCodec.CodecException e) {
        e.printStackTrace();
        VideoStatus.addVideoEncodeErrorFrameTimes();
        MyLog.lpe(TAG, "onError..." + e.getLocalizedMessage());
    }

    @Override
    public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
        MyLog.lp(TAG, "onOutputFormatChanged..." + format.toString());
    }
}
