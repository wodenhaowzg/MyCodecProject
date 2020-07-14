package com.azx.myapplication.inter;

public interface OnVideoModuleEventCallBack {

    int onVideoTextureFrameReport(int textureId, byte[] data, int width, int height);

    void onVideoNV21FrameReport(byte[] data, int width, int height);

    void onVideoEncodedDataReport(boolean dualEncoder, byte[] data, int frameType, int width, int height, long pts);

    void onVideoCameraError(int error);
}
