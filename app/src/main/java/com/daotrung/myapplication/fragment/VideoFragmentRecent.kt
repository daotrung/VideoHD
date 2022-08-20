package com.daotrung.myapplication.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.abs.App.Companion.videoViewModel
import com.daotrung.myapplication.adapter.VideoAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentVideoRecentBinding
import com.daotrung.myapplication.util.LogInstance

class VideoFragmentRecent : BaseFragment() {

    private lateinit var binding: FragmentVideoRecentBinding
    private var adapter: VideoAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoRecentBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        setAdapter()
    }

    private fun setAdapter() {
        binding.rvListTabRecent.setHasFixedSize(true)
        binding.rvListTabRecent.setItemViewCacheSize(10)
        binding.rvListTabRecent.layoutManager = LinearLayoutManager(requireContext())
        adapter = VideoAdapter(requireContext())
        binding.rvListTabRecent.adapter = adapter
        videoViewModel.getVideoLocal().observe(requireActivity(), Observer {
            LogInstance.e(it)
            adapter?.setListData(it.toCollection(ArrayList()))
        })
    }

}