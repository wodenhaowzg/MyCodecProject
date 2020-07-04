package com.azx.myapplication;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.azx.myapplication.databinding.ActivityMainBinding;
import com.azx.utils.MyBitmapUtils;
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
        viewDataBinding.mainSrcImage.setImageBitmap(bitmaps[0]);
        viewDataBinding.mainRedImage.setImageBitmap(bitmaps[1]);
        viewDataBinding.mainGreenImage.setImageBitmap(bitmaps[2]);
        viewDataBinding.mainBlueImage.setImageBitmap(bitmaps[3]);
        // 旋转bitmap
        libyuvRotate(bitmaps[0], 90);
    }

    /**
     * 测试通过libyuv旋转byte[]数据
     */
    private void libyuvRotate(Bitmap bitmap, int rotate) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int byteCount = bitmap.getByteCount();
        ByteBuffer buf = ByteBuffer.allocate(byteCount);
        bitmap.copyPixelsToBuffer(buf);
        byte[] srcArray = buf.array();
        srcArray = mNativeVideoEncodeHelper.videoDataRotate(NativeVideoEncodeHelper.TTT_FORMAT_ARGB, srcArray, width, height, 4, rotate);
        if (srcArray == null) {
            throw new RuntimeException("libyuvRotate failed!");
        }
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(srcArray));
    }

    private void initJni() {
        mNativeVideoEncodeHelper = new NativeVideoEncodeHelper(0);
    }
}
