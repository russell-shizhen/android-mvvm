package com.arophix.mvvm.example.data

import com.arophix.mvvm.example.api.BaseDataSource
import com.arophix.mvvm.example.api.ArophixWebService
import javax.inject.Inject

/**
 * Works with the Arophix API to get data.
 */
class ArophixDnsRemoteDataSource @Inject constructor(
        private val service: ArophixWebService
) : BaseDataSource() {

    suspend fun fetchData() = getResult { service.getDnsServerLocations() }

}
