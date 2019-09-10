package com.plugin.gradle.lucio.core.http;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Description
 * @Author luxiao
 * @Date 2019-07-23 15:10
 * @Version
 */
public abstract class DownloadResponseListener<T> implements ResponseListener<T> {

    private final String mDestFileDir;
    private final String mdestFileName;

    @Override
    public void onStart(Request request) {

    }

    /**
     * @param destFileDir:文件目录
     * @param destFileName：文件名
     */
    public DownloadResponseListener(String destFileDir, String destFileName) {
        mDestFileDir = destFileDir;
        mdestFileName = destFileName;
    }

    @Override
    public T convert(Response response) throws Throwable {
        InputStream is = null;
        byte[] buf = new byte[1024 * 8];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = response.body().stream();
            final long total = response.body().length();
            long sum = 0;
            File dir = new File(mDestFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, mdestFileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                onProgress(finalSum * 100.0f / total, total);
            }
            fos.flush();
            is.close();
            return (T) file;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                assert fos != null;
                fos.close();
                if (is != null) is.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void onFail(Response response) {
        Log.e("HTTP ERROR", response.throwable().toString());
    }

    protected void onProgress(float v, long total) {
        Log.d("onProgress", String.valueOf(v));
    }
}
