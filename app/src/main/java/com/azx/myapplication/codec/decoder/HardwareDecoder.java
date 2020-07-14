package com.azx.myapplication.codec.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.azx.myapplication.bean.CodecConfigureBean;
import com.azx.myapplication.bean.CodecDecoderConfigureBean;
import com.azx.myapplication.bean.CodecHardwareEncoderConfigureBean;
import com.wushuangtech.utils.PviewLog;
import com.wushuangtech.videocore.RemoteSurfaceView;
import com.wushuangtech.videocore.inter.OnVideoDecoderEventCallBack;

import java.io.IOException;
import java.nio.ByteBuffer;

public class HardwareDecoder extends BaseDecoderImpl {

    private static final String MIME_TYPE = "video/avc";
    private static final String TAG = "HardwareDecoder";

    private MediaCodec mMediaCodec;
    private int mWidth, mHeight;
    private MediaCodec currentMediaCodec;
    private boolean surfaceEnabled;
    private boolean resetMode;
    private Surface mSurface;

    public void setOnVideoDecoderEventCallBack(OnVideoDecoderEventCallBack mOnVideoDecoderEventCallBack) {
        this.mOnVideoDecoderEventCallBack = mOnVideoDecoderEventCallBack;
    }

    private OnVideoDecoderEventCallBack mOnVideoDecoderEventCallBack;

    public boolean openHardwareDecoder(Surface surface, int width, int height) {
        boolean isFailed = true;
        MediaCodec mediaCodec;
        synchronized (HardwareDecoder.class) {
            mediaCodec = mMediaCodec;
        }

        try {
            if (mediaCodec == null) {
                mediaCodec = MediaCodec.createDecoderByType(MIME_TYPE);
            }

            MediaFormat mMediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            mediaCodec.configure(mMediaFormat, surface, null, 0);
            mediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaCodec.setOnFrameRenderedListener(this, null);
            }
            mediaCodec.start();
            isFailed = false;
            synchronized (HardwareDecoder.class) {
                mMediaCodec = mediaCodec;
                mWidth = width;
                mHeight = height;
            }
            return true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            PviewLog.rv_e(TAG, "open hardware decoder IllegalStateException : " + e.getLocalizedMessage());
            isFailed = true;
        } catch (Exception e) {
            e.printStackTrace();
            PviewLog.rv_e(TAG, "open hardware decoder Exception : " + e.getLocalizedMessage());
            isFailed = true;
        } finally {
            if (isFailed) {
                if (mediaCodec != null) {
                    mediaCodec.release();
                }
            }
        }
        return false;
    }

    public void decodingFrame(RemoteSurfaceView.VideoFrame frame) {
        MediaCodec mediaCodec;
        synchronized (HardwareDecoder.class) {
            mediaCodec = mMediaCodec;
            if (mediaCodec == null) {
                return;
            }
        }

        byte[] buf = frame.data;
        long pts = frame.timeStamp;
        long start_decode = System.currentTimeMillis();
        try {
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex < 0) {
                return;
            }
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, 0, buf.length);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, buf.length, pts, 0);

            // Get output buffer index
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // 解码出数据宽和高
                int width = mMediaCodec.getOutputFormat().getInteger(MediaFormat.KEY_WIDTH);
                int height = mMediaCodec.getOutputFormat().getInteger(MediaFormat.KEY_HEIGHT);
                PviewLog.rv_d(TAG, "mediacodec format changed! " + width + " | " + height);
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                PviewLog.rv_d(TAG, "mediacodec buffer changed!");
            }

            int currentOutputBufferIndex = outputBufferIndex;
            if (outputBufferIndex >= 0) {
                if (mOnVideoDecoderEventCallBack != null) {
                    mOnVideoDecoderEventCallBack.onVideoFirstFrameDecoded(mWidth, mHeight);
                }
            }

            while (outputBufferIndex >= 0) {
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (currentOutputBufferIndex > 0) {
                    if (mOnVideoDecoderEventCallBack != null) {
                        mOnVideoDecoderEventCallBack.onVideoFirstFrameDrawn(mWidth, mHeight);
                    }
                }
            }
            PviewLog.debug(PviewLog.REMOTE_VIEW, TAG, "decode spend time : " + (System.currentTimeMillis() - start_decode));
        } catch (Exception e) {
            e.printStackTrace();
            PviewLog.rv_e(TAG, "hardware decode exception! " + e.getLocalizedMessage());
        }
    }

    @Override
    public boolean onSyncCodecStartCheck() {
        return false;
    }

    @Override
    public CodecConfigureBean onSyncCodecStartConfigure(int width, int height) {
        CodecDecoderConfigureBean bean = new CodecDecoderConfigureBean();
        bean.mediaCodec = currentMediaCodec;
        bean.width = width;
        bean.height = height;
        bean.surfaceEnabled = surfaceEnabled;
        return bean;
    }

    @Override
    public CodecConfigureBean onCodecConfiguring(CodecConfigureBean bean) {
        CodecDecoderConfigureBean temp = (CodecDecoderConfigureBean) bean;
        try {
            if (resetMode) {
                temp.mediaCodec = currentMediaCodec;
                if (currentMediaCodec == null) {
                    logE("MediaCodec is null in reset mode!");
                    return null;
                }
            } else {
                if (currentMediaCodec != null) { // FIXME 有问题
                    temp.mediaCodec = currentMediaCodec;
                    return null;
                }
            }

            if (temp.mediaCodec == null) {
                // 创建新的 mediacodec
                temp.mediaCodec = createVideoDecoder();
                if (temp.mediaCodec == null) {
                    return null;
                }
            }

            MediaCodec mediaCodec = temp.mediaCodec;
            Surface surface = temp.surface;
            boolean surfaceEnabled = temp.surfaceEnabled;
            int width = temp.width;
            int height = temp.height;
            MediaFormat mMediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            if (surfaceEnabled) {
                mediaCodec.configure(mMediaFormat, surface, null, 0);
            } else {
                mediaCodec.configure(mMediaFormat, null, null, 0);
            }
            mediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaCodec.setOnFrameRenderedListener(new MediaCodec.OnFrameRenderedListener() {
                    @Override
                    public void onFrameRendered(@NonNull MediaCodec codec, long presentationTimeUs, long nanoTime) {
//                        if (mOnVideoDecoderEventCallBack != null) {
//                            mOnVideoDecoderEventCallBack.onVideoFirstFrameDrawn(mWidth, mHeight);
//                        }
                    }
                }, null);
            }
            return bean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onSyncCodecStart(CodecConfigureBean bean) {
        CodecDecoderConfigureBean temp = (CodecDecoderConfigureBean) bean;
        try {
            temp.mediaCodec.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onSyncCodecAssignment(CodecConfigureBean bean) {
        CodecDecoderConfigureBean temp = (CodecDecoderConfigureBean) bean;
        this.currentMediaCodec = temp.mediaCodec;
        this.mSurface = temp.surface;
    }

    @Override
    public void onCodecStartFinish(CodecConfigureBean bean) {

    }

    @Override
    public CodecConfigureBean onSyncCodecPrepareRelease() {
        return null;
    }

    @Override
    public void onSyncCodecReleasing(CodecConfigureBean bean) {

    }

    @Override
    public boolean onCodecReleasing(CodecConfigureBean bean) {
        if (mMediaCodec != null) {
            try {
                mMediaCodec.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaCodec.release();
            mMediaCodec = null;
        }
        return false;
    }

    @Override
    public boolean onCodecStartFailed() {
        return false;
    }

    private MediaCodec createVideoDecoder() {
        try {
            return MediaCodec.createDecoderByType(MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
