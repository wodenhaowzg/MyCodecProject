package com.wushuangtech.videocore;

import android.graphics.SurfaceTexture;
import android.view.Surface;


/**
 * VideoDecoder 软件和硬件编码都有，可以通过创建对象的时候指定软硬件编码
 * VideoDecoder硬件编码根据DecoderH264写的，软件编码使用gffmpeg
 */

public class VideoDecoder {

    private static final String TAG = VideoDecoder.class.getSimpleName();
    private static final boolean DEBUG = false;

    private long mpdecoder;
    private boolean mEnableSoftwareDecoder;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    private boolean isNotifyed, mFirstDraw, isIFrameComing;
    private int mDecWidth, mDecHeight;
    private boolean mIsFinish = true, mIsPause;

    private final Object mLock = new Object();
    private final Object mHardwareLock = new Object();

    private OnVideoDecoderListener mOnVideoDecoderListener;

    public VideoDecoder() {
        mpdecoder = 0;
    }

    public void setOnVideoDecoderListener(OnVideoDecoderListener onVideoDecoderListener) {
        this.mOnVideoDecoderListener = onVideoDecoderListener;
    }

    private native long Initialize(VideoDecoder decoder);

    private native void Uninitialize(long pdecoder);

    private native boolean openSoftDecoder(long ldecoder, int nWidth, int nHeight);

    private native void decodeYuvFrame(long ldecoder, byte[] yuvFrame, int pts);

    private native void closeSoftDecoder(long ldecoder);

    private native boolean setSurface(long ldecoder, Surface surface);

    private native boolean useDecodedData(long ldecoder, boolean use);

    //软件解码完成为ARGB
    private void OnFrameDecoded(byte[] decdata, int width, int height) {
    }

    private void updateDecSize(int width, int height) {
    }

    private void OnFirstFrameDecoded(int width, int height) {
    }

    private void OnFirstFrameDrawed(int width, int height) {
    }

    private void OnFrameSizeChanged(int width, int height) {
    }
}
