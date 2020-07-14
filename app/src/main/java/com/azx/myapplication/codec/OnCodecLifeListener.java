package com.azx.myapplication.codec;


import com.azx.myapplication.bean.CodecConfigureBean;

public interface OnCodecLifeListener {

    /**
     * 在打开 codec 之前，让具体的 codec 实例对象进行一次检测，是否有必要打开。
     */
    boolean onSyncCodecStartCheck();

    /**
     * 从具体的 codec 实例对象中，获取配置 codec 的必要参数。
     */
    CodecConfigureBean onSyncCodecStartConfigure(int width, int height);

    /**
     * 获取到配置 codec 的必要参数后，开始配置 codec 。
     */
    CodecConfigureBean onCodecConfiguring(CodecConfigureBean bean);

    /**
     * 成功配置 codec 后，开始尝试打开 codec 。
     */
    boolean onSyncCodecStart(CodecConfigureBean bean);

    /**
     * 成功打开 codec 后，将新的 codec 实例与 codec 实例对象关联起来。
     */
    void onSyncCodecAssignment(CodecConfigureBean bean);

    /**
     * 关联新的 codec 实例后，让具体的 codec 实例对象在打开之后处理一些其他逻辑。
     */
    void onCodecStartFinish(CodecConfigureBean bean);

    /**
     * 关联新的 codec 实例后，让具体的 codec 实例对象在打开之后处理一些其他逻辑。
     */
    CodecConfigureBean onSyncCodecPrepareRelease();

    /**
     * 准备停止 codec ，停止之前通知 codec 实例对象，做准备工作。
     */
    void onSyncCodecReleasing(CodecConfigureBean bean);

    /**
     * 停止 codec 。
     */
    boolean onCodecReleasing(CodecConfigureBean bean);

    boolean onCodecStartFailed();
}
