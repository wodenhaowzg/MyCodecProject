package com.azx.myapplication.codec;

public class VideoStatus {

    // 经过帧率稳定函数过滤后，实际渲染的帧数，不是帧率，帧率需要再计算。
    public static long videoCapFrameTimes;
    public static long videoCapDropFrameTimes;

    // opengles渲染的效率。
    public static int videoCapFrameEffectBufferSurface;
    public static int videoCapFrameEffectDisplay;
    public static int videoCapFrameEffectEncoder;

    public static long videoEncodeFrameTimes; // 编码次数
    public static long videoEncodeFrameITimes; // 编码I帧的次数
    public static long videoEncodeFrameErrorTimes; // 编码错误次数
    public static long videoEncoderSoftRecvFrameTimes; // 软编接收数据的次数

    public static int mEglSurfaceCreateTimes;
    public static OpenglESRenderStatus mEglRenderStatus;
    public static int mEglDisplaySurfaceNum;
    public static int mEglEncodeSurfaceNum;

    // 通过libyuv转换数据格式花费的时间，瞬时值
    public static long videoYuvChangeFormatSpendTimes;

    public static long notifyVideoFrameTimes;
    public static long notifyVideoFrameAvgTime;

    public static OpenglESVideoReadPixelType mVideoReadPixelType;
    public static long mVideoReadPixelSpendTime;
    public static long mVideoReadPixelSpendTimePBO;

    // 相芯美颜耗时
    public static int faceUnityBeautfySpendTime;

    // 要删除的
    // 帧率稳定函数未过滤之前的，即egl线程执行的频率。
    public static int videoCapBeforeFrameRate;

    public enum OpenglESVideoReadPixelType {

        FBO, PBO
    }

    public enum OpenglESRenderStatus {

        RENDERING, PAUSE, STOP
    }

    public static void addVideoCapFrameTimes() {
        videoCapFrameTimes++;
//        GlobalConfig.mVideoCapFramsFirst++;
    }

    public static void addVideoCapDropFrameTimes() {
        videoCapDropFrameTimes++;
//        GlobalConfig.mVideoCapFramsDrop++;
    }

    public static void addVideoEncodedFrameTimes() {
        videoEncodeFrameTimes++;
//        GlobalConfig.mVideoCapFramsEncoded++;
    }

    public static void addVideoEncodeIFrameTimes() {
        videoEncodeFrameITimes++;
//        GlobalConfig.mVideoEncodedIFrames++;
    }

    public static void addVideoEncodeErrorFrameTimes() {
        videoEncodeFrameErrorTimes++;
//        GlobalConfig.mVideoEncodeErrorFrames++;
    }

    public static void addVideoEncoderSoftRecvFrameTimes() {
        videoEncoderSoftRecvFrameTimes++;
//        GlobalConfig.mVideoEncoderRecvFrames++;
    }
}
