package com.arophix.mvvm.example.viewmodels

import androidx.lifecycle.*
import androidx.work.*
import com.arophix.mvvm.example.MvvmApp
import com.arophix.mvvm.example.data.ArophixDnsResponse
import com.arophix.mvvm.example.data.ArophixDnsResponseRepository
import com.arophix.mvvm.example.data.Result
import com.arophix.mvvm.example.data.DnsLocation
import com.arophix.mvvm.example.utilities.SIMPLE_BEST_LOCATION_WORKER_NAME
import com.arophix.mvvm.example.workers.CleanupWorker
import com.arophix.mvvm.example.workers.PingBestLocationWorker
import com.arophix.mvvm.example.workers.SimpleBestLocationWorker
import java.util.concurrent.TimeUnit


/**
 * The ViewModel for [com.arophix.mvvm.example.DnsLocationListFragment].
 */
class DnsLocationListViewModel internal constructor(
        private val repository: ArophixDnsResponseRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workManager = WorkManager.getInstance(MvvmApp.applicationContext())

    /**
     * Need network connected.
     */
    private var constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    /**
     * Add a cleanup worker at beginning.
     */
    private var continuation: WorkContinuation = workManager.beginUniqueWork(
                    SIMPLE_BEST_LOCATION_WORKER_NAME,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequest.from(CleanupWorker::class.java))

    internal val bestLocationResult: LiveData<List<WorkInfo>> = getUpdateBestLocationState().switchMap {
        workManager.getWorkInfosByTagLiveData(SIMPLE_BEST_LOCATION_WORKER_NAME)
    }

    internal fun startBestLocationWorker(dnsLocations: List<DnsLocation>) {

        workManager.pruneWork()

        println("====>>> startBestLocationWorker.")

        val simpleBestLocationWorker = OneTimeWorkRequestBuilder<SimpleBestLocationWorker>()
                .setInputData(createInputData(dnsLocations))
                .setInitialDelay(1, TimeUnit.SECONDS)
                .setConstraints(constraints)
//                .addTag(SIMPLE_BEST_LOCATION_WORKER_NAME)
                .build()

        val pingBestLocationWorker = OneTimeWorkRequestBuilder<PingBestLocationWorker>()
//                .setInputData(createInputData(dnsLocations))
                .setInitialDelay(1, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .addTag(SIMPLE_BEST_LOCATION_WORKER_NAME)
                .build()

        /**
         * Then start the simple best location worker and ping base best location worker.
         */
        continuation.then(simpleBestLocationWorker).then(pingBestLocationWorker).enqueue()
    }

    internal fun cancel() {
        workManager.cancelUniqueWork(SIMPLE_BEST_LOCATION_WORKER_NAME)
    }

    private fun createInputData(dnsLocations: List<DnsLocation>): Data {
        val dnsNameServerMap = dnsLocations.map { it.name to it.serverList }.toMap()
        return Data.Builder().putAll(dnsNameServerMap).build()
    }

    val arophixDnsResponses: LiveData<Result<List<ArophixDnsResponse>>>
            = getSavedFetchLocationsState().switchMap {
        repository.getArophixDnsResponses()
    }

    fun updateDnsLocations(update: Boolean) {
        savedStateHandle.set(FETCH_LOCATIONS, update)
    }

    fun updateBestLocation(update: Boolean) {
        savedStateHandle.set(UPDATE_BEST_LOCATIONS, update)
    }

    private fun getSavedFetchLocationsState(): MutableLiveData<Boolean> {
        return savedStateHandle.getLiveData(FETCH_LOCATIONS, true)
    }

    private fun getUpdateBestLocationState(): MutableLiveData<Boolean> {
        return savedStateHandle.getLiveData(UPDATE_BEST_LOCATIONS, true)
    }

    companion object {
        private const val FETCH_LOCATIONS = "FETCH_LOCATIONS"
        private const val UPDATE_BEST_LOCATIONS = "UPDATE_BEST_LOCATIONS"
    }
}
