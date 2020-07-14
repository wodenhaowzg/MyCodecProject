package com.azx.myapplication.codec.encoder;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

import com.azx.myapplication.codec.BaseCodecImpl;
import com.azx.myapplication.codec.CodecLife;
import com.azx.myapplication.codec.OnCodecLifeListener;
import com.azx.myapplication.inter.OnVideoModuleEventCallBack;

import java.util.Locale;

public abstract class BaseEncoderImpl extends BaseCodecImpl implements BaseEncoder, OnCodecLifeListener {

    static final String ENCODER_TYPE = "video/avc"; // H264

    OnVideoModuleEventCallBack mOnVideoModuleEventCallBack;
    final MediaCodecInfo mediaCodecInfo;
    final int mVideoColorFormat;

    boolean dualEncoder;
    int fps, bitrate, gop;
    boolean encoderParamsChanged;

    byte[] mEncodeDatas;
    byte[] sps_pps_byte_buffer;
    int sps_pps_len;

    public BaseEncoderImpl() {
        codecLife = new CodecLife(TAG, this);
        mediaCodecInfo = chooseVideoEncoderInfo();
        mVideoColorFormat = chooseVideoEncoderColorFormat(mediaCodecInfo);
    }

    @Override
    public void setOnVideoModuleEventCallBack(OnVideoModuleEventCallBack onVideoModuleEventCallBack) {
        this.mOnVideoModuleEventCallBack = onVideoModuleEventCallBack;
    }

    @Override
    public void setDualEncoder(boolean dualEncoder) {
        this.dualEncoder = dualEncoder;
    }

    public void setEncoderParams(int fps, int bitrate, int gop) {
        if (fps == 0 || bitrate == 0 || gop == 0) {
            return;
        }

        if (this.fps != fps || this.bitrate != bitrate || this.gop != gop) {
            final Object lock = codecLife.getLock();
            synchronized (lock) {
                this.fps = fps;
                this.bitrate = bitrate;
                this.gop = gop;
                encoderParamsChanged = true;
            }
            log("Set encodr params : " + fps + " | " + bitrate + " | " + gop);
        }
    }

    @Override
    public boolean onSyncCodecStartCheck() {
        if (!encoderParamsChanged) { // 编码器参数都未发生改变或还没设置，无法或没必要config
            logE("config check failed! size or params not setting or changed!");
            return false;
        }
        return true;
    }

    int chooseVideoEncoderColorFormat(MediaCodecInfo mediaCodecInfo) {
        int matchedColorFormat = 0;
        if (mediaCodecInfo == null) {
            return 0;
        }

        MediaCodecInfo.CodecCapabilities cc = mediaCodecInfo.getCapabilitiesForType(ENCODER_TYPE);
        for (int i = 0; i < cc.colorFormats.length; i++) {
            int cf = cc.colorFormats[i];
            logd(String.format(Locale.CHINA, "vencoder %s supports color fomart 0x%x(%d)", mediaCodecInfo.getName(), cf, cf));
            // choose YUV for h.264, prefer the bigger one.
            // corresponding to the color space transform in onPreviewFrame
            if (cf >= MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar && cf <= MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
                if (cf > matchedColorFormat) {
                    matchedColorFormat = cf;
                }
            }
        }
        return matchedColorFormat;
    }

    private MediaCodecInfo chooseVideoEncoderInfo() {
        int nbCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < nbCodecs; i++) {
            MediaCodecInfo mci = MediaCodecList.getCodecInfoAt(i);
            if (!mci.isEncoder()) {
                continue;
            }
            String[] types = mci.getSupportedTypes();
            for (String type : types) {
                logd(String.format("vencoder support %s types: %s", mci.getName(), type));
                if (type.equalsIgnoreCase(ENCODER_TYPE)) {
                    return mci;
                }
            }
        }
        return null;
    }
}
