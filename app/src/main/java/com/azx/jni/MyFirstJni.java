package com.azx.jni;

import android.util.Log;

public class MyFirstJni {

    public native boolean initialize(MyFirstJni mMyFirstJni);

    public native void unInitialize();

    public native void firstOneNativeMethod();

    public native void firstTwoNativeMethod(long arg1);

    private void firstNativeCallBack() {
        Log.d("wzg", "firstNativeCallBack invoked...");
    }

    private void secondNativeCallBack(String s) {
        Log.d("wzg", "secondNativeCallBack invoked...");
    }
}
