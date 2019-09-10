package com.plugin.gradle.lucio.core.http;

import java.io.IOException;

/**
 * @Description
 * @Author luxiao
 * @Date 2019-07-09 10:33
 * @Version
 */
public interface Call {
	Response execute() throws IOException;
	void enqueue();
	void cancel();
}
