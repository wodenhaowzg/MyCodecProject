package com.azx.myapplication.codec.encoder;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.view.Surface;

import com.azx.myapplication.bean.CodecConfigureBean;
import com.azx.myapplication.bean.CodecHardwareEncoderConfigureBean;
import com.azx.myapplication.codec.BaseCodecImpl;
import com.azx.myapplication.codec.VideoFrame;
import com.azx.myapplication.codec.VideoStatus;
import com.azx.myapplication.utils.MyLog;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import static android.media.MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR;

public class HardwareEncoder extends BaseEncoderImpl {

    private static final int MEDIACODEC_TIMEOUT_US = 10 * 1000;

    private Thread videoEncoderThread;
    private OnHardwareSurfaceLifeListener onHardwareSurfaceLifeListener;
    private HardwareEncoderMediaCallBack currentHardwareEncoderMediaCallBack;
    private volatile MediaCodec currentMediaCodec;
    private volatile Surface currentEncoderSurface;

    private boolean resetMode;
    private boolean surfaceEnabled;

    boolean checkEncoderStatus() {
        return codecLife.isCodecOpened();
    }

    public void setEnableRestMode() { // FIXME 不支持动态变更
        boolean codecOpened = codecLife.isCodecOpened();
        if (codecOpened) {
            return;
        }
        currentMediaCodec = createVideoEncoder(mediaCodecInfo);
        log("Enable reset mode, create mediacodec : " + currentMediaCodec);
        if (currentMediaCodec == null) {
            return;
        }
        resetMode = true;
    }

    public void setOnHardwareSurfaceLifeListener(OnHardwareSurfaceLifeListener onHardwareSurfaceLifeListener) {
        this.onHardwareSurfaceLifeListener = onHardwareSurfaceLifeListener;
    }

    public void setEncoderSurfaceMode(boolean enabled) {
        final Object lock = codecLife.getLock();
        synchronized (lock) {
            boolean codecOpened = codecLife.isCodecOpened();
            if (codecOpened) {
                return;
            }
            log("Change surface mode :" + enabled);
            this.surfaceEnabled = enabled;
        }
    }

    @Override
    public CodecConfigureBean onSyncCodecStartConfigure(int width, int height) {
        CodecHardwareEncoderConfigureBean bean = new CodecHardwareEncoderConfigureBean();
        bean.mediaCodec = currentMediaCodec;
        bean.width = width;
        bean.height = height;
        bean.fps = fps;
        bean.bitrate = bitrate;
        bean.gop = gop;
        bean.surfaceEnabled = surfaceEnabled;
        bean.notifySurfaceReleased = false;
        return bean;
    }

    @SuppressLint("NewApi")
    @Override
    public CodecConfigureBean onCodecConfiguring(CodecConfigureBean beans) {
        CodecHardwareEncoderConfigureBean bean = (CodecHardwareEncoderConfigureBean) beans;
        if (resetMode) {
            bean.mediaCodec = currentMediaCodec;
            if (currentMediaCodec == null) {
                logE("MediaCodec is null in reset mode!");
                return null;
            }
        } else {
            if (currentMediaCodec != null) { // FIXME 有问题
                bean.mediaCodec = currentMediaCodec;
                return null;
            }
        }

        if (bean.mediaCodec == null) {
            // 创建新的 mediacodec
            bean.mediaCodec = createVideoEncoder(mediaCodecInfo);
            if (bean.mediaCodec == null) {
                return null;
            }
        }

        boolean configEncoder = false;
        try {
            configEncoder = configCodec(bean);
            if (!configEncoder) {
                logE("Config encoder failed!");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logE("Config encoder exception! " + e.getLocalizedMessage());
            return null;
        } finally {
            if (!configEncoder) {
                if (surfaceEnabled && bean.surface != null) {
                    bean.surface.release();
                    bean.surface = null;
                }

                if (resetMode) {
                    bean.mediaCodec.reset();
                } else {
                    bean.mediaCodec.release();
                }
            }
        }
        return bean;
    }

    @Override
    public boolean onSyncCodecStart(CodecConfigureBean bean) {
        CodecHardwareEncoderConfigureBean encoderBean = (CodecHardwareEncoderConfigureBean) bean;
        try {
            encoderBean.mediaCodec.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onSyncCodecAssignment(CodecConfigureBean beans) {
        CodecHardwareEncoderConfigureBean bean = (CodecHardwareEncoderConfigureBean) beans;
        this.currentMediaCodec = bean.mediaCodec;
        this.currentEncoderSurface = bean.surface;
        this.currentHardwareEncoderMediaCallBack = bean.hardwareEncoderMediaCallBack;
    }

    @Override
    public void onCodecStartFinish(CodecConfigureBean bean) {
        if (surfaceEnabled && currentEncoderSurface != null) {
            if (onHardwareSurfaceLifeListener != null) {
                onHardwareSurfaceLifeListener.encoderSurfaceCreated(currentEncoderSurface);
            }
        }
    }

    @Override
    public CodecConfigureBean onSyncCodecPrepareRelease() {
        CodecConfigureBean releaseBean = buildHardwareEncoderReleaseBean(currentMediaCodec, currentHardwareEncoderMediaCallBack, currentEncoderSurface, resetMode,
                surfaceEnabled, true);
        if (!resetMode) {
            currentMediaCodec = null;
        }
        currentEncoderSurface = null;
        currentHardwareEncoderMediaCallBack = null;
        return releaseBean;
    }

    @Override
    public void onSyncCodecReleasing(CodecConfigureBean bean) {
        CodecHardwareEncoderConfigureBean releseBean = (CodecHardwareEncoderConfigureBean) bean;
        releaseCurrentMediaCodec(releseBean);
    }

    @Override
    public boolean onCodecReleasing(CodecConfigureBean bean) {
        CodecHardwareEncoderConfigureBean releseBean = (CodecHardwareEncoderConfigureBean) bean;
        releaseCurrentMediaCodec(releseBean);
        return false;
    }

    @Override
    public boolean onCodecStartFailed() {
        // TODO
        return false;
    }

    @Override
    public boolean isHardwareEncoder() {
        return true;
    }

    @SuppressLint("NewApi")
    public void setEncoderBitrateParams(int bitrate) {
        if (bitrate == 0) {
            return;
        }

        if (this.bitrate != bitrate) {
            MediaCodec mediaCodec = null;
            final Object lock = codecLife.getLock();
            synchronized (lock) {
                this.bitrate = bitrate;
                if (currentMediaCodec != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mediaCodec = currentMediaCodec;
                } else {
                    encoderParamsChanged = true;
                }
            }

            if (mediaCodec != null) {
                Bundle param = new Bundle();
                param.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bitrate);
                mediaCodec.setParameters(param);
            }
        }
    }

    private MediaCodec createVideoEncoder(MediaCodecInfo mediaCodecInfo) {
        if (mediaCodecInfo == null) {
            return null;
        }

        try {
            return MediaCodec.createByCodecName(mediaCodecInfo.getName()); // 这里不判断，如果触发空指针，会转为软编
        } catch (IOException e) {
            e.printStackTrace();
            logE("Create video encoder exception! " + e.getLocalizedMessage());
        }
        return null;
    }

    @SuppressLint("NewApi")
    void obtainVideoDatas(MediaCodec mMediaEncoder, MediaCodec.BufferInfo bufferInfo, int outputBufferIndex, boolean sync) {
        if (currentEncoderSurface == null) {
            VideoStatus.addVideoEncodeErrorFrameTimes();
            return;
        }

        boolean encoded = false;
        try {
            ByteBuffer outputBuffer;
            if (sync) {
                ByteBuffer[] outputBuffers = mMediaEncoder.getOutputBuffers();
                outputBuffer = outputBuffers[outputBufferIndex];
            } else {
                outputBuffer = mMediaEncoder.getOutputBuffer(outputBufferIndex);
            }

            if (outputBuffer == null) {
                mMediaEncoder.releaseOutputBuffer(outputBufferIndex, false);
                return;
            }
            outputBuffer.position(bufferInfo.offset);
            if (mEncodeDatas == null || mEncodeDatas.length != bufferInfo.size) {
                mEncodeDatas = new byte[bufferInfo.size];
            }

            outputBuffer.get(mEncodeDatas);
            byte[] sendData = null;
            byte naluType = (byte) (outputBuffer.get(4) & 0x1f);
            if (naluType == 5) {
                int indexs;
                sendData = new byte[bufferInfo.size + sps_pps_len];
                System.arraycopy(sps_pps_byte_buffer, 0, sendData, 0, sps_pps_len);
                indexs = sps_pps_len;
                System.arraycopy(mEncodeDatas, 0, sendData, indexs, bufferInfo.size);
            } else if (naluType == 7) {
                sps_pps_len = bufferInfo.size - 4;
                if (sps_pps_byte_buffer == null || sps_pps_byte_buffer.length != sps_pps_len) {
                    sps_pps_byte_buffer = new byte[sps_pps_len];
                }
                System.arraycopy(mEncodeDatas, 4, sps_pps_byte_buffer, 0, sps_pps_len);
            } else {
                sendData = new byte[bufferInfo.size - 4]; //FIXME 这里new不太好，是否可以优化
                System.arraycopy(mEncodeDatas, 4, sendData, 0, bufferInfo.size - 4);
            }

            if (sendData != null) {
                if (mOnVideoModuleEventCallBack != null) {
                    int width = codecLife.getCodecWidth();
                    int height = codecLife.getCodecHeight();
                    VideoFrame.VideoFrameType type;
                    if (naluType == 5) {
                        type = VideoFrame.VideoFrameType.FRAMETYPE_I;
                    } else {
                        type = VideoFrame.VideoFrameType.FRAMETYPE_P;
                    }

                    mOnVideoModuleEventCallBack.onVideoEncodedDataReport(dualEncoder, sendData, type.ordinal(), width, height, System.currentTimeMillis());
                }
            }
            mMediaEncoder.releaseOutputBuffer(outputBufferIndex, false);
            encoded = true;
        } catch (Exception e) {
            e.printStackTrace();
            VideoStatus.addVideoEncodeErrorFrameTimes();
        } finally {
            if (!encoded) {
                try {
                    mMediaEncoder.releaseOutputBuffer(outputBufferIndex, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean configCodec(CodecHardwareEncoderConfigureBean configureBean) throws Exception {
        MediaFormat mMediaFormat = MediaFormat.createVideoFormat(ENCODER_TYPE, configureBean.width, configureBean.height);
        int mVideoColorFormat;
        if (configureBean.surfaceEnabled) {
            mVideoColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        } else {
            mVideoColorFormat = this.mVideoColorFormat;
            if (mVideoColorFormat == 0) {
                return false;
            }
        }

        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mVideoColorFormat);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, configureBean.bitrate);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, configureBean.fps);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, configureBean.gop);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, BITRATE_MODE_VBR);
        }
        log("Encoder params list : width : " + configureBean.width + " | height : " + configureBean.height + " | fps : " + configureBean.fps + " | bitrate : " + configureBean.bitrate + " | gop : " + configureBean.gop);
        if (configureBean.surfaceEnabled) {
            configureBean.mediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = configureBean.mediaCodec.createInputSurface();
            log("hardware params, create surface : " + surface);
            configureBean.surface = surface;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                HardwareEncoderMediaCallBack callback = new HardwareEncoderMediaCallBack(this);
                callback.width = configureBean.width;
                callback.height = configureBean.height;
                configureBean.mediaCodec.setCallback(callback);
                configureBean.hardwareEncoderMediaCallBack = callback;
            } else {
                initThread();
            }
        } else {
            configureBean.mediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        }
        return true;
    }

    @SuppressLint("NewApi")
    private void releaseCurrentMediaCodec(CodecHardwareEncoderConfigureBean bean) {
        log("Release mediacodec! " + bean.toString());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            unInitThread();
        }

        if (bean.surfaceEnabled && bean.surface != null) {
            if (bean.notifySurfaceReleased && onHardwareSurfaceLifeListener != null) {
                if (bean.mediaCodec != null) {
                    try {
                        bean.mediaCodec.signalEndOfInputStream();
                    } catch (Exception e) {
                        logE("signalEndOfInputStream failed!");
                    }
                }
                onHardwareSurfaceLifeListener.encoderSurfaceReleased(bean.surface);
            }
            bean.surface.release();
        }

        if (bean.hardwareEncoderMediaCallBack != null) {
            bean.hardwareEncoderMediaCallBack.stopProcess();
        }

        try {
            if (bean.resetMode) {
                if (bean.mediaCodec != null) {
                    bean.mediaCodec.stop();
                    bean.mediaCodec.reset();
                }
            } else {
                if (bean.mediaCodec != null) {
                    bean.mediaCodec.stop();
                    bean.mediaCodec.release();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private CodecHardwareEncoderConfigureBean buildHardwareEncoderReleaseBean(MediaCodec codec, HardwareEncoderMediaCallBack hardwareEncoderMediaCallBack, Surface surface,
                                                                              boolean resetMode, boolean surfaceEnabled, boolean notifySurfaceReleased) {
        CodecHardwareEncoderConfigureBean releaseBean = new CodecHardwareEncoderConfigureBean();
        releaseBean.mediaCodec = codec;
        releaseBean.hardwareEncoderMediaCallBack = hardwareEncoderMediaCallBack;
        releaseBean.surfaceEnabled = surfaceEnabled;
        releaseBean.surface = surface;
        releaseBean.resetMode = resetMode;
        releaseBean.notifySurfaceReleased = notifySurfaceReleased;
        return releaseBean;
    }

    private void pushVideoDataBySurface() {
        MediaCodec encoder = currentMediaCodec;
        if (encoder == null) {
            return;
        }

        try {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, MEDIACODEC_TIMEOUT_US); // FIXME 时间待验证
            while (outputBufferIndex >= 0) {
                obtainVideoDatas(encoder, bufferInfo, outputBufferIndex, true);
                outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, MEDIACODEC_TIMEOUT_US);
            }
        } catch (Exception e) {
            e.printStackTrace();
            VideoStatus.addVideoEncodeErrorFrameTimes();
        }
    }

    private void initThread() {
        if (videoEncoderThread != null) {
            return;
        }

        LocalEncoderRunnable runnable = new LocalEncoderRunnable(this);
        videoEncoderThread = new Thread(runnable);
        if (dualEncoder) {
            videoEncoderThread.setName("HardwareEncoder-Dual");
        } else {
            videoEncoderThread.setName("HardwareEncoder-Main");
        }
        videoEncoderThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        videoEncoderThread.start();
        log("Crate new thread and start! : " + videoEncoderThread);
    }

    private void unInitThread() {
        if (videoEncoderThread != null) {
            videoEncoderThread.interrupt();
            try {
                videoEncoderThread.join();
            } catch (InterruptedException e) {
                videoEncoderThread.interrupt();
            }
            log("Stop thread : " + videoEncoderThread);
            videoEncoderThread = null;
        }
    }

    static class LocalEncoderRunnable implements Runnable {

        private final WeakReference<HardwareEncoder> outReference;

        LocalEncoderRunnable(HardwareEncoder outReference) {
            this.outReference = new WeakReference<>(outReference);
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                HardwareEncoder hardwareEncoder = outReference.get();
                if (hardwareEncoder == null) {
                    return;
                }

                MyLog.fd(BaseCodecImpl.TAG, "Encoder thread " + Thread.currentThread().getName() + " running! recv size : " + VideoStatus.videoEncodeFrameTimes + " | " + VideoStatus.videoEncodeFrameErrorTimes);
                hardwareEncoder.pushVideoDataBySurface();

                // Waiting for next frame
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }

    public interface OnHardwareSurfaceLifeListener {

        void encoderSurfaceCreated(Surface surface);

        void encoderSurfaceReleased(Surface surface);
    }
}
