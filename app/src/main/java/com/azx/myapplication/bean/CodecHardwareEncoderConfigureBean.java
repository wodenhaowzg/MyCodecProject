package com.azx.myapplication.bean;

import android.media.MediaCodec;
import android.view.Surface;

import com.azx.myapplication.codec.encoder.HardwareEncoderMediaCallBack;


public class CodecHardwareEncoderConfigureBean extends CodecBaseEncoderConfigureBean {

    public MediaCodec mediaCodec;
    public Surface surface; // 接收新的surface
    public HardwareEncoderMediaCallBack hardwareEncoderMediaCallBack;
    public boolean resetMode;
    public boolean surfaceEnabled;
    public boolean notifySurfaceReleased;

    @Override
    public String toString() {
        return "HardwareEncoderConfigureBean{" +
                "mediaCodec=" + mediaCodec +
                ", width=" + width +
                ", height=" + height +
                ", fps=" + fps +
                ", bitrate=" + bitrate +
                ", gop=" + gop +
                ", surfaceEnabled=" + surfaceEnabled +
                ", surface=" + surface +
                '}';
    }
}
