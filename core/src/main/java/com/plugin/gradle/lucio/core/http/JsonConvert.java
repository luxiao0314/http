/*
 * Copyright 2018 Zhenjie Yan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.plugin.gradle.lucio.core.http;

import com.plugin.gradle.lucio.core.utils.JSONUtils;
import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * @Description json转换器
 * @Author luxiao
 * @Date 2019/4/26 11:19 AM
 * @Version
 */
public class JsonConvert<T> implements Converter<T> {

    private Type type;

    JsonConvert(Type type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T convert(Response response) throws Throwable {
        JSONObject json = new JSONObject(response.body().string());
        int code = json.optInt("code");
        String message = json.optString("message");
        if (code == 0) {
            return (T) JSONUtils.convertToObj(json.optJSONObject("data"), (Class<?>) this.type);
        } else {
            throw new Exception("code：" + code + "，message：" + message);
        }
    }
}