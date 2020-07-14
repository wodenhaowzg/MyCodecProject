package com.azx.myapplication.utils;

import android.util.Log;

import java.util.HashMap;

public class MyLog {

    public static boolean DEBUG_MODE;
    private static final String TAG = "WSTECH";
    private static final String LOCAL_PREVIEW = "LocalPreview";
    private static final String GLRENDERER = "OPENGL_WATCH";
    public static final String VIDEO_FRAME_WATCH = "VIDEO_FRAME_WATCH";

    private static HashMap<String, Long> fastLogCache = new HashMap<>();

    public static void i(String tag, String msg) {
        logI(tag, msg);
    }

    public static void lp(String tag, String msg) {
        logD(TAG, formatWatchMessage(LOCAL_PREVIEW, tag, msg));
    }

    public static void lpd(String tag, String msg) {
        if (DEBUG_MODE) {
            logD(TAG, formatWatchMessage(LOCAL_PREVIEW, tag, msg));
        }
    }

    public static void lpe(String tag, String msg) {
        logE(TAG, formatWatchMessage(LOCAL_PREVIEW, tag, msg));
    }

    public static void gld(String tag, String msg) {
        logD(TAG, formatWatchMessage(LOCAL_PREVIEW, GLRENDERER, tag, msg));
    }

    public static void gldd(String tag, String msg) {
        if (DEBUG_MODE) {
            logD(TAG, formatWatchMessage(LOCAL_PREVIEW, GLRENDERER, tag, msg));
        }
    }

    public static void glde(String tag, String msg) {
        logE(TAG, formatWatchMessage(LOCAL_PREVIEW, GLRENDERER, tag, msg));
    }

    public static void fast(String tag, String msg) {
//        logD(TAG, formatWatchMessage(GLRENDERER, tag, msg));
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
        logD(TAG, formatLogMessage(tag, msg));
    }

    private static String formatLogMessage(String tag, String msg) {
        return "<" + tag + "> - " + msg;
    }

    private static String formatWatchMessage(String watcher, String tag, String msg) {
        return "<" + watcher + "> - " + tag + " invoked! -> " + msg;
    }

    private static String formatWatchMessage(String watcher, String secondWatcher, String tag, String msg) {
        return "<" + watcher + "> - <" + secondWatcher + "> - " + tag + " invoked! -> " + msg;
    }

    public static void printStackTrace() {
        if (DEBUG_MODE) {
            logD(TAG, Log.getStackTraceString(new Throwable()));
        }
    }

    private static void logI(String tag, String msg) {
        Log.i(tag, msg);
    }

    private static void logD(String tag, String msg) {
        Log.d(tag, msg);
    }

    private static void logW(String tag, String msg) {
        Log.w(tag, msg);
    }

    private static void logE(String tag, String msg) {
        Log.e(tag, msg);
    }
}
