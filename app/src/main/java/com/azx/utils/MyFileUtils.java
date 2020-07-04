package com.azx.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyFileUtils {

    private static final String TAG = "MyFileUtils";

    public static File getExternalStorageFile(String filePath) {
        if (!checkExternalStorageState()) {
            Log.e(TAG, "external storage not exist!");
            return null;
        }

        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File temp = new File(externalStorageDirectory, filePath);
        if (!temp.exists()) {
            Log.e(TAG, "target file not exist! " + filePath);
            return null;
        }
        return temp;
    }

    public static String[] loadImagesFromDCIM() {

        File externalFilesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (externalFilesDir == null) {
            Log.e("MyFileUtils", "File is null!");
            return null;
        }

        String[] files = externalFilesDir.list();
        if (files == null || files.length <= 0) {
            Log.e("MyFileUtils", "Image file size is zero!");
            return null;
        }

        for (String file : files) {
            Log.d("MyFileUtils", "iterator image file : " + file);
        }

        return files;
    }

    public static List<FileImageBean> loadImagesFromPath(String path) {
        if (TextUtils.isEmpty(path)) {
            Log.e("MyFileUtils", "path is empty!");
            return null;
        }

        File rootFile = new File(path);
        String[] files = rootFile.list();
        if (files == null || files.length <= 0) {
            Log.e("MyFileUtils", "Image file size is zero!");
            return null;
        }

        Log.d("MyFileUtils", "loadImagesFromPath -> path : " + path);
        List<FileImageBean> beans = new ArrayList<>();
        for (String file : files) {
            String targetFile = path + File.separator + file;
            File tempFile = new File(targetFile);
//            Log.d("MyFileUtils", "loadImagesFromPath -> tempFile : " + tempFile.getAbsolutePath() + " | " + tempFile.isDirectory());
            if (tempFile.isDirectory()) {
                List<FileImageBean> fileImageBeans = loadImagesFromPath(targetFile);
                if (fileImageBeans != null && fileImageBeans.size() > 0) {
                    beans.addAll(fileImageBeans);
                }
            } else {
                FileImageBean bean = new FileImageBean();
                bean.fileName = tempFile.getName();
                bean.filePath = targetFile;
                bean.fileSize = tempFile.length();
                beans.add(bean);
            }
        }
        return beans;
    }

    private static boolean checkExternalStorageState() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static class FileImageBean {

        String fileName;
        String filePath;
        long fileSize;

        @Override
        public String toString() {
            return "FileImageBean{" +
                    "fileName='" + fileName + '\'' +
                    ", filePath='" + filePath + '\'' +
                    ", fileSize=" + fileSize +
                    '}';
        }
    }
}
