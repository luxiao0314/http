/*
 * Copyright © 2018 Zhenjie Yan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.plugin.gradle.lucio.core.http;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

/**
 * @Description
 * @Author luxiao
 * @Date 2019/4/25 7:23 PM
 * @Version
 */
public final class Response implements Closeable {

    private int code;
    private ResponseBody body;
    private Throwable throwable;
    private Request request;

    public Response code(int code) {
        this.code = code;
        return this;
    }

    public Response body(ResponseBody body) {
        this.body = body;
        return this;
    }

    public Response request(Request request) {
        this.request = request;
        return this;
    }

    public int code() {
        return code;
    }

    public Request request() {
        return this.request;
    }

    public ResponseBody body() {
        return body;
    }

    public Throwable throwable() {
        return throwable;
    }

    public Response throwable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    @Override
    public void close() {
        body.close(body);
    }

    //是否重定向
    public boolean isRedirect() {
        switch (code) {
            case 300:
            case 301:
            case 302:
            case 303:
            case 307:
            case 308:
                return true;
            case 304:
            case 305:
            case 306:
            default:
                return false;
        }
    }

    public static class ResponseBody implements Closeable {

        private InputStream inputStream;
        private long contentLength;
        private LinkedHashMap<String, String> headers;
        private String chartset;
        private byte[] byteArray;
        public String data;

        public ResponseBody(String chartset, InputStream inputStream, long contentLength, LinkedHashMap<String, String> headers) {
            this.headers = headers;
            this.chartset = chartset;
            this.inputStream = inputStream;
            this.contentLength = contentLength;
        }

        public long length() {
            return contentLength;
        }

        public String string() {
            if (data == null) toByteArray();
            return data;
        }

        public byte[] byteArray() {
            if (byteArray == null) toByteArray();
            return byteArray;
        }

        public LinkedHashMap<String, String> headers() {
            return headers;
        }

        public InputStream stream() {
            return inputStream;
        }

        @Override
        public void close() {
            close(inputStream);
        }

        private void toByteArray() {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int b;
                while ((b = inputStream.read()) != -1) {
                    os.write(b);
                }
                data = new String(os.toByteArray(), chartset);
                byteArray = os.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(inputStream);
            }
        }

        private void close(Closeable... closeables) {
            for (Closeable cb : closeables) {
                try {
                    if (null == cb) {
                        continue;
                    }
                    cb.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}