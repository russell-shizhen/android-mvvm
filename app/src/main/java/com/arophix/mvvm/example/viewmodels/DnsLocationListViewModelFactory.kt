package com.arophix.mvvm.example.viewmodels

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.arophix.mvvm.example.data.ArophixDnsResponseRepository

/**
 * Factory for creating a [DnsLocationListViewModel] with a constructor
 * that takes a [ArophixDnsResponseRepository] and an
 * ID for the current [com.arophix.mvvm.example.data.DnsLocation].
 */
class DnsLocationListViewModelFactory(
        private val repository: ArophixDnsResponseRepository,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
    ): T {
        return DnsLocationListViewModel(repository, handle) as T
    }
}

