package com.arophix.mvvm.example.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arophix.mvvm.example.data.DnsLocation
import com.arophix.mvvm.example.databinding.ListItemDnsLocationBinding

/**
 * Adapter for the [RecyclerView] in [com.arophix.mvvm.example.DnsLocationListFragment].
 */
class DnsLocationAdapter : ListAdapter<DnsLocation, RecyclerView.ViewHolder>(DnsLocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DnsLocationViewHolder(ListItemDnsLocationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dnsLocation = getItem(position)
        (holder as DnsLocationViewHolder).bind(dnsLocation)
    }

    class DnsLocationViewHolder(
        private val binding: ListItemDnsLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener {
                binding.dnsLocation?.let { _ ->

                }
            }
        }

        fun bind(item: DnsLocation) {
            binding.apply {
                dnsLocation = item
                executePendingBindings()
            }
        }
    }
}

private class DnsLocationDiffCallback : DiffUtil.ItemCallback<DnsLocation>() {

    override fun areItemsTheSame(oldItem: DnsLocation, newItem: DnsLocation): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: DnsLocation, newItem: DnsLocation): Boolean {
        return oldItem == newItem
    }
}