package com.daotrung.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.adapter.VideoFolderAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentVideoFolderBinding
import com.daotrung.myapplication.util.GetAllListFolderUtils

class VideoFragmentFolder : BaseFragment() {

    private lateinit var binding : FragmentVideoFolderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoFolderBinding.inflate(
            inflater,container,false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.rvListTabFolderVideo.setHasFixedSize(true)
        binding.rvListTabFolderVideo.setItemViewCacheSize(10)
        binding.rvListTabFolderVideo.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListTabFolderVideo.adapter = VideoFolderAdapter(requireContext(), GetAllListFolderUtils.getAllVideosInFolder(mActivity))
    }


}