package com.azx.myapplication;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.azx.myapplication.databinding.ActivityMainBinding;
import com.azx.utils.MyBitmapUtils;
import com.azx.utils.MyFileUtils;
import com.azx.utils.MyYuvUtils;
import com.wushuangtech.api.NativeVideoEncodeHelper;

import java.io.File;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NativeVideoEncodeHelper mNativeVideoEncodeHelper;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("codec_ttt");
    }

    private ActivityMainBinding viewDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initJni();

        // 手机的根目录下需要有此路径的文件，才能成功执行，默认解析出的bitmap格式为ARGB_8888
        File externalStorageFile = MyFileUtils.getExternalStorageFile("1/2.jpg");
        if (externalStorageFile == null) {
            finish();
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(externalStorageFile.getAbsolutePath());
        testBitmapSplite(bitmap);
        testRgbRotate(bitmap, 90, false);
        testYuvAndSplite(bitmap);
        testYuvRotate(bitmap, 90, false);
    }

    /**
     * 测试bitmap单通道颜色分离。
     */
    private void testBitmapSplite(Bitmap bitmap) {
        Bitmap[] bitmaps = MyBitmapUtils.spliteArgbBitmapColor(bitmap);
        viewDataBinding.mainSrcImage.setImageBitmap(bitmaps[0]);
        viewDataBinding.mainRedImage.setImageBitmap(bitmaps[1]);
        viewDataBinding.mainGreenImage.setImageBitmap(bitmaps[2]);
        viewDataBinding.mainBlueImage.setImageBitmap(bitmaps[3]);
    }

    /**
     * 测试argb格式的数据旋转和镜像，数据来源为bitmap
     */
    private void testRgbRotate(Bitmap bitmap, int rotate, boolean flip) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        ByteBuffer buf = ByteBuffer.allocate(bitmap.getByteCount());
        buf.position(0);
        bitmap.copyPixelsToBuffer(buf);
        boolean rotateResult = mNativeVideoEncodeHelper.rgbVideoDataRotate(NativeVideoEncodeHelper.TTT_FORMAT_ARGB,
                buf.array(), width, height, 4, rotate, flip);
        if (!rotateResult) {
            throw new RuntimeException("rgbVideoDataRotate failed!");
        }

        // 这里需要重置position !!! 一个坑
        buf.position(0);
        if (rotate == 90 || rotate == 270) {
            Bitmap desBitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);
            desBitmap.copyPixelsFromBuffer(buf);
            viewDataBinding.mainRgbRotateFlipImage.setImageBitmap(desBitmap);
        } else {
            bitmap.copyPixelsFromBuffer(buf);
            viewDataBinding.mainRgbRotateFlipImage.setImageBitmap(bitmap);
        }

    }

    /**
     * 测试nv21格式的数据平面分离，数据来源为bitmap
     */
    private void testYuvAndSplite(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int byteCount = bitmap.getByteCount();
        ByteBuffer buf = ByteBuffer.allocate(byteCount);
        bitmap.copyPixelsToBuffer(buf);
        byte[] argb = buf.array();
        byte[] nv21Array = mNativeVideoEncodeHelper.ARGBToNV21(argb, width, height, 4);
        if (nv21Array == null) {
            throw new RuntimeException("ARGBToNV21 failed!");
        }

        // 将 nv21 数据分离为两个平面的数据，y平面和uv平面。
        byte[][] yuvSpliteArray = MyYuvUtils.spliteNV21Array(nv21Array, width, height);
        // 将 y 平面数据合并为一个完整的 nv21 数据展示出来
        byte[] yuvArray = MyYuvUtils.mergeNV21Array(true, yuvSpliteArray[0], width, height);
        Bitmap yBitmap = MyYuvUtils.nv21ToBitmap(this, yuvArray, width, height);
        // 将 uv 平面数据合并为一个完整的 nv21 数据展示出来
        byte[] yuvArray2 = MyYuvUtils.mergeNV21Array(false, yuvSpliteArray[1], width, height);
        Bitmap uvBitmap = MyYuvUtils.nv21ToBitmap(this, yuvArray2, width, height);
        viewDataBinding.mainNv21YImage.setImageBitmap(yBitmap);
        viewDataBinding.mainNv21VuImage.setImageBitmap(uvBitmap);
    }

    /**
     * 测试nv21格式的数据旋转，数据来源为bitmap
     */
    private void testYuvRotate(Bitmap bitmap, int rotate, boolean flip) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int byteCount = bitmap.getByteCount();
        ByteBuffer buf = ByteBuffer.allocate(byteCount);
        bitmap.copyPixelsToBuffer(buf);
        byte[] argb = buf.array();
        byte[] nv21Array = mNativeVideoEncodeHelper.ARGBToNV21(argb, width, height, 4);
        if (nv21Array == null) {
            throw new RuntimeException("ARGBToNV21 failed!");
        }

        boolean rotateResult = mNativeVideoEncodeHelper.yuvVideoDataRotate(NativeVideoEncodeHelper.TTT_FORMAT_NV21, nv21Array, width, height, rotate, flip);
        if (!rotateResult) {
            throw new RuntimeException("videoDataRotate failed!");
        }

        Bitmap desBitmap;
        if (rotate == 90 || rotate == 270) {
            desBitmap = MyYuvUtils.nv21ToBitmap(this, nv21Array, height, width);
        } else {
            desBitmap = MyYuvUtils.nv21ToBitmap(this, nv21Array, width, width);
        }
        viewDataBinding.mainNv21RotateFlip.setImageBitmap(desBitmap);
    }

    private void initJni() {
        mNativeVideoEncodeHelper = new NativeVideoEncodeHelper(0);
    }
}
