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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * DNS location not reachable means that all the IP addresses associated with the same DNS location
 * name are not reachable, e.g. for below DNS location
 *
 * <location name="Comodo Secure DNS" sort_order="4" icon_id="3">
 *      <server ip="8.26.56.26"/>
 *      <server ip="8.20.247.20"/>
 * </location>
 *
 * If all the IPs are not reachable, then the DNS "Comodo Secure DNS" is not reachable.
 *
 * Need to consider following cases:
 *
 * 1. All server locations are not reachable.
 * 2. Some of the server locations are not reachable.
 * 3. One ore more IPs of the same server location are not reachable.
 */
abstract class BestLocationBaseWorker(context: Context, parameters: WorkerParameters) :
        CoroutineWorker(context, parameters) {

    /**
     * name -> server ip address list with comma as separator, e.g.
     * UK - Berkshire -> 109.169.27.39,78.129.227.106,78.129.233.74
     */
    private lateinit var nameServerIpMap: Map<String, Any>

    override suspend fun doWork(): Result {

        nameServerIpMap = inputData.keyValueMap

        println("====>>> BestLocationBaseWorker.doWork()")

        return compute(nameServerIpMap)
    }

    abstract suspend fun compute(nameServerIpMap: Map<String, Any>): Result

    companion object {
        const val TAG = "BestLocationBaseWorker"
    }
}

