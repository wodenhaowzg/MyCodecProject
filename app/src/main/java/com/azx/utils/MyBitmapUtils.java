package com.azx.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.nio.ByteBuffer;

public class MyBitmapUtils {

    /**
     * 将格式为 ARGB_8888 的 bitmap 对象中每个颜色通道分离出来，产生单通道的bitmap。
     *
     * @param bitmap bitmap 对象。
     * @return 返回原始的 bitmap 对象以及r，g，b 三个单颜色通道的 bitmap，一共四个。
     */
    public static Bitmap[] spliteArgbBitmapColor(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap[] rgbArray = new Bitmap[4];
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 获取bitmap总的字节数
        int byteCount = bitmap.getByteCount();
        // a，r，g，b 每一个颜色通道占1个字节，所以一个颜色通道的大小为总大小的四分之一
        int byteCountPerChannel = byteCount / 4;
        // 从bitmap中获取原始的字节数组
        ByteBuffer buf = ByteBuffer.allocate(byteCount);
        bitmap.copyPixelsToBuffer(buf);
        byte[] srcArray = buf.array();
        // 获取 r，g，b 每个通道的颜色数据，bitmap 颜色排序是 r，g，b，a 依次排列。
        byte[] rArray = new byte[byteCountPerChannel];
        byte[] gArray = new byte[byteCountPerChannel];
        byte[] bArray = new byte[byteCountPerChannel];
        int rColorIndex = 0;
        int gColorIndex = 1;
        int bColorIndex = 2;
        for (int i = 0; i < byteCountPerChannel; i++) {
            rArray[i] = srcArray[rColorIndex];
            rColorIndex += 4;

            gArray[i] = srcArray[gColorIndex];
            gColorIndex += 4;

            bArray[i] = srcArray[bColorIndex];
            bColorIndex += 4;
        }

        // 转换为 pixels 数组
        int[] rPixels = new int[byteCountPerChannel];
        int[] gPixels = new int[byteCountPerChannel];
        int[] bPixels = new int[byteCountPerChannel];
        for (int i = 0; i < byteCountPerChannel; i++) {
            // java 的第一个bit是符号位，正常的颜色大小范围是0-255，java的byte是-128-127，所以如果是负数
            // 说明值超过了127，需要进行特殊处理
            int redByte = rArray[i];
            if (redByte < 0) {
                redByte = Math.abs(redByte) + 127;
            }
            // alpha 0代表透明，255代表不透明，这里需要不透明
            int color = Color.argb(255, redByte, 0, 0);
            rPixels[i] = color;

            int greenByte = gArray[i];
            if (greenByte < 0) {
                greenByte = Math.abs(greenByte) + 127;
            }
            int gColor = Color.argb(255, 0, greenByte, 0);
            gPixels[i] = gColor;

            int blueByte = bArray[i];
            if (blueByte < 0) {
                blueByte = Math.abs(blueByte) + 127;
            }
            int bColor = Color.argb(255, 0, 0, blueByte);
            bPixels[i] = bColor;
        }

        Bitmap rBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        rBitmap.setPixels(rPixels, 0, width, 0, 0, width, height);
        Bitmap gBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        gBitmap.setPixels(gPixels, 0, width, 0, 0, width, height);
        Bitmap bBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bBitmap.setPixels(bPixels, 0, width, 0, 0, width, height);
        rgbArray[0] = bitmap;
        rgbArray[1] = rBitmap;
        rgbArray[2] = gBitmap;
        rgbArray[3] = bBitmap;
        return rgbArray;
    }
}
