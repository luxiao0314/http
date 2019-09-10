package com.plugin.gradle.lucio.core.http;

import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Description
 * @Author luxiao
 * @Date 2019/4/28 4:56 PM
 * @Version
 */
public abstract class SimpleResponseListener<T> implements ResponseListener<T> {

    @Override
    public void onStart(Request request) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public T convert(Response response) throws Throwable {
        int code = response.code();
        if (code >= 200 && code < 300) { // Http is successful.
            return converts(response);
        } else if (response.isRedirect()) { //重定向
            return (T) response.request().url(response.body().headers().get(Request.KEY_LOCATION)).execute();
        } else if (code >= 400 && code < 500) {
            throw new Exception("code：" + code + "，message：" + "未知错误");
        } else if (code >= 500) {
            throw new Exception("code：" + code + "，message：" + "服务器错误");
        }
        throw new Exception("code：" + code + "，message：" + "未知错误");
    }

    @SuppressWarnings("unchecked")
    private T converts(Response response) throws Throwable {
        String json = response.body().string();
        Log.i("HTTP RESPONSE", response.request().getMethod() + ":  " + response.request().getUrl() + "\n" + json);
        Type genType = getClass().getGenericSuperclass();
        assert genType != null;
        Type type = ((ParameterizedType) genType).getActualTypeArguments()[0];
        if (type instanceof Class) {
            if (((Class<?>) type).newInstance() instanceof Response) {
                return (T) response;
            }
            return new JsonConvert<T>(type).convert(response);
        }
        return null;
    }

    @Override
    public void onFail(Response response) {
        Log.e("HTTP ERROR", response.request().getMethod() + ":  " + response.request().getUrl() + "\n" + response.throwable().toString());
    }
}
