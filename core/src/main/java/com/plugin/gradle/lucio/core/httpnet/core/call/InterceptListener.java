/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/huanghaibin_dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.plugin.gradle.lucio.core.httpnet.core.call;

/**
 * 上传文件进度监听
 */

public interface InterceptListener {
    /**
     * 上传进度回调
     *
     * @param index         当前文件
     * @param currentLength 当前进度
     * @param totalLength   文件大小
     */
    void onProgress(int index, long currentLength, long totalLength);
}
