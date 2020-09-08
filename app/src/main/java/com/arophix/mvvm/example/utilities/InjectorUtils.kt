package com.arophix.mvvm.example.utilities

import android.content.Context
import androidx.fragment.app.Fragment
import com.arophix.mvvm.example.api.ArophixWebService
import com.arophix.mvvm.example.data.*
import com.arophix.mvvm.example.viewmodels.*

/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */
object InjectorUtils {

    private fun provideArophixDnsRemoteDataSource()
            = ArophixDnsRemoteDataSource(ArophixWebService.create())

    private fun getArophixDnsResponseRepository(context: Context): ArophixDnsResponseRepository {
        return ArophixDnsResponseRepository.getInstance(
                AppDatabase.getInstance(context.applicationContext).arophixDnsResponseDao(),
                provideArophixDnsRemoteDataSource())
    }

    fun provideDnsLocationListViewModelFactory(fragment: Fragment): DnsLocationListViewModelFactory {
        return DnsLocationListViewModelFactory(getArophixDnsResponseRepository(fragment.requireContext()), fragment)
    }

}
