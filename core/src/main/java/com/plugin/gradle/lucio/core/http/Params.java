package com.plugin.gradle.lucio.core.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @Description
 * @Author luxiao
 * @Date 2019-07-26 14:54
 * @Version
 */
public class Params {
    private IdentityHashMap<String, File> multiParams;
    private JSONObject jsonParams;

    public Params() {
        jsonParams = new JSONObject();
    }

    public Params put(String name, String value) {
        try {
            jsonParams.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Params put(Map params) {
        jsonParams = new JSONObject(params);
        return this;
    }

    public Params put(JSONObject jsonObject) {
        jsonParams = jsonObject;
        return this;
    }

    public Params put(String name, int value) {
        return put(name, String.valueOf(value));
    }

    public Params put(String name, long value) {
        return put(name, String.valueOf(value));
    }

    public Params put(String name, double value) {
        return put(name, String.valueOf(value));
    }

    public Params put(String name, float value) {
        return put(name, String.valueOf(value));
    }

    public Params put(String name, byte value) {
        return put(name, String.valueOf(value));
    }

    public Params put(String name, boolean value) {
        return put(name, String.valueOf(value));
    }

    public Params putFile(String name, File file) {
        if (multiParams == null) multiParams = new IdentityHashMap<>();
        if (!file.exists()) return this;
        multiParams.put(name, file);
        return this;
    }

    public Params putFile(String name, String fileName) {
        return putFile(name, new File(fileName));
    }

    protected JSONObject getJsonParams() {
        return jsonParams;
    }

    protected IdentityHashMap<String, File> getMultiParams() {
        return multiParams;
    }
}
