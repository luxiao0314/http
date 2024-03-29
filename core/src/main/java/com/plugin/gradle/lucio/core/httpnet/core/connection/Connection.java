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

import android.text.TextUtils;
import com.plugin.gradle.lucio.core.httpnet.HttpNetClient;
import com.plugin.gradle.lucio.core.httpnet.builder.Headers;
import com.plugin.gradle.lucio.core.httpnet.builder.Request;
import com.plugin.gradle.lucio.core.httpnet.builder.RequestParams;
import com.plugin.gradle.lucio.core.httpnet.core.Response;
import com.plugin.gradle.lucio.core.httpnet.core.call.Callback;
import com.plugin.gradle.lucio.core.httpnet.core.io.HttpContent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 链接对象，使用策略模式
 */
public abstract class Connection {
    HttpNetClient mClient;
    Request mRequest;
    private URLConnection mUrlConnection;
    DataOutputStream mOutputStream;
    InputStream mInputStream;

    Connection(HttpNetClient client, Request request) {
        this.mClient = client;
        this.mRequest = request;
    }

    public void connect(Callback callBack) {
        try {
            doInit();
            onResponse(callBack);
        } catch (IOException e) {
            e.printStackTrace();
            callBack.onFailure(e);
        } finally {
            finish();
            disconnect();
        }
    }

    public Response connect() throws IOException {
        doInit();
        return getResponse();
    }

    private void doInit() throws IOException {
        Proxy proxy = TextUtils.isEmpty(mRequest.host()) ?
                mClient.getProxy() :
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(mRequest.host(), mRequest.port()));
        String method = mRequest.method().toUpperCase();
        URL url = new URL(getHttpUrl(mRequest));
        if (proxy == null) {
            mUrlConnection = url.openConnection();
        } else {
            mUrlConnection = url.openConnection(proxy);
        }
        initHeaders();
        connect(mUrlConnection, method);
        switch (method) {
            case "GET":
                get();
                break;
            case "POST":
                post();
                break;
            case "DELETE":
                delete();
                break;
            case "PUT":
                put();
                break;
            case "PATCH":
                patch();
                break;
        }
        mInputStream = mUrlConnection.getInputStream();
    }

    abstract void connect(URLConnection connection, String method) throws IOException;

    abstract void post() throws IOException;

    abstract void get() throws IOException;

    abstract void put() throws IOException;

    abstract void delete() throws IOException;

    abstract void patch() throws IOException;

    abstract void onResponse(Callback callBack) throws IOException;

    abstract Response getResponse() throws IOException;

    public abstract void disconnect();

    abstract void finish();

    /**
     * 初始化头部
     */
    private void initHeaders() {
        Headers headers = mRequest.headers();
        if (headers != null) {
            Map<String, List<String>> maps = headers.getHeaders();
            if (maps != null) {
                Set<String> sets = maps.keySet();
                for (String key : sets) {
                    for (String value : maps.get(key)) {
                        mUrlConnection.addRequestProperty(key, value);
                    }
                }
            }
        }
    }

    /**
     * 获取Content-Type
     *
     * @return "multipart/form-data || application/json; charset=utf-8 || application/x-www-form-urlencoded
     */
    String getContentType(RequestParams params) {
        return params != null ? (params.getMultiParams() != null ? "multipart/form-data; boundary=\"" + HttpContent.BOUNDARY + "\"" : "application/x-www-form-urlencoded") : "application/json; charset=utf-8";
    }


    private String getHttpUrl(Request request) {
        String method = request.method().toUpperCase();
        String httpUrl = request.url();
        if ("GET".equals(method) || "DELETE".equals(method) || "PATCH".equals(method)) {
            RequestParams params = request.params();
            if (params != null
                    && params.getTextParams() != null
                    && params.getTextParams().size() != 0) {
                String paramsStr = request.content().intoString();
                httpUrl = httpUrl + (httpUrl.contains("?") ? paramsStr : "?" + paramsStr);
            }
        }
        return httpUrl;
    }
}
