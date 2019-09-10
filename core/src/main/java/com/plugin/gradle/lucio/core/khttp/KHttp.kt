/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
@file:JvmName("KHttp")

package com.plugin.gradle.lucio.core.khttp

import com.plugin.gradle.lucio.core.khttp.requests.GenericRequest
import com.plugin.gradle.lucio.core.khttp.responses.GenericResponse
import com.plugin.gradle.lucio.core.khttp.responses.Response
import com.plugin.gradle.lucio.core.khttp.structures.authorization.Authorization
import com.plugin.gradle.lucio.core.khttp.structures.files.FileLike
import kotlin.concurrent.thread

/**
 * The default number of seconds to wait before timing out a request.
 */
const val DEFAULT_TIMEOUT = 30.0

@JvmOverloads
fun delete(url: String, headers: Map<String, String?> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf()): Response {
    return request("DELETE", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files)
}

@JvmOverloads
fun get(url: String, headers: Map<String, String?> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf()): Response {
    return request("GET", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files)
}

@JvmOverloads
fun head(url: String, headers: Map<String, String?> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf()): Response {
    return request("HEAD", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files)
}

@JvmOverloads
fun options(url: String, headers: Map<String, String?> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf()): Response {
    return request("OPTIONS", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files)
}

@JvmOverloads
fun patch(url: String, headers: Map<String, String?> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf()): Response {
    return request("PATCH", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files)
}

@JvmOverloads
fun post(url: String, headers: Map<String, String?> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf()): Response {
    return request("POST", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files)
}

@JvmOverloads
fun put(url: String, headers: Map<String, String?> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf()): Response {
    return request("PUT", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files)
}

@JvmOverloads
fun request(method: String, url: String, headers: Map<String, String?> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf()): Response {
    return GenericResponse(GenericRequest(method, url, params, headers, data, json, auth, cookies, timeout, allowRedirects, stream, files)).run {
        this.init()
        this._history.last().apply {
            this@run._history.remove(this)
        }
    }
}

/**
 * Provides a library interface for performing asynchronous requests
 */
class async {
    companion object {

        @JvmOverloads
        fun delete(url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf(), onError: Throwable.() -> Unit = { throw this }, onResponse: Response.() -> Unit = {}): Unit {
            request("DELETE", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files, onError, onResponse)
        }

        @JvmOverloads
        fun get(url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf(), onError: Throwable.() -> Unit = { throw this }, onResponse: Response.() -> Unit = {}): Unit {
            request("GET", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files, onError, onResponse)
        }

        @JvmOverloads
        fun head(url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf(), onError: Throwable.() -> Unit = { throw this }, onResponse: Response.() -> Unit = {}): Unit {
            request("HEAD", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files, onError, onResponse)
        }

        @JvmOverloads
        fun options(url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf(), onError: Throwable.() -> Unit = { throw this }, onResponse: Response.() -> Unit = {}): Unit {
            request("OPTIONS", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files, onError, onResponse)
        }

        @JvmOverloads
        fun patch(url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf(), onError: Throwable.() -> Unit = { throw this }, onResponse: Response.() -> Unit = {}): Unit {
            request("PATCH", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files, onError, onResponse)
        }

        @JvmOverloads
        fun post(url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf(), onError: Throwable.() -> Unit = { throw this }, onResponse: Response.() -> Unit = {}): Unit {
            request("POST", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files, onError, onResponse)
        }

        @JvmOverloads
        fun put(url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf(), onError: Throwable.() -> Unit = { throw this }, onResponse: Response.() -> Unit = {}): Unit {
            request("PUT", url, headers, params, data, json, auth, cookies, timeout, allowRedirects, stream, files, onError, onResponse)
        }

        @JvmOverloads
        fun request(method: String, url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf(), data: Any? = null, json: Any? = null, auth: Authorization? = null, cookies: Map<String, String>? = null, timeout: Double = DEFAULT_TIMEOUT, allowRedirects: Boolean? = null, stream: Boolean = false, files: List<FileLike> = listOf(), onError: Throwable.() -> Unit = { throw this }, onResponse: Response.() -> Unit = {}): Unit {
            thread {
                try {
                    onResponse(
                        com.plugin.gradle.lucio.core.khttp.request(
                            method,
                            url,
                            headers,
                            params,
                            data,
                            json,
                            auth,
                            cookies,
                            timeout,
                            allowRedirects,
                            stream,
                            files
                        )
                    )
                } catch (e: Throwable) {
                    onError(e)
                }
            }
        }

    }
}
