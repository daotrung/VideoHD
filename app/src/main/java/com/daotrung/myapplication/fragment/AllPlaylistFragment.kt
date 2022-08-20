package com.daotrung.myapplication.fragment

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daotrung.myapplication.adapter.ViewPagerAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentPlayListAllBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import me.yokeyword.fragmentation.SupportActivity


class AllPlaylistFragment : BaseFragment() {
    companion object {
        @JvmStatic
        fun newInstance(): AllPlaylistFragment {
            val args = Bundle()
            val fragment = AllPlaylistFragment()
            fragment.arguments = args
            return fragment
        }
    }
    private lateinit var binding: FragmentPlayListAllBinding
    private var tabTitle = arrayOf("PLAYLIST", "FAVORITE")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayListAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabTitle.forEach {
            val tab = binding.tabLayout.newTab()
            tab.text = it
            binding.tabLayout.addTab(tab)
        }

        val adapter = ViewPagerAdapter(_mActivity)

        val musicSongFragment = PlayListFragment.newInstance()
        adapter.addFragment(musicSongFragment, "")

        val musicFragmentAlbum = FavoriteFragment.newInstance()
        adapter.addFragment(musicFragmentAlbum, "")

        binding.viewPager.apply {
            offscreenPageLimit = adapter.itemCount
            isUserInputEnabled = false
            this.adapter = adapter
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()
    }
}