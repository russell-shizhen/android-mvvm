package com.arophix.mvvm.example

import com.arophix.mvvm.example.api.OkHttpProvider

/**
 * Registered in [ArophixMockTestRunner].
 */
class MvvmTestApp : MvvmApp(){
    override fun getBaseUrl() = "http://127.0.0.1:8080"

    // For testing, we set the timeout to a smaller value
    override fun getHttpClient() = OkHttpProvider.getOkHttpClient(5)

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MvvmTestApp
            private set

    }
}