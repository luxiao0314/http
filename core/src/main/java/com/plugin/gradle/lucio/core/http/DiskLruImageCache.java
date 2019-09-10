package com.plugin.gradle.lucio.core.http;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;

import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Tony Shen on 2016/11/13.
 */

public class DiskLruImageCache {

    private static final String TAG = "DiskLruImageCache";
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static int mCompressQuality = 70;
    private final String FILE_SAVE_PATH = "cpc_sdk_image";
    private DiskLruCache mDiskCache;

    public DiskLruImageCache(Context context) {
        this(context, mCompressQuality);
    }

    public DiskLruImageCache(Context context, int quality) {
        if (context == null)
            return;
        try {
            if (mDiskCache == null) {
                File cacheDir = getDiskCacheDir(context, FILE_SAVE_PATH);
                if (cacheDir != null && cacheDir.exists() && cacheDir.isDirectory()) {
                    mDiskCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, DISK_CACHE_SIZE);
                    mCompressQuality = quality;
                } else {
                    mDiskCache = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查权限是否开启
     *
     * @param permission
     * @return true or false
     */
    public static boolean checkPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            PackageManager localPackageManager = context.getApplicationContext().getPackageManager();
            return localPackageManager.checkPermission(permission, context.getApplicationContext().getPackageName()) == PackageManager.PERMISSION_GRANTED;
        } else {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private static boolean hasExternalCache(Context context) {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && checkPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE")
                && context.getExternalCacheDir() != null;
    }

    public static File getDiskCacheDir(final Context context, String fileDir) {

        File cacheDirectory;
        if (hasExternalCache(context)) {
            cacheDirectory = context.getExternalCacheDir();
        } else {
            cacheDirectory = context.getCacheDir();
        }
        if (cacheDirectory == null) {
            cacheDirectory = context.getCacheDir();
            if (cacheDirectory == null) {
                return null;
            }
        }
        if (fileDir != null) {
            File file = new File(cacheDirectory, fileDir);
            if (!file.exists() && !file.mkdir()) {
                return cacheDirectory;
            } else {
                return file;
            }
        }
        return cacheDirectory;
    }



    private int getAppVersion(Context context) {
//        int result = 1;
//        try {
//            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            if (info != null) {
//                result = info.versionCode;
//            }
//        } catch (PackageManager.NameNotFoundException ignored) {
//        }
        return 1;
    }

    public void put(final String key, final Bitmap data) {

        if (data == null || mDiskCache == null)
            return;

        OutputStream out = null;
        String ekey = md5(key);
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get(key);
            if (snapshot == null) {
                DiskLruCache.Editor editor = mDiskCache.edit(ekey);
                if (editor == null)
                    return;
                out = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
                Bitmap.CompressFormat format;
                if (key.endsWith("png") || key.endsWith("PNG")) {
                    format = Bitmap.CompressFormat.PNG;
                } else if (Build.VERSION.SDK_INT >= 14 && key.endsWith("webp")) {
                    format = Bitmap.CompressFormat.WEBP;
                } else {
                    format = Bitmap.CompressFormat.PNG; //存为jpg会导致bitmap有黑色背景
                }
                data.compress(format, mCompressQuality, out);
                editor.commit();
                mDiskCache.flush();
            } else {
                snapshot.getInputStream(0).close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * 安全关闭io流
     *
     * @param closeable
     */
    public void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getBitmap(final String key) {

        if (mDiskCache == null)
            return null;

        DiskLruCache.Snapshot snapShot = null;

        try {
            snapShot = mDiskCache.get(md5(key));
        } catch (IOException e) {
            return null;
        }
        if (snapShot != null) {
            InputStream is = snapShot.getInputStream(0);
            if (is != null) {
                BufferedInputStream buffIn = new BufferedInputStream(is, IO_BUFFER_SIZE);
                return BitmapFactory.decodeStream(buffIn);
            }
        }

        return null;
    }

    public void clearCache() {
        if (mDiskCache == null)
            return;
        try {
            mDiskCache.delete();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * MD5 encrypt做过处理，取的是中间16位。
     */
    public static String md5(String str) {
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(str.getBytes(Charset.defaultCharset()));
            byte[] arrayOfByte = localMessageDigest.digest();
            StringBuilder stringBuffer = new StringBuilder();
            for (byte anArrayOfByte : arrayOfByte) {
                int j = 0xFF & anArrayOfByte;
                if (j < 16)
                    stringBuffer.append("0");
                stringBuffer.append(Integer.toHexString(j));
            }
            return stringBuffer.toString().toLowerCase().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
