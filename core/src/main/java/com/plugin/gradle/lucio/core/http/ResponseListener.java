package com.plugin.gradle.lucio.core.http;

public interface ResponseListener<T> extends Converter<T> {

    void onStart(Request request);

    void onSuccess(T content);

    /**
     * 采用Response,可以接收服务器错误时返回的code及message
     *
     * @param response
     */
    void onFail(Response response);
}