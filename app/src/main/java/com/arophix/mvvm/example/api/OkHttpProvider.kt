/*
 * Copyright 2020 Shizhen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arophix.mvvm.example.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkHttpProvider {

    private val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

    // Default timeout for the network requests
    private const val REQUEST_TIMEOUT = 30L

    private var okHttpClient: OkHttpClient? = null

    /**
     * requestTimeout is in seconds
     */
    fun getOkHttpClient(requestTimeout: Long = REQUEST_TIMEOUT): OkHttpClient {
        return if (okHttpClient == null) {
            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .readTimeout(requestTimeout, TimeUnit.SECONDS)
                    .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                    .build()
            this.okHttpClient = okHttpClient
            okHttpClient
        } else {
            okHttpClient!!
        }
    }
}
