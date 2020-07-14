package com.azx.myapplication.codec;

import com.azx.myapplication.bean.CodecConfigureBean;

public interface BaseCodec {

    boolean open(CodecConfigureBean bean);

    boolean restart(CodecConfigureBean bean);

    boolean pause();

    boolean resume();

    void release();
}
