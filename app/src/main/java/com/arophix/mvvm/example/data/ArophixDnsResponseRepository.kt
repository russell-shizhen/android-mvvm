package com.arophix.mvvm.example.data

/**
 * Repository module for handling data operations.
 */
class ArophixDnsResponseRepository constructor(
        private val arophixDnsResponseDao: ArophixDnsResponseDao,
        private val arophixDnsRemoteDataSource: ArophixDnsRemoteDataSource
) {

    fun getArophixDnsResponses() = resultLiveData(
            databaseQuery = {
                println("==>>> databaseQuery")
                arophixDnsResponseDao.getArophixDnsResponses()
            },
            networkCall = {
                println("==>>> networkCall")
                arophixDnsRemoteDataSource.fetchData()
            },
            saveNetworkCallResult = {
                println("==>>> saveNetworkCallResult")
                println("arophixDnsResponseDao.insertArophixDnsResponse")
                println("it.toButtonText(): ${it.toButtonText().text}")
                arophixDnsResponseDao.insertArophixDnsResponse(it.toButtonText(), it.toDnsLocationList())
            }
    )

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: ArophixDnsResponseRepository? = null

        fun getInstance(arophixDnsResponseDao: ArophixDnsResponseDao,
                        arophixDnsRemoteDataSource: ArophixDnsRemoteDataSource) =
                instance ?: synchronized(this) {
                    instance ?: ArophixDnsResponseRepository(
                            arophixDnsResponseDao,
                            arophixDnsRemoteDataSource
                    ).also { instance = it }
                }
    }

}

