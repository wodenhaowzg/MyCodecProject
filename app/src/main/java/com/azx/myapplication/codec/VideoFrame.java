package com.azx.myapplication.codec;

import java.util.Arrays;

public class VideoFrame {

    @Override
    public String toString() {
        return "VideoFrame{" +
                "frameType=" + frameType +
                ", data=" + Arrays.toString(data) +
                ", width=" + width +
                ", height=" + height +
                ", timeStamp=" + timeStamp +
                '}';
    }

    public VideoFrameType frameType;
    public byte[] data;
    public int width, height;
    public long timeStamp;

    public enum VideoFrameType {
        FRAMETYPE_INVALID,
        FRAMETYPE_SPS_PPS,
        FRAMETYPE_I,
        FRAMETYPE_P
    }
}
