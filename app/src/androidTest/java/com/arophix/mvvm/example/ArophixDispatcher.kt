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

package com.arophix.mvvm.example

import android.content.Context
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


class ArophixDispatcher(
        private val context: Context = InstrumentationRegistry.getInstrumentation().context
) : Dispatcher() {

    @Throws(InterruptedException::class)
    override fun dispatch(request: RecordedRequest): MockResponse {

        val errorResponse = MockResponse().setResponseCode(404).setBody("path errors")

        // Path errors, just return
        val requestPath = Uri.parse(request.path).path ?: return errorResponse

        return if (requestPath == "/locations") {
            // Success case
            val responseBody = AssetReaderUtil.asset(context, "arophix_response_success.xml")
            MockResponse().setResponseCode(200).setBody(responseBody)
        } else {
            // Unauthorized user
            val responseBody = AssetReaderUtil.asset(context, "arophix_response_error.xml")
            MockResponse().setResponseCode(403).setBody(responseBody)
        }
    }
}

object AssetReaderUtil {
    fun asset(context: Context, assetPath: String): String {
        try {
            val inputStream = context.assets.open("api-response/$assetPath")
            return inputStreamToString(inputStream, "UTF-8")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    private fun inputStreamToString(inputStream: InputStream, charsetName: String): String {
        val builder = StringBuilder()
        val reader = InputStreamReader(inputStream, charsetName)
        reader.readLines().forEach {
            builder.append(it)
        }
        return builder.toString()
    }
}