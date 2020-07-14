package com.azx.myapplication.codec.encoder;

import com.azx.myapplication.inter.OnVideoModuleEventCallBack;

public interface BaseEncoder {

    void setEncoderParams(int fps, int bitrate, int gop);

    void setOnVideoModuleEventCallBack(OnVideoModuleEventCallBack onVideoModuleEventCallBack);

    void setDualEncoder(boolean dualEncoder);

    boolean isHardwareEncoder();
}
