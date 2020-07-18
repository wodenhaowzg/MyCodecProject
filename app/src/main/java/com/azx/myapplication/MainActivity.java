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

    private static final String TEST_FILE = "1/2.jpg";
    private ActivityMainBinding viewDataBinding;
    private File mBitmapFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initJni();

        // 手机的根目录下需要有此路径的文件，才能成功执行，默认解析出的bitmap格式为ARGB_8888
        mBitmapFile = MyFileUtils.getExternalStorageFile(TEST_FILE);
        if (mBitmapFile == null || !mBitmapFile.exists()) {
            throw new RuntimeException("图片文件不存在" + TEST_FILE);
        }
        testBitmapSplite();
        testRgbRotate(90, false);
        testYuvAndSplite();
        testYuvRotate(0, true);
    }

    /**
     * 测试bitmap单通道颜色分离。
     */
    private void testBitmapSplite() {
        Bitmap bitmap = BitmapFactory.decodeFile(mBitmapFile.getAbsolutePath());
        Bitmap[] bitmaps = MyBitmapUtils.spliteArgbBitmapColor(bitmap);
        viewDataBinding.mainSrcImage.setImageBitmap(bitmap);
        viewDataBinding.mainRedImage.setImageBitmap(bitmaps[0]);
        viewDataBinding.mainGreenImage.setImageBitmap(bitmaps[1]);
        viewDataBinding.mainBlueImage.setImageBitmap(bitmaps[2]);
    }

    /**
     * 测试argb格式的数据旋转和镜像，数据来源为bitmap
     */
    private void testRgbRotate(int rotate, boolean flip) {
        Bitmap bitmap = BitmapFactory.decodeFile(mBitmapFile.getAbsolutePath());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        ByteBuffer buf = ByteBuffer.allocate(bitmap.getByteCount());
        buf.position(0);
        bitmap.copyPixelsToBuffer(buf);
        byte[] argb = buf.array();
        boolean rotateResult = mNativeVideoEncodeHelper.rgbVideoDataRotate(NativeVideoEncodeHelper.TTT_FORMAT_ARGB, argb, width, height, 4, rotate, flip);
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
    private void testYuvAndSplite() {
        Bitmap bitmap = BitmapFactory.decodeFile(mBitmapFile.getAbsolutePath());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int byteCount = bitmap.getByteCount();
        ByteBuffer buf = ByteBuffer.allocate(byteCount);
        bitmap.copyPixelsToBuffer(buf);
        byte[] argb = buf.array();
//        boolean result = mNativeVideoEncodeHelper.ARGBToNV21(argb, width, height, 4);
//        if (!result) {
//            throw new RuntimeException("ARGBToNV21 failed!");
//        }

        argb = mNativeVideoEncodeHelper.ARGBToNV12(argb, width, height, 4);
        if (argb == null) {
            throw new RuntimeException("ARGBToNV12 failed!");
        }


//        // 将 nv21 数据分离为两个平面的数据，y平面和uv平面。
//        byte[][] yuvSpliteArray = MyYuvUtils.spliteY420SPArray(argb, width, height);
//        // 将 y 平面数据合并为一个完整的 nv21 数据展示出来
//        byte[] yuvArray = MyYuvUtils.mergeY420SPArray(true, yuvSpliteArray[0], width, height);
//        Bitmap yBitmap = MyYuvUtils.nv21ToBitmap(this, yuvArray, width, height);
//        // 将 uv 平面数据合并为一个完整的 nv21 数据展示出来
//        byte[] yuvArray2 = MyYuvUtils.mergeY420SPArray(false, yuvSpliteArray[1], width, height);
//        Bitmap uvBitmap = MyYuvUtils.nv21ToBitmap(this, yuvArray2, width, height);
//        viewDataBinding.mainNv21YImage.setImageBitmap(yBitmap);
//        viewDataBinding.mainNv21VuImage.setImageBitmap(uvBitmap);

//        byte[][] yuvSpliteArray = MyYuvUtils.spliteNV12ToSinglePlane(argb, width, height);
//        // 将 y 平面数据合并为一个完整的 nv21 数据展示出来
//        byte[] yuvArray = MyYuvUtils.mergeNV12Array(MyYuvUtils.YUV_PLANE_Y, yuvSpliteArray[0], width, height);
//        Bitmap yBitmap = MyYuvUtils.nv21ToBitmap(this, yuvArray, width, height);
//        // 将 uv 平面数据合并为一个完整的 nv21 数据展示出来
//        byte[] yuvArray2 = MyYuvUtils.mergeNV12Array(MyYuvUtils.YUV_PLANE_U, yuvSpliteArray[2], width, height);
//        Bitmap uvBitmap = MyYuvUtils.nv21ToBitmap(this, yuvArray2, width, height);
//        viewDataBinding.mainNv21YImage.setImageBitmap(yBitmap);
//        viewDataBinding.mainNv21VuImage.setImageBitmap(uvBitmap);
    }

    /**
     * 测试nv21格式的数据旋转，数据来源为bitmap
     */
    private void testYuvRotate(int rotate, boolean flip) {
        Bitmap bitmap = BitmapFactory.decodeFile(mBitmapFile.getAbsolutePath());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int byteCount = bitmap.getByteCount();
        ByteBuffer buf = ByteBuffer.allocate(byteCount);
        bitmap.copyPixelsToBuffer(buf);
        byte[] argb = buf.array();
        boolean result = mNativeVideoEncodeHelper.ARGBToNV21(argb, width, height, 4);
        if (!result) {
            throw new RuntimeException("ARGBToNV21 failed!");
        }

        boolean rotateResult = mNativeVideoEncodeHelper.yuvVideoDataRotate(NativeVideoEncodeHelper.TTT_FORMAT_NV21, argb, width, height, rotate, flip);
        if (!rotateResult) {
            throw new RuntimeException("videoDataRotate failed!");
        }

        Bitmap desBitmap;
        if (rotate == 90 || rotate == 270) {
            desBitmap = MyYuvUtils.nv21ToBitmap(this, argb, height, width);
        } else {
            desBitmap = MyYuvUtils.nv21ToBitmap(this, argb, width, height);
        }
        viewDataBinding.mainNv21RotateFlip.setImageBitmap(desBitmap);
    }

    private void initJni() {
        mNativeVideoEncodeHelper = new NativeVideoEncodeHelper(0);
    }
}
