package com.arophix.mvvm.example

import android.app.Application
import android.content.Context
import com.arophix.mvvm.example.api.OkHttpProvider

open class MvvmApp : Application() {

    open fun getBaseUrl() = "${BuildConfig.SERVER_ENDPOINT}:${BuildConfig.SERVER_PORT}"
    open fun getHttpClient() = OkHttpProvider.getOkHttpClient()

    override fun onCreate() {
        super.onCreate()
        instance = this

        // initialize for any

        // Use ApplicationContext.
        // example: SharedPreferences etc...
         val context: Context = MvvmApp.applicationContext()
    }

    companion object {

        var BASE_URL = "${BuildConfig.SERVER_ENDPOINT}:${BuildConfig.SERVER_PORT}"

        lateinit var instance: MvvmApp
            private set

        fun applicationContext() : Context {
            return instance.applicationContext
        }
    }

}