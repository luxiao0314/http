package com.plugin.gradle.lucio.core.http;

import android.support.v4.util.LruCache;
import com.plugin.gradle.lucio.core.utils.ContextUtil;
import com.plugin.gradle.lucio.core.utils.Preconditions;
import com.plugin.gradle.lucio.core.utils.StringUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.plugin.gradle.lucio.core.http.Request.METHOD_GET;
import static com.plugin.gradle.lucio.core.http.Request.METHOD_POST;

/**
 * @Description
 * @Author luxiao
 * @Date 2019/4/24 5:59 PM
 * @Version
 */
public class RealCall implements Call, Runnable {

    private Request request;
    private RestClient restClient;
    private static ThreadPoolExecutor executorService;
    private static LruCache<String, Response> reqCache;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    public RealCall(Request request) {
        this.request = request;
    }

    @Override
    public void enqueue() {
        executorService().execute(this);
    }

    @Override
    public Response execute() {
        return doRequest();
    }

    @Override
    public void cancel() {
        if (restClient != null && request != null) {
            restClient.cleanUp();
            request.cancel();
        }
    }

    @Override
    public void run() {
        if (request.isCanceled()) {
            request.finish();
            return;
        }
        Response response = doRequest();
        if (response != null) {
            request.deliveryResponse(response); // 分发请求结果
        }
    }

    private Response doRequest() {
        //如果没有权限，则不需要申请网络
        if (!DiskLruImageCache.checkPermission(ContextUtil.getAppContext(), "android.permission.INTERNET")) {
            return null;
        }
        Response response = null;
        // 重试没必要从缓存中取数据
        if (!request.isRetry() && isUseCache(request)) {
            response = cache().get(getCacheKey(request));  // 从缓存中取数据
        } else {
            try {
                restClient = new RestClient(request.getUrl());
                // 从网络上获取数据
                switch (request.getMethod()) {
                    case METHOD_GET:
                        restClient.setRequestMethod(METHOD_GET);
                        response = restClient.getByte(request);
                        break;
                    case METHOD_POST:
                        restClient.setRequestMethod(METHOD_POST);
                        response = restClient.postByte(request);
                        break;
                    default:
                        break;
                }
            } catch (RestException e) {
                if (request.getRetryNum() > 0) {
                    request.setRetryNum(request.getRetryNum() - 1);
                    request.setRetry(true);
                    doRequest();
                } else if (request.getRetryNum() <= 0) {
                    request.deliveryError(new Response().throwable(e));
                }
                return null;
            } catch (SocketTimeoutException e) {
                if (request.getRetryNum() > 0) {
                    request.setRetryNum(request.getRetryNum() - 1);
                    request.setRetry(true);
                    doRequest();
                } else if (request.getRetryNum() <= 0) {
                    request.deliveryError(new Response().throwable(e));
                }
                return null;
            } catch (IOException e) {
                if (request.getRetryNum() > 0) {
                    request.setRetryNum(request.getRetryNum() - 1);
                    request.setRetry(true);
                    doRequest();
                } else if (request.getRetryNum() <= 0) {
                    request.deliveryError(new Response().throwable(e));
                }
                return null;
            } catch (Exception e) {
                request.deliveryError(new Response().throwable(e));
                restClient.cleanUp();
                return null;
            }

            // 如果该请求需要缓存,那么请求成功则缓存到mResponseCache中
            if (request.shouldCache() && Preconditions.isNotBlank(response)) {
                cache().put(getCacheKey(request), response);
            }
        }
        return response;
    }

    /**
     * 生成http缓存的key
     * req_id 不一样,导致缓存key不同
     *
     * @param request
     * @return
     */
    private String getCacheKey(Request request) {
        String key = null;
        if (Preconditions.isNotBlank(request.getUrl())) {
            if (Preconditions.isNotBlank(request.getBodyParams())) {
                // post请求,使用url+post body然后md5,生成key
                key = StringUtils.md5(request.getUrl() + request.getBodyParams().toString());
            } else {
                // get请求,使用url然后md5,生成key
                key = StringUtils.md5(request.getUrl());
            }
        }
        return key;
    }

    private boolean isUseCache(Request request) {
        return request.shouldCache() && Preconditions.isNotBlank(getCacheKey(request)) && cache().get(getCacheKey(request)) != null;
    }

    public static synchronized ThreadPoolExecutor executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(
                    Math.max(2, Math.min(CPU_COUNT - 1, 4)),
                    CPU_COUNT * 2 + 1,
                    60L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(),
                    Executors.defaultThreadFactory());   //线程创建工厂
        }
        return executorService;
    }

    private static synchronized LruCache<String, Response> cache() {
        if (reqCache == null) {
            reqCache = new LruCache<String, Response>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8)) {
                @Override
                protected int sizeOf(String key, Response response) {
                    return response.body().byteArray().length / 1024;
                }
            };
        }
        return reqCache;
    }

}
