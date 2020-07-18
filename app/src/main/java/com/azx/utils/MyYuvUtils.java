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

    public static final int YUV_PLANE_Y = 1;
    public static final int YUV_PLANE_U = 2;
    public static final int YUV_PLANE_V = 3;

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
     * 将给定的YUV420SP数据，把y, uv 两个平面分离出来。
     *
     * @param yuvArray 给定的 YUV420SP 数据，包含具体的 NV12，NV21 两种格式，NV12 是 UV , NV21 是 VU
     * @param width    图像的宽度
     * @param height   图像的高度
     * @return 返回y, uv 两个平面的单独数据。
     */
    public static byte[][] spliteY420SPArray(byte[] yuvArray, int width, int height) {
        byte[][] result = new byte[3][];
        byte[] yArray = new byte[width * height]; // 每个像素点都有 y 值。
        byte[] uvArray = new byte[width * height / 2]; // 每四个像素点共用一个 uv 值
        System.arraycopy(yuvArray, 0, yArray, 0, yArray.length);
        System.arraycopy(yuvArray, yArray.length, uvArray, 0, uvArray.length);
        result[0] = yArray;
        result[1] = uvArray;
        return result;
    }

    public static byte[][] spliteNV12ToSinglePlane(byte[] yuvArray, int width, int height) {
        byte[][] result = new byte[3][];
        byte[] yArray = new byte[width * height]; // 每个像素点都有 y 值。
        byte[] uvArray = new byte[width * height / 2]; // 每四个像素点共用一个 uv 值
        System.arraycopy(yuvArray, 0, yArray, 0, yArray.length);
        System.arraycopy(yuvArray, yArray.length, uvArray, 0, uvArray.length);
        byte[] uArray = new byte[width * height / 4];
        byte[] vArray = new byte[width * height / 4];
        int uCopyIndex = 0, vCopyIndex = 0;
        for (int i = 0; i < uvArray.length; i++) {
            if (i % 2 == 0) {
                uArray[uCopyIndex] = uvArray[i];
                uCopyIndex++;
            } else {
                vArray[vCopyIndex] = uvArray[i];
                vCopyIndex++;
            }
        }
        result[0] = yArray;
        result[1] = uArray;
        result[2] = vArray;
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
    public static byte[] mergeY420SPArray(boolean yPlanar, byte[] srcArray, int width, int height) {
        byte[] yuvArray = new byte[width * height * 3 / 2];
        Arrays.fill(yuvArray, (byte) -128);
        int yOffset = width * height;
        if (yPlanar) { // y
            System.arraycopy(srcArray, 0, yuvArray, 0, srcArray.length);
        } else { // uv
            System.arraycopy(srcArray, 0, yuvArray, yOffset, srcArray.length);
        }
        return yuvArray;
    }

    public static byte[] mergeNV12Array(int planeType, byte[] srcArray, int width, int height) {
        byte[] yuvArray = new byte[width * height * 3 / 2];
        Arrays.fill(yuvArray, (byte) -128);
        int yOffset = width * height;
        int uOffset = yOffset + yOffset / 4;
        if (planeType == YUV_PLANE_Y) { // y
            System.arraycopy(srcArray, 0, yuvArray, 0, srcArray.length);
        } else if (planeType == YUV_PLANE_U) { // uv
            System.arraycopy(srcArray, 0, yuvArray, yOffset, srcArray.length);
        } else {
            System.arraycopy(srcArray, 0, yuvArray, uOffset, srcArray.length);
        }
        return yuvArray;
    }
}
