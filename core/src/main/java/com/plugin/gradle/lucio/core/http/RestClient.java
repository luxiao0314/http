package com.plugin.gradle.lucio.core.http;

import android.annotation.SuppressLint;
import com.plugin.gradle.lucio.core.utils.Preconditions;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * @Description
 * @Author luxiao
 * @Date 2019/4/25 7:23 PM
 * @Version
 */
class RestClient {

    private static SSLSocketFactory TRUSTED_FACTORY;
    private static HostnameVerifier TRUSTED_VERIFIER;
    private HttpURLConnection connection = null;
    private String requestMethod;
    private URL url;

    RestClient(String url) throws RestException {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new RestException(e);
        }
    }

    void setRequestMethod(String method) {
        this.requestMethod = method;
        //支持https
        if (Preconditions.isNotBlank(url) && url.toString().startsWith("https")) {
            //Accept all certificates
            trustAllCerts();
            //Accept all hostnames
            trustAllHosts();
        }
    }

    void cleanUp() {
        if (connection != null) connection.disconnect();
    }

    @SuppressWarnings("unchecked")
    Response getByte(Request request) throws Exception {
        if (getConnection() == null) return null;
        addHeaders(request.getHeaders())
                .readTimeout(request.getReadTimeoutMillis())
                .connectTimeout(request.getConnectTimeoutMillis());
        return byteBody(request);
    }

    @SuppressWarnings("unchecked")
    Response postByte(Request request) throws Exception {
        if (getConnection() == null) return null;
        addHeaders(request.getHeaders())
                .readTimeout(request.getReadTimeoutMillis())
                .connectTimeout(request.getConnectTimeoutMillis())
                .acceptJson()
                .contentType(request.getContentType(), null);
        if (request.getParams().getMultiParams() != null && request.getParams().getMultiParams().size() > 0) {
            outputFileFormData(request);
        }
        if (request.getBody() != null) {
            send(request.getBody());
        }
        return byteBody(request);
    }

    private RestClient readTimeout(int timeout) {
        getConnection().setReadTimeout(timeout);
        return this;
    }

    private RestClient connectTimeout(int timeout) {
        getConnection().setConnectTimeout(timeout);
        return this;
    }

    private HttpURLConnection createConnection() {
        try {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setReadTimeout(Request.DEFAULT_READ_TIMEOUT);
            connection.setConnectTimeout(Request.DEFAULT_CONNECTION_TIMEOUT);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            return connection;
        } catch (IOException e) {
            throw new RestException(e);
        }
    }

    private HttpURLConnection getConnection() {
        if (connection == null) {
            connection = createConnection();
        }
        return connection;
    }

    /**
     * 配置HTTPS连接，信任所有证书
     *
     * @return RestClient
     * @throws RestException
     */
    private void trustAllCerts() throws RestException {
        final HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(getTrustedFactory());
        }
    }

    /**
     * 配置HTTPS连接，信任所有host
     *
     * @return RestClient
     */
    private void trustAllHosts() {
        final HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setHostnameVerifier(getTrustedVerifier());
        }
    }

    /**
     * 返回指定charset的http response
     *
     * @param request
     * @return byte[]
     */
    @SuppressWarnings("unchecked")
    private Response byteBody(Request request) throws Exception {
        if (getConnection() == null) {
            return null;
        }
        InputStream stream;
        if (getConnection().getResponseCode() < HTTP_BAD_REQUEST) {
            stream = getConnection().getInputStream();
        } else {
            stream = getConnection().getErrorStream();
            if (stream == null) {
                stream = getConnection().getInputStream();
            }
        }
        if (stream == null) {
            return null;
        }
        return new Response()
                .request(request)
                .code(getConnection().getResponseCode())
                .body(new Response.ResponseBody(charset(), stream, getConnection().getContentLength(), parseResponseHeaders(getConnection().getHeaderFields())));
    }

    private LinkedHashMap<String, String> parseResponseHeaders(Map<String, List<String>> headersMap) {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : headersMap.entrySet()) {
            List<String> entryValue = entry.getValue();
            for (String value : entryValue) {
                headers.put(entry.getKey(), value);
            }
        }
        return headers;
    }

    /**
     * 设置{@link HttpURLConnection#setUseCaches(boolean)}的值
     *
     * @param useCaches
     * @return RestClient
     */
    public RestClient useCaches(final boolean useCaches) {
        getConnection().setUseCaches(useCaches);
        return this;
    }

    /**
     * 设置header中accept的值
     *
     * @param value
     * @return RestClient
     */
    private RestClient accept(final String value) {
        return header(Request.HEADER_ACCEPT, value);
    }

    /**
     * 设置header中accept的值为application/json
     *
     * @return RestClient
     */
    RestClient acceptJson() {
        return accept(Request.CONTENT_TYPE_JSON);
    }

    /**
     * 从response header中返回Content-Type的charset参数
     *
     * @return charset or null if none
     */
    public String charset() {
        return parameter(Request.HEADER_CONTENT_TYPE, Request.PARAM_CHARSET);
    }

    /**
     * 从response header中返回指定的参数值
     *
     * @param headerName
     * @param paramName
     * @return parameter value or null if missing
     */
    private String parameter(final String headerName, final String paramName) {
        return getParam(header(headerName), paramName) == null ? Request.CHARSET_UTF8 : getParam(header(headerName), paramName);
    }

    /**
     * 从header中获取参数值
     *
     * @param value
     * @param paramName
     * @return parameter value or null if none
     */
    private String getParam(final String value, final String paramName) {
        if (value == null || value.length() == 0) {
            return null;
        }

        final int length = value.length();
        int start = value.indexOf(';') + 1;
        if (start == 0 || start == length) {
            return null;
        }

        int end = value.indexOf(';', start);
        if (end == -1) {
            end = length;
        }

        while (start < end) {
            int nameEnd = value.indexOf('=', start);
            if (nameEnd != -1 && nameEnd < end
                    && paramName.equals(value.substring(start, nameEnd).trim())) {
                String paramValue = value.substring(nameEnd + 1, end).trim();
                int valueLength = paramValue.length();
                if (valueLength != 0) {
                    if (valueLength > 2 && '"' == paramValue.charAt(0)
                            && '"' == paramValue.charAt(valueLength - 1)) {
                        return paramValue.substring(1, valueLength - 1);
                    } else {
                        return paramValue;
                    }
                }
            }

            start = end + 1;
            end = value.indexOf(';', start);
            if (end == -1) {
                end = length;
            }
        }

        return null;
    }

    /**
     * 获取response header中的值
     *
     * @param name
     * @return response header
     * @throws RestException
     */
    private String header(final String name) throws RestException {
        return getConnection().getHeaderField(name);
    }

    /**
     * 设置request header中的值
     *
     * @param name
     * @param value
     * @return RestClient
     */
    private RestClient header(final String name, final String value) {
        getConnection().setRequestProperty(name, value);
        return this;
    }

    /**
     * 增加request header中的值
     *
     * @param additionalHeaders
     * @return RestClient
     */
    private RestClient addHeaders(Map<String, String> additionalHeaders) {
        if (Preconditions.isNotBlank(additionalHeaders)) {
            for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                getConnection().addRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    /**
     * 将字节数组写入post body
     *
     * @param input body
     */
    private void send(final byte[] input) throws Exception {
        if (input == null) {
            return;
        }
        getConnection().setDoOutput(true);
        // post 请求不能使用缓存
        getConnection().setUseCaches(false);
        DataOutputStream out = new DataOutputStream(getConnection().getOutputStream());
        out.write(input);
        out.close();
    }

    private void outputFileFormData(Request params) throws IOException {
        Set<String> set = params.getParams().getMultiParams().keySet();
        IdentityHashMap<String, File> fileMap = params.getParams().getMultiParams();
        for (String keys : set) {
            StringBuilder buffer = new StringBuilder();
            String key = urlEncode(keys);
            File file = fileMap.get(keys);
            String fileName = file.getName();
            buffer.append(Request.END + Request.DATA_TAG + Request.BOUNDARY + Request.END);
            buffer.append("Content-Disposition: form-data; name=\"").append(key).append("\"; filename=\"").append(fileName).append("\"");
            buffer.append(Request.END);
            buffer.append("Content-Type: ").append(params.getContentType());
            buffer.append(Request.END + Request.END);
            getConnection().setDoOutput(true);
            getConnection().setUseCaches(false);
            DataOutputStream out = new DataOutputStream(getConnection().getOutputStream());
            out.writeBytes(buffer.toString());
            outputFile(file, out);

            out.writeBytes(Request.END + Request.DATA_TAG + Request.BOUNDARY + Request.DATA_TAG + Request.END);
            out.flush();
            out.close();
        }
    }

    private void outputFile(File file, DataOutputStream out) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        int bytes;
        byte[] bufferOut = new byte[1024 * 10];
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        in.close();
    }

    /**
     * URL编码表单
     */
    String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置header的Content-Type
     *
     * @param value
     * @param charset
     * @return RestClient
     */
    private void contentType(final String value, final String charset) {
        if (Preconditions.isNotBlank(charset)) {
            final String separator = "; " + Request.PARAM_CHARSET + '=';
            header(Request.HEADER_CONTENT_TYPE, value + separator + charset);
        } else {
            header(Request.HEADER_CONTENT_TYPE, value);
        }
    }

    private SSLSocketFactory getTrustedFactory()
            throws RestException {
        if (TRUSTED_FACTORY == null) {
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                @SuppressLint("TrustAllX509TrustManager")
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    // Intentionally left blank
                }

                @Override
                @SuppressLint("TrustAllX509TrustManager")
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    // Intentionally left blank
                }
            }};
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustAllCerts, new SecureRandom());
                TRUSTED_FACTORY = context.getSocketFactory();
            } catch (GeneralSecurityException e) {
                IOException ioException = new IOException("Security exception configuring SSL context", e);
                throw new RestException(ioException);
            }
        }
        return TRUSTED_FACTORY;
    }

    private HostnameVerifier getTrustedVerifier() {
        if (TRUSTED_VERIFIER == null) {
            TRUSTED_VERIFIER = new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
        }
        return TRUSTED_VERIFIER;
    }

}
