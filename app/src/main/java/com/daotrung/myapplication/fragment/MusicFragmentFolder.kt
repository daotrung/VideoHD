package com.daotrung.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.abs.App.Companion.musicViewModel
import com.daotrung.myapplication.activity.SongsActivity
import com.daotrung.myapplication.adapter.MusicFolderAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentMusicFolderBinding
import com.daotrung.myapplication.model.FolderMusic
import com.daotrung.myapplication.util.FOLDER
import com.google.gson.Gson
import com.simplemobiletools.commons.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.commons.helpers.ensureBackgroundThread

class MusicFragmentFolder : BaseFragment() {

    private lateinit var binding: FragmentMusicFolderBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicFolderBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val mLayout = LinearLayoutManager(mContext)
        binding.rcvFolder.layoutManager = mLayout
        musicViewModel.getFolder().observe(requireActivity(), Observer {
            loadFolder(it)
        })
    }

    private fun loadFolder(folders: ArrayList<FolderMusic>) {
        ensureBackgroundThread {
            mActivity.runOnUiThread {
                val adapter = binding.rcvFolder.adapter
                if (adapter == null) {
                    MusicFolderAdapter(folders, itemClick = {
                        Intent(mActivity, SongsActivity::class.java).apply {
                            putExtra(FOLDER, Gson().toJson(it))
                            mActivity.startActivity(this)
                        }
                    }).apply {
                        binding.rcvFolder.adapter = this
                    }
                    if (mActivity.areSystemAnimationsEnabled) {
                        binding.rcvFolder.scheduleLayoutAnimation()
                    }
                }
            }
        }
    }

}