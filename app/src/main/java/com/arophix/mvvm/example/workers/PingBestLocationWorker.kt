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
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * This is a ping based best location worker
 */
class PingBestLocationWorker(context: Context, parameters: WorkerParameters) :
        BestLocationBaseWorker(context, parameters) {

    @ExperimentalStdlibApi
    override suspend fun compute(nameServerIpMap: Map<String, Any>): Result {

        val resultNameServerIpMap: MutableMap<String, MutableMap<String, Double>>
                = emptyMap<String, MutableMap<String, Double>>().toMutableMap()

        println("====>>> SimpleBestLocationWorker.compute(): $id\"")

        nameServerIpMap.forEach { (name, servers) ->
            val serverLatency: MutableMap<String, Double> = emptyMap<String, Double>().toMutableMap()
            (servers as String).split(",").forEach { ipString ->
                val latency = pingServerAverageRtt(ipString)
                if (latency != Double.MAX_VALUE) { // Only consider the reachable servers
                    serverLatency[ipString] = pingServerAverageRtt(ipString)
                }
            }
            resultNameServerIpMap[name] = serverLatency
        }

        // Testing data, intentionally set latency as a bigger value
        val serverLatencyTestBigValue: MutableMap<String, Double> = emptyMap<String, Double>().toMutableMap()
        serverLatencyTestBigValue["192.168.1.173"] = 1000000.32
        serverLatencyTestBigValue["192.168.1.156"] = 2000000.98
        resultNameServerIpMap["localhost_big"] = serverLatencyTestBigValue

//        val serverLatencyTestSmallValue: MutableMap<String, Double> = emptyMap<String, Double>().toMutableMap()
//        serverLatencyTestSmallValue["192.168.2.173"] = 1.32
//        serverLatencyTestSmallValue["192.168.2.156"] = 2.98
//        resultNameServerIpMap["localhost_small"] = serverLatencyTestSmallValue

        // filter the reachable DNS only.
        resultNameServerIpMap.values.removeAll { it.isEmpty() }

        var serverWithLowestLatency: Map.Entry<String, MutableMap<String, Double>> = resultNameServerIpMap.entries.first()
        var lowestLatency = serverWithLowestLatency.value.values.min()

        resultNameServerIpMap.forEach {
            val latency = it.value.values.min()
            if (latency!! < lowestLatency!!) {
                serverWithLowestLatency = it
                lowestLatency = latency
            }
            println("====>>> resultNameServerIpMap.forEach: ${it.key} min latency: $latency")
        }

        val nameServerIpMapToReturn: MutableMap<String, String> = emptyMap<String, String>().toMutableMap()
        nameServerIpMapToReturn[serverWithLowestLatency.key] = serverWithLowestLatency.value.keys.toList().joinToString()
        val bestLocation = Data.Builder().putAll(nameServerIpMapToReturn as Map<String, Any>).build()

        return Result.success(bestLocation)
    }

    /**
     * Function that uses ping, takes server name or ip as argument.
     *
     * @return [Double.MAX_VALUE] if server is not reachable. Average RTT if the server is reachable.
     *
     * <location name="Google DNS Server" sort_order="2" icon_id="1">
     *     <server ip="63.52.161.132"/>
     *     <server ip="8.8.8.8"/> // the only reachable IP
     *     <server ip="8.8.8.9"/>
     * </location>
     *
     * Success output example
     *
     * PING 8.8.8.8 (8.8.8.8) 56(84) bytes of data.
     * 64 bytes from 8.8.8.8: icmp_seq=1 ttl=254 time=172 ms
     * 64 bytes from 8.8.8.8: icmp_seq=2 ttl=254 time=166 ms
     * 64 bytes from 8.8.8.8: icmp_seq=3 ttl=254 time=167 ms
     * 64 bytes from 8.8.8.8: icmp_seq=4 ttl=254 time=172 ms
     * 64 bytes from 8.8.8.8: icmp_seq=5 ttl=254 time=167 ms

     * --- 8.8.8.8 ping statistics ---
     * 5 packets transmitted, 5 received, 0% packet loss, time 4011ms
     * rtt min/avg/max/mdev = 166.470/169.313/172.322/2.539 ms
     *          |________________________|
     * value to parse using it.split('=')[1].trim().split(' ')[0].trim().split('/')[1].toDouble()
     */
    @ExperimentalStdlibApi
    fun pingServerAverageRtt(host: String): Double {

        var aveRtt: Double = Double.MAX_VALUE

        // https://stackoverflow.com/questions/3905358/how-to-ping-external-ip-from-java-android
        try {
            // execute the command on the environment interface, timeout is set as 0.2 to get response faster.
            val pingProcess: Process = Runtime.getRuntime().exec("/system/bin/ping -i 0.2 -c 5 $host")
            // gets the input stream to get the output of the executed command
            val bufferedReader = BufferedReader(InputStreamReader(pingProcess.inputStream))

            bufferedReader.forEachLine {
                if (it.isNotEmpty() && it.contains("min/avg/max/mdev")) {  // when we get to the last line of executed ping command
                    println("====>>> inputLine: $it")
                    aveRtt = it.split('=')[1].trim()
                            .split(' ')[0].trim()
                            .split('/')[1].toDouble()
                }
            }
        } catch (e: IOException) {
            Timber.tag(this.javaClass.simpleName).v("getLatency: EXCEPTION")
            e.printStackTrace()
        }

        return aveRtt
    }
}
