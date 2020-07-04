package com.azx.utils;

import android.util.Log;

import java.util.HashMap;

public class MyLog {
    public static final boolean DEBUG_MODE = true;
    public static final boolean DEBUG_LEVEL_LOG = true;
    private static final String TAG = "WSTECH";
    private static final String LOCAL_PREVIEW = "LocalPreview";
    public static final String GLRENDERER = "LocalPreview|OPENGL_WATCH";
    public static final String VIDEO_FRAME_WATCH = "VIDEO_FRAME_WATCH";

    private static HashMap<String, Long> fastLogCache = new HashMap<>();

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void lp(String tag, String msg) {
        Log.d(TAG, formatWatchMessage(LOCAL_PREVIEW, tag, msg));
    }

    public static void lpe(String tag, String msg) {
        Log.e(TAG, formatWatchMessage(LOCAL_PREVIEW, tag, msg));
    }

    public static void gld(String tag, String msg) {
        Log.d(TAG, formatWatchMessage(GLRENDERER, tag, msg));
    }

    public static void gldw(String tag, String msg) {
        Log.w(TAG, formatWatchMessage(GLRENDERER, tag, msg));
    }

    public static void glde(String tag, String msg) {
        Log.e(TAG, formatWatchMessage(GLRENDERER, tag, msg));
    }

    public static void gldf(String tag, String msg) {
//        Log.d(TAG, formatWatchMessage(GLRENDERER, tag, msg));
    }

    public static void fd(String tag, String msg) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = fastLogCache.get(tag);
        if (lastTime == null) {
            fastLogCache.put(tag, currentTime);
            return;
        }

        long interval = currentTime - lastTime;
        if (interval < 2000) {
            return;
        }
        fastLogCache.put(tag, currentTime);
        Log.d(TAG, formatLogMessage(tag, msg));
    }

    private static String formatLogMessage(String tag, String msg) {
        return "[" + tag + "] - " + msg;
    }

    private static String formatWatchMessage(String watcher, String tag, String msg) {
        return "[" + watcher + "] - [" + tag + "] invoked! -> " + msg;
    }

    public static void ptd(String tag, String s) {

    }

    public static void ptdf(String tag, String s) {

    }

    public static void printStackTrace() {
        Log.d(TAG, Log.getStackTraceString(new Throwable()));
    }

    public static void rv_d(String tag, String s) {


    }
}
