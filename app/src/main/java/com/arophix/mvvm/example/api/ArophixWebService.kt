package com.arophix.mvvm.example.api

import com.arophix.mvvm.example.MvvmApp
import com.arophix.mvvm.example.data.ArophixDnsApiResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET

/**
 * Used to connect to the Arophix API to fetch DNS server locations
 * E.g. https://arophix.com/locations
 */
interface ArophixWebService {
    // need to convert ArophixDnsApiResponse to ArophixDnsResponse
    @GET("/AdhocCPS/dns/express_dns/list")
    suspend fun getDnsServerLocations(): Response<ArophixDnsApiResponse>

    companion object {
        fun create(): ArophixWebService {
            return Retrofit.Builder()
                    .baseUrl(MvvmApp.instance.getBaseUrl())
                    .client(MvvmApp.instance.getHttpClient())
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .build()
                    .create(ArophixWebService::class.java)
        }
    }
}
