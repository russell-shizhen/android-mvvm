package com.arophix.mvvm.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import com.arophix.mvvm.example.adapters.DnsLocationAdapter
import com.arophix.mvvm.example.data.ArophixDnsResponse
import com.arophix.mvvm.example.data.Result
import com.arophix.mvvm.example.data.Result.Status
import com.arophix.mvvm.example.databinding.FragmentDnsLocationListBinding
import com.arophix.mvvm.example.utilities.InjectorUtils
import com.arophix.mvvm.example.viewmodels.DnsLocationListViewModel
import com.arophix.mvvm.example.views.hide
import com.arophix.mvvm.example.views.show
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DnsLocationListFragment : Fragment() {

    /**
     * Workaround for testing pass.
     */
    companion object {
        var isForTesting: Boolean = false
    }

    private lateinit var buttonShowBestLocation: Button
    private var bestDnsLocation: MutableMap<String, Any?> = emptyMap<String, Any?>().toMutableMap()// name -> ip servers

    private lateinit var buttonRefresh: Button
    private val adapter = DnsLocationAdapter()
    private var getDnsLocationsJob: Job? = null

    private val viewModel: DnsLocationListViewModel by viewModels {
        InjectorUtils.provideDnsLocationListViewModelFactory(this)
    }

    private fun getDnsDescription(keyValueMap: Map<String, Any?>): String {
        val mapAsString = StringBuilder("\n")
        for (key in keyValueMap.keys) {
            mapAsString.append(key + "=" + keyValueMap[key] + "\n")
        }

        return mapAsString.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDnsLocationListBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.dnsLocationList.adapter = adapter

        getDnsLocations(binding)

        buttonRefresh = binding.refresh
        buttonRefresh.setOnClickListener {
            updateData()
        }

        buttonShowBestLocation = binding.showBestLocation
        buttonShowBestLocation.setOnClickListener {
//            updateBestLocation()
            showMessageBox(getDnsDescription(bestDnsLocation))
        }

        if(!isForTesting) {
            subscribeBestLocationResult(binding)
        }

        return binding.root
    }

    private fun subscribeBestLocationResult(binding: FragmentDnsLocationListBinding) {

        viewModel.bestLocationResult.observe(viewLifecycleOwner, Observer<List<WorkInfo>> { listOfInfos ->
            if (listOfInfos == null || listOfInfos.isEmpty()) {
                Snackbar.make(binding.root, "No best location figured out yet.", Snackbar.LENGTH_LONG).show()
                return@Observer
            }

            Toast.makeText(activity, "listOfInfos.size=${listOfInfos.size}",
                    Toast.LENGTH_LONG).show()

            val workInfo = listOfInfos.findLast { it.state.isFinished }
            if (workInfo == null) {
                Snackbar.make(binding.root, "Best location worker not finished yet.", Snackbar.LENGTH_LONG).show()
            } else {
                if (workInfo.state.isFinished) {
                    Snackbar.make(binding.root, "Best DNS location is figured out by ${workInfo.tags}",
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction("DISMISS"){}.show()
                    bestDnsLocation = workInfo.outputData.keyValueMap
                } else {
                    Snackbar.make(binding.root, "Worker state: ${workInfo.state.name}",
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction("DISMISS"){}.show()
                }
            }
        })
    }

    private fun getDnsLocations(binding: FragmentDnsLocationListBinding) {
        // Make sure we cancel the previous job before creating a new one
        getDnsLocationsJob?.cancel()
        getDnsLocationsJob = lifecycleScope.launch {
            viewModel.arophixDnsResponses.observe(viewLifecycleOwner, Observer<Result<List<ArophixDnsResponse>>> { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        binding.progressBar.hide()
                        result.data?.let {
                            if (it.isNotEmpty()) {
                                // cache the latest DNS locations
                                println("====>>> DNS locations refreshed.")
                                if (!isForTesting) {
                                    viewModel.startBestLocationWorker(it[0].dnsLocations)
                                }
                                adapter.submitList(it[0].dnsLocations)
                                buttonRefresh.text = it[0].buttonText.text
                            }
                        }
                    }
                    Status.LOADING -> binding.progressBar.show()
                    Status.ERROR -> {
                        binding.progressBar.hide()
                        Snackbar.make(binding.root, result.message!!, Snackbar.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    private fun updateData() {
        with(viewModel) {
            updateDnsLocations(true)
        }
    }

    private fun updateBestLocation() {
        with(viewModel) {
            updateBestLocation(true)
        }
    }

    private fun showMessageBox(messageText: String) {

        val builder = AlertDialog.Builder(context!!)

        with(builder) {
            setTitle("Best DNS Location")
            setCancelable(false)
            setMessage("According to ping tests I’ve been running in the background, " +
                    "the best location for you appears to be \n$messageText")
            setPositiveButton("OK") { _, _ -> }
            show()
        }

    }
}