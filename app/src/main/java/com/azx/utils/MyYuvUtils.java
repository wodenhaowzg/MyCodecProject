package com.azx.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import java.util.Arrays;

public class MyYuvUtils {

    private static Allocation in, out;
    private static RenderScript renderScript;
    private static ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private static int lastWidth, lastHeight;
    private static int lastDataLen;

    public static Bitmap nv21ToBitmap(Context context, byte[] nv21, int width, int height) {
        if (renderScript == null) {
            renderScript = RenderScript.create(context);
        }

        if (yuvToRgbIntrinsic == null) {
            yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));
        }

        if (in == null || lastDataLen != nv21.length) {
            Type.Builder yuvType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(nv21.length);
            in = Allocation.createTyped(renderScript, yuvType.create(), Allocation.USAGE_SCRIPT);
            lastDataLen = nv21.length;
        }

        if (out == null || lastWidth != width || lastHeight != height) {
            Type.Builder rgbaType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(width).setY(height);
            out = Allocation.createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SCRIPT);
            lastWidth = width;
            lastHeight = height;
        }

        in.copyFrom(nv21);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        return bmpout;
    }

    /**
     * 将给定的nv21数据，把y, uv 两个平面分离出来。
     *
     * @param nv21Array 给定的 nv21 数据
     * @param width     图像的宽度
     * @param height    图像的高度
     * @return 返回y, uv 两个平面的单独数据。
     */
    public static byte[][] spliteNV21Array(byte[] nv21Array, int width, int height) {
        byte[][] result = new byte[3][];
        // nv21 是 YUV420SP ，v在u之前
        byte[] yArray = new byte[width * height]; // 每个像素点都有 y 值。
        byte[] vuArray = new byte[width * height / 2]; // 每四个像素点共用一个 uv 值
        System.arraycopy(nv21Array, 0, yArray, 0, yArray.length);
        System.arraycopy(nv21Array, yArray.length, vuArray, 0, vuArray.length);
        result[0] = yArray;
        result[1] = vuArray;
        return result;
    }

    /**
     * 将单平面的nv21数据，组成一个完整的nv21数据。
     *
     * @param yPlanar  给的数据是 y 平面的数据还是 uv 平面的数据
     * @param srcArray 单平面数据，有y、uv 两种
     * @param width    图像的宽度
     * @param height   图像的高度
     * @return 完整平面的yuv数据
     */
    public static byte[] mergeNV21Array(boolean yPlanar, byte[] srcArray, int width, int height) {
        byte[] yuvArray = new byte[width * height * 3 / 2];
        Arrays.fill(yuvArray, (byte) -128);
        int yOffset = width * height;
        if (yPlanar) { // y
            System.arraycopy(srcArray, 0, yuvArray, 0, srcArray.length);
        } else { // u
            System.arraycopy(srcArray, 0, yuvArray, yOffset, srcArray.length);
        }
        return yuvArray;
    }
}
