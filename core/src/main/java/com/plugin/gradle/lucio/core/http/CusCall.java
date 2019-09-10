package com.plugin.gradle.lucio.core.http;

import com.plugin.gradle.lucio.core.urlhttp.RealRequest;
import com.plugin.gradle.lucio.core.urlhttp.RealResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import static com.plugin.gradle.lucio.core.http.RealCall.executorService;

public class CusCall implements Call {

    private final Request request;

    public CusCall(Request request) {
        this.request = request;
    }

    @Override
    public Response execute() {
        return doRequest(request);
    }

    @Override
    public void enqueue() {
        executorService().execute(new Runnable() {
            @Override
            public void run() {
                if (request.isCanceled()) {
                    request.finish();
                    return;
                }
                Response response = doRequest(request);
                if (response != null) {
                    request.deliveryResponse(response); // 分发请求结果
                }
            }
        });
    }

    @Override
    public void cancel() {

    }

    private Response doRequest(Request request) {
        RealRequest realRequest = new RealRequest();
        RealResponse realResponse = null;
        // 从网络上获取数据
        switch (request.getMethod()) {
            case Request.METHOD_GET:
                realResponse = realRequest.getData(request.getUrl(), request.getHeaders());
                break;
            case Request.METHOD_POST:
                realResponse = realRequest.postData(request.getUrl(), request.getBodyParams().toString(), request.getContentType(), request.getHeaders());
                break;
            default:
                break;
        }
        if (realResponse.code == HttpURLConnection.HTTP_OK) {
            return new Response()
                    .request(request)
                    .code(realResponse.code)
                    .body(new Response.ResponseBody("UTF-8", realResponse.inputStream, realResponse.contentLength, null));
        } else {
            String errorMessage;
            if (realResponse.inputStream != null) {
                errorMessage = getRetString(realResponse.inputStream);
            } else if (realResponse.errorStream != null) {
                errorMessage = getRetString(realResponse.errorStream);
            } else if (realResponse.exception != null) {
                errorMessage = realResponse.exception.getMessage();
            } else {
                errorMessage = "";
            }
            request.deliveryError(new Response().throwable(new Throwable(errorMessage)));
            return null;
        }
    }

    private static String getRetString(InputStream is) {
        String buf;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            buf = sb.toString();
            return buf;

        } catch (Exception e) {
            return null;
        }
    }
}
