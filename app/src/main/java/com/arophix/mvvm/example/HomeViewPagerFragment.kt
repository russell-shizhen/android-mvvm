package com.arophix.mvvm.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.arophix.mvvm.example.adapters.ArophixDnsPagerAdapter
import com.arophix.mvvm.example.adapters.DNS_LOCATION_LIST_PAGE_INDEX
import com.arophix.mvvm.example.databinding.FragmentViewPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class HomeViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager

        viewPager.adapter = ArophixDnsPagerAdapter(this)

        // Set the icon and text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        return binding.root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            DNS_LOCATION_LIST_PAGE_INDEX -> R.drawable.dns_location_tab_selector
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            DNS_LOCATION_LIST_PAGE_INDEX -> getString(R.string.dns_location_list_title)
            else -> null
        }
    }
}