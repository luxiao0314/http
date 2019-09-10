package com.plugin.gradle.lucio.core.http;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.plugin.gradle.lucio.core.utils.JSONUtils;
import com.plugin.gradle.lucio.core.utils.Preconditions;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description
 * @Author luxiao
 * @Date 2019/4/25 10:34 AM
 * @Version
 */
public class Request<T> implements Serializable {

    public static final String KEY_LOCATION = "Location";

    public static final String CHARSET_UTF8 = "UTF-8";

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String HEADER_ACCEPT = "Accept";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String METHOD_GET = "GET";

    public static final String METHOD_POST = "POST";

    public static final String PARAM_CHARSET = "charset";

    public final static int DEFAULT_READ_TIMEOUT = 10000;

    public final static int DEFAULT_CONNECTION_TIMEOUT = 30000;

    public static int DEFAULT_RETRY_NUM = 2;

    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    public static final String BOUNDARY = java.util.UUID.randomUUID().toString();

    public static final String DATA_TAG = "--";

    public static final String END = "\r\n";

    private String httpMethod = METHOD_GET;

    private LinkedHashMap<String, String> headers = new LinkedHashMap<>();

    private Params params = new Params();

    private boolean shouldCache = false;

    private int retryNum = DEFAULT_RETRY_NUM;   //request重试次数,0为1次。2为3次

    private boolean isRetry = false;    //request是否是重试

    private int connctTimeout = DEFAULT_CONNECTION_TIMEOUT; // request超时时间,默认是30秒

    private int readTimeout = DEFAULT_READ_TIMEOUT;

    private String stringUrl;

    private String safeStringUrl;

    private boolean isCancel = false;

    private ResponseListener<T> responseListener;

    private Converter<T> converter;

    private Call call;

    private static Handler mResponseHandler = new Handler(Looper.getMainLooper());

    protected Request(String method, String url) {
        this.url(url);
        this.method(method);
    }

    public Request<T> method(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public Request<T> url(String url) {
        this.stringUrl = url;
        return this;
    }

    public Request<T> addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public Request<T> fileParam(String name, File file) {
        this.params.putFile(name, file);
        return this;
    }

    public Request<T> fileParam(String name, String filename) {
        this.params.putFile(name, filename);
        return this;
    }

    public Request<T> param(String name, String value) {
        this.params.put(name, value);
        return this;
    }

    public Request<T> param(Map params) {
        this.params.put(params);
        return this;
    }

    public Request<T> param(JSONObject params) {
        this.params.put(params);
        return this;
    }

    public Request<T> setShouldCache(boolean shouldCache) {
        this.shouldCache = shouldCache;
        return this;
    }

    public Request<T> setRetryNum(int retryNum) {
        this.setRetry(true);
        this.retryNum = retryNum;
        return this;
    }

    public Request<T> setRetry(boolean retry) {
        this.isRetry = retry;
        return this;
    }

    public Request<T> setConnectTimeoutMillis(int timeoutMillis) {
        this.connctTimeout = timeoutMillis;
        return this;
    }

    public Request<T> setReadTimeoutMillis(int timeoutMillis) {
        this.readTimeout = timeoutMillis;
        return this;
    }

    public Request<T> converter(Converter<T> converter) {
        this.converter = converter;
        return this;
    }

    public Request<T> call(Call call) {
        this.call = call;
        return this;
    }

    public Request<T> cancel() {
        this.isCancel = true;
        return this;
    }

    protected String getContentType() {
        return params != null ? (params.getMultiParams() != null ? "multipart/form-data; boundary=\"" + BOUNDARY + "\"" : "application/x-www-form-urlencoded") : "application/json; charset=utf-8";
    }

    protected String getMethod() {
        return httpMethod;
    }

    protected LinkedHashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * get请求，将键值对凭接到url上
     */
    protected String getUrl(String path, JSONObject jsonParams) {
        Map<String, String> paramsMap = JSONUtils.convertToMap(jsonParams);
        if (paramsMap != null) {
            StringBuilder pathBuilder = new StringBuilder(path + "?");
            for (String key : paramsMap.keySet()) {
                pathBuilder.append(key).append("=").append(paramsMap.get(key)).append("&");
            }
            path = pathBuilder.toString();
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    protected JSONObject getBodyParams() {
        return params.getJsonParams();
    }

    protected Params getParams() {
        return params;
    }

    protected boolean shouldCache() {
        return shouldCache;
    }

    protected boolean isCanceled() {
        return isCancel;
    }

    protected int getRetryNum() {
        return retryNum;
    }

    protected boolean isRetry() {
        return isRetry;
    }

    protected int getConnectTimeoutMillis() {
        return connctTimeout;
    }

    protected int getReadTimeoutMillis() {
        return readTimeout;
    }

    protected Converter<T> getConverter() {
        if (converter == null) converter = responseListener;
        return converter;
    }

    protected String getUrl() {
        return getSafeStringUrl();
    }

    private String getSafeStringUrl() {
        if (METHOD_GET.equals(getMethod())) {
            stringUrl = getUrl(stringUrl, params.getJsonParams());
        }
        if (TextUtils.isEmpty(safeStringUrl)) {
            String unsafeStringUrl = stringUrl;
            safeStringUrl = Uri.encode(unsafeStringUrl, ALLOWED_URI_CHARS);
        }
        return safeStringUrl;
    }

    protected byte[] getBody() {
        return params.getJsonParams() != null ? encodeParameters(params.getJsonParams()) : null;
    }

    private byte[] encodeParameters(JSONObject params) {
        if (Preconditions.isBlank(params)) return null;
        try {
            return params.toString().getBytes(CHARSET_UTF8);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + CHARSET_UTF8, uee);
        }
    }

    void deliveryResponse(final Response response) {
        try {
            final T convert = this.getConverter().convert(response);
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (responseListener != null && convert != null) {
                        responseListener.onSuccess(convert);
                    } else {
                        deliveryError(response.throwable(new RuntimeException("responseListener or convert is null")));
                    }
                }
            });
        } catch (Throwable throwable) {
            deliveryError(response.throwable(throwable));
        }
    }

    void deliveryError(final Response response) {
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (responseListener != null) {
                    responseListener.onFail(response);
                }
            }
        });
    }

    void finish() {
        responseListener = null;
    }

    private Call adapt() {
        if (call == null) {
            call = new RealCall(this);
        }
        return call;
    }

    public void enqueue(ResponseListener<T> listener) {
        this.responseListener = listener;
        this.responseListener.onStart(this);
        Call adapt = this.adapt();
        adapt.enqueue();
    }

    public Response execute() throws IOException {
        Call adapt = this.adapt();
        return adapt.execute();
    }

    public static <T> Request<T> get(String url) {
        return new Request<>(METHOD_GET, url);
    }

    public static <T> Request<T> post(String url) {
        return new Request<>(METHOD_POST, url);
    }
}
