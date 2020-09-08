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

package com.arophix.mvvm.example.workers

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters

/**
 * This is a simple best location worker, it can be the cases like below:
 *
 * 1. According to user's current geographic location, choose the closest DNS server.
 * 2. Randomly choose a server that help ease the load balance of all servers.
 * 3. ....
 *
 * This worker is to provide a quick result to user before the subsequent Ping worker (slower)
 * returns the "real" best location.
 */
class SimpleBestLocationWorker(context: Context, parameters: WorkerParameters) :
        BestLocationBaseWorker(context, parameters) {

    override suspend fun compute(nameServerIpMap: Map<String, Any>): Result {

        println("====>>> SimpleBestLocationWorker.compute(): $id")

//        Simply return UK servers, for testing purpose only
//        val map = nameServerIpMap.filter { (key, _ ) -> key.startsWith("UK")}

        /**
         * We don't have any real steps to do here, just forward all the input data to next Worker.
         */
        val bestLocation = Data.Builder().putAll(nameServerIpMap).build()
        return Result.success(bestLocation)

    }
}
