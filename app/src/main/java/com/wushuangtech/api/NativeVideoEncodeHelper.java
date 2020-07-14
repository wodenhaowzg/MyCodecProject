package com.wushuangtech.api;

import android.media.MediaCodecInfo;

public class NativeVideoEncodeHelper {

    private String TAG;

    private static final int FORMAT_I420 = 1;
    private static final int FORMAT_NV12 = 2;

    public static final int TTT_FORMAT_NV21 = 1;
    public static final int TTT_FORMAT_NV12 = 2;
    public static final int TTT_FORMAT_RGBA = 3;
    public static final int TTT_FORMAT_ABGR = 4;
    public static final int TTT_FORMAT_I420 = 5;
    public static final int TTT_FORMAT_ARGB = 6;

    private long mlencoder;
    private int mVideoColorFormat;
    private int width, height;
    private OnVideoEncodedDataCallBack onVideoEncodedDataCallBack;

    public NativeVideoEncodeHelper(int mVideoColorFormat) {
        this.mVideoColorFormat = mVideoColorFormat;
        mlencoder = Initialize(this);
        TAG = "NativeVideoEncodeHelper - LocalVideoEncoder";
    }

    public void setOnVideoEncodedDataCallBack(OnVideoEncodedDataCallBack onVideoEncodedDataCallBack) {
        this.onVideoEncodedDataCallBack = onVideoEncodedDataCallBack;
    }

    public boolean openSoftEncoder(int width, int height, int fps, int bitrate, int gop) {
        if (mlencoder == 0) {
            return false;
        }
        MyLog.lp(TAG, "Open soft encoder params, size : " + width + " * " + height + " | fps : " + fps + "  | " + bitrate + " | " + gop);
        this.width = width;
        this.height = height;
        if (MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar == mVideoColorFormat) {
            return openSoftEncoder(mlencoder, width, height, fps, bitrate, gop * fps, FORMAT_NV12);
        } else {
            return openSoftEncoder(mlencoder, width, height, fps, bitrate, gop * fps, FORMAT_I420);
        }
    }

    public void closeSoftEncoder() {
        if (mlencoder == 0) {
            return;
        }
        MyLog.lp(TAG, "Close soft encoder");
        closeSoftEncoder(mlencoder);
    }

    public void unInitialize() {
        if (mlencoder == 0) {
            return;
        }
        Uninitialize(mlencoder);
        mlencoder = 0;
    }

    public void setSoftEncoderParams(int dataWidth, int dataHeight, int cropLeft, int cropTop, int cropWidth, int cropHeight, int encodeWidth, int encodeHeight, int rotate) {
        if (mlencoder == 0) {
            return;
        }

        MyLog.lp(TAG, "Set encoder params, data size : " + dataWidth + " * " + dataHeight + " | crop : " + cropLeft + "  | " + cropTop + " | " + cropWidth + " * " + cropHeight +
                " | scale size : " + +encodeWidth + " * " + encodeHeight + " | rotate : " + rotate);
        this.width = encodeWidth;
        this.height = encodeHeight;
        setEncoderResolution(mlencoder, dataWidth, dataHeight, cropLeft, cropTop, cropWidth, cropHeight, encodeWidth, encodeHeight, rotate);
    }

    public void changeSoftEncParams(int mScaleWidth, int mScaleHeight, int mFps, int mBitrate, int gop) {
        changeSoftEncParams(mlencoder, mScaleWidth, mScaleHeight, mFps, mBitrate, gop);
    }

    public byte[] changeVideoDataFormat(byte[] data, boolean mIsRemoteVideoMirror, int format) {
        if (mlencoder == 0) {
            return null;
        }

        if (mVideoColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
            return CommonToNV12(mlencoder, data, mIsRemoteVideoMirror, format);
        } else {
            return CommonToI420(mlencoder, data, mIsRemoteVideoMirror, format);
        }
    }

    public void encodeYuvFrame(byte[] yuvData, int ts) {
        encodeYuvFrame(mlencoder, yuvData, ts);
    }

    private native long Initialize(NativeVideoEncodeHelper encoder);

    private native void Uninitialize(long lencoder);

    private native boolean openSoftEncoder(long lencoder, int nWidth, int nHeight, int nFs, int nBitRate, int nGop, int YuvType);

    private native void encodeYuvFrame(long lencoder, byte[] yuvFrame, int pts);//I420格式

    private native void closeSoftEncoder(long lencoder);

    private native void setBitRate(long lencoder, int bitRate);

    private native void setEncoderResolution(long lencoder, int outWidth, int outHeight, int cropLeft, int cropTop, int cropWidth, int cropHeight, int scaleWidth, int scaleHeight, int rotate);

    private native void changeSoftEncParams(long lencoder, int width, int height, int bitrate, int fps, int gop);

    public native boolean videoDataRotateOperator(long lencoder, int format, byte[] data, int width, int height, int pixelWidth, int rotate, boolean flip);

    public native byte[] CommonToNV12(long lencoder, byte[] yuvFrame, boolean flip, int format);

    private native byte[] CommonToI420(long lencoder, byte[] yuvFrame, boolean flip, int format);

    private native boolean RGBAToARGB(long lencoder, byte[] data, int width, int height, int pixelWidth);

    private native boolean ARGBToNV21(long lencoder, byte[] data, int width, int height, int pixelWidth);

    public boolean yuvVideoDataRotate(int format, byte[] array, int width, int height, int rotate, boolean flip) {
        return videoDataRotateOperator(mlencoder, format, array, width, height, 0, rotate, flip);
    }

    public boolean rgbVideoDataRotate(int format, byte[] array, int width, int height, int pixelWidth, int rotate, boolean flip) {
        return videoDataRotateOperator(mlencoder, format, array, width, height, pixelWidth, rotate, flip);
    }

    public boolean RGBAToARGB(byte[] srcArray, int width, int height, int pixelWidth) {
        return RGBAToARGB(mlencoder, srcArray, width, height, pixelWidth);
    }

    public boolean ARGBToNV21(byte[] srcArray, int width, int height, int pixelWidth) {
        return ARGBToNV21(mlencoder, srcArray, width, height, pixelWidth);
    }

    //软件编码完成后回调函数
    private void OnYuvFrameEncoded(byte[] encdata, int length, int frameType, int ptsMs) {  // FIXME  时间戳需要改成long
//        long current = (System.nanoTime() / 1000000);
//        long diff = (current - (mPresentTimeUs / 1000 + ptsMs));
//        if (diff < 0) {
//            diff = 0;
//        } else if (diff > 2000) {
//            diff = 2000;
//        }

//        Log.d("LocalVideoEncoder","OnYuvFrameEncoded... " + length + " | pts : " + ptsMs);
        byte nelkey = (byte) (encdata[4] & 0x1f);
        //去掉编码前面00 00 00 01
        byte[] sendData = new byte[length - 4];
        System.arraycopy(encdata, 4, sendData, 0, length - 4);

        if (nelkey == 7) {
            onVideoEncodedDataCallBack.onEncodedDataRreport(sendData, ExternalVideoModuleCallback.VideoFrameType.FRAMETYPE_I.ordinal(), width, height, System.currentTimeMillis());
        } else {
            onVideoEncodedDataCallBack.onEncodedDataRreport(sendData, ExternalVideoModuleCallback.VideoFrameType.FRAMETYPE_P.ordinal(), width, height, System.currentTimeMillis());
        }
    }
}
