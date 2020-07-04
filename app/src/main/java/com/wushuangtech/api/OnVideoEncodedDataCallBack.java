package com.wushuangtech.api;

public interface OnVideoEncodedDataCallBack {

    void onEncodedDataRreport(byte[] data, int frameType, int width, int height, long pts);
}
