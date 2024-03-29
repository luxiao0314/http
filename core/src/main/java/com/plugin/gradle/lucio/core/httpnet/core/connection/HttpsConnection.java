/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/huanghaibin_dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.plugin.gradle.lucio.core.httpnet.core.connection;


import com.plugin.gradle.lucio.core.httpnet.HttpNetClient;
import com.plugin.gradle.lucio.core.httpnet.builder.Request;
import com.plugin.gradle.lucio.core.httpnet.core.Response;
import com.plugin.gradle.lucio.core.httpnet.core.call.Callback;
import com.plugin.gradle.lucio.core.httpnet.core.call.InterceptListener;
import com.plugin.gradle.lucio.core.httpnet.core.io.HttpContent;
import com.plugin.gradle.lucio.core.httpnet.core.io.IO;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URLConnection;

/**
 * https连接
 */
@SuppressWarnings("unused")
public class HttpsConnection extends Connection {
    private HttpsURLConnection mHttpsUrlConnection;
    private InterceptListener mListener;

    public HttpsConnection(HttpNetClient client, Request request) {
        super(client, request);
    }

    public HttpsConnection(HttpNetClient client, Request request, InterceptListener listener) {
        super(client, request);
        this.mListener = listener;
    }

    @Override
    void connect(URLConnection connection, String method) throws IOException {
        mHttpsUrlConnection = (HttpsURLConnection) connection;
        mHttpsUrlConnection.setSSLSocketFactory(mClient.getSslSocketFactory());
        mHttpsUrlConnection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        mHttpsUrlConnection.setRequestMethod(method);
        mHttpsUrlConnection.setUseCaches(true);
        mHttpsUrlConnection.setConnectTimeout(mRequest.timeout());
        mHttpsUrlConnection.setRequestProperty("Accept-Language", "zh-CN");
        mHttpsUrlConnection.setRequestProperty("Charset", mRequest.encode());
        mHttpsUrlConnection.setRequestProperty("Connection", "Keep-Alive");
    }

    @Override
    void post() throws IOException {
        mHttpsUrlConnection.setDoOutput(true);
        mHttpsUrlConnection.setRequestProperty("Content-Type", getContentType(mRequest.params()));
        mOutputStream = new DataOutputStream(mHttpsUrlConnection.getOutputStream());
        HttpContent body = mRequest.content();
        if (body != null) {
            body.setOutputStream(mOutputStream);
            body.doOutput(mListener);
        }
    }

    @Override
    void get() throws IOException {

    }

    @Override
    void put() throws IOException {
        post();
    }

    @Override
    void delete() throws IOException {

    }

    @Override
    void patch() throws IOException {

    }

    @Override
    void onResponse(Callback callBack) throws IOException {
        callBack.onResponse(
                new Response(mHttpsUrlConnection.getResponseCode(),
                        mInputStream,
                        mHttpsUrlConnection.getHeaderFields(),
                        mRequest.encode(), mHttpsUrlConnection.getContentLength()));
    }

    @Override
    Response getResponse() throws IOException {
        return new Response(mHttpsUrlConnection.getResponseCode(),
                mInputStream,
                mHttpsUrlConnection.getHeaderFields(),
                mRequest.encode(), mHttpsUrlConnection.getContentLength());
    }

    @Override
    public void disconnect() {
        if (mHttpsUrlConnection != null)
            mHttpsUrlConnection.disconnect();
    }

    @Override
    void finish() {
        IO.close(mOutputStream, mInputStream);
    }
}
