/**
 *
 */
package com.plugin.gradle.lucio.core.http;

import java.io.IOException;

/**
 * @Description
 * @Author luxiao
 * @Date 2019/4/25 7:23 PM
 * @Version
 */
public class RestException extends RuntimeException {

    private static final long serialVersionUID = 8520390726356829268L;
    private String errorCode;

    protected RestException(IOException cause) {
        super(cause);
    }

    protected RestException(IOException cause, String code) {
        super(cause);
        this.errorCode = code;
    }

    protected RestException(String code) {
        super();
        this.errorCode = code;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public IOException getCause() {
        return (IOException) super.getCause();
    }
}
