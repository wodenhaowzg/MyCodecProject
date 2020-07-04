package com.azx.jni;

import android.util.Log;

public class MySecondJni {

    public native boolean initialize(MySecondJni mMySecondJni);

    public native void unInitialize();

    public native void secondOneNativeMethod();

    public native void secondTwoNativeMethod(long arg1);

    private void secondOneNativeCallBack() {
        Log.d("wzg", "secondOneNativeCallBack invoked...");
    }

    private void secondTwoNativeCallBack(String s) {
        Log.d("wzg", "secondTwoNativeCallBack invoked...");
    }
}
