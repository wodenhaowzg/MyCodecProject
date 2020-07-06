package com.azx.myapplication;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.azx.myapplication.databinding.ActivityMainBinding;
import com.azx.utils.MyBitmapUtils;
import com.azx.utils.MyYuvUtils;
import com.wushuangtech.api.NativeVideoEncodeHelper;

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
        // 手机的根目录下需要有此路径的文件，才能成功执行
        Bitmap[] bitmaps = MyBitmapUtils.spliteBitmapColor("1/2.jpg");
//        Bitmap bitmap1 = testRgbRotate(bitmaps[0]);
        viewDataBinding.mainSrcImage.setImageBitmap(bitmaps[0]);
        viewDataBinding.mainRedImage.setImageBitmap(bitmaps[1]);
        viewDataBinding.mainGreenImage.setImageBitmap(bitmaps[2]);
        viewDataBinding.mainBlueImage.setImageBitmap(bitmaps[3]);
        // 测试yuv数据分离
//        Bitmap[] bitmaps2 = testYuvAndSplite(bitmaps[0]);
//        viewDataBinding.mainNv21YImage.setImageBitmap(bitmaps2[0]);
//        viewDataBinding.mainNv21VuImage.setImageBitmap(bitmaps2[1]);

        Bitmap bitmap = testYuvRotate(bitmaps[0], 90);
        viewDataBinding.mainNv21YImage.setImageBitmap(bitmap);
    }

    private Bitmap testRgbRotate(Bitmap bitmap) {
        ByteBuffer buf = ByteBuffer.allocate(bitmap.getByteCount());
        buf.position(0);
        bitmap.copyPixelsToBuffer(buf);
        boolean rotate = mNativeVideoEncodeHelper.rgbVideoDataRotate(NativeVideoEncodeHelper.TTT_FORMAT_ARGB, buf.array(), bitmap.getWidth(), bitmap.getHeight(), 4, 270, false);
        if (!rotate) {
            throw new RuntimeException("videoDataRotate failed!");
        }
        // 这里需要重置position
        buf.position(0);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        bitmap1.copyPixelsFromBuffer(buf);
        return bitmap1;
    }

    private Bitmap[] testYuvAndSplite(Bitmap bitmap) {
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

        Bitmap[] bitmaps = new Bitmap[2];
        byte[][] yuvSpliteArray = MyYuvUtils.spliteNV21Array(nv21Array, width, height);
        byte[] yuvArray = MyYuvUtils.mergeNV21Array(0, yuvSpliteArray[0], width, height);
        Bitmap temp = MyYuvUtils.nv21ToBitmap(this, yuvArray, width, height);
        bitmaps[0] = temp;
        byte[] yuvArray2 = MyYuvUtils.mergeNV21Array(1, yuvSpliteArray[1], width, height);
        Bitmap temp2 = MyYuvUtils.nv21ToBitmap(this, yuvArray2, width, height);
        bitmaps[1] = temp2;
        return bitmaps;
    }

    private Bitmap testYuvRotate(Bitmap bitmap, int rotate) {
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

        boolean rotateResult = mNativeVideoEncodeHelper.yuvVideoDataRotate(NativeVideoEncodeHelper.TTT_FORMAT_NV21, nv21Array, width, height, rotate, true);
        if (!rotateResult) {
            throw new RuntimeException("videoDataRotate failed!");
        }

        Bitmap desBitmap;
        if (rotate == 90 || rotate == 270) {
            desBitmap = MyYuvUtils.nv21ToBitmap(this, nv21Array, height, width);
        } else {
            desBitmap = MyYuvUtils.nv21ToBitmap(this, nv21Array, width, width);
        }
        return desBitmap;
    }

    private void initJni() {
        mNativeVideoEncodeHelper = new NativeVideoEncodeHelper(0);
    }
}
