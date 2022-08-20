package com.daotrung.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore.Audio.AlbumColumns.ALBUM
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.activity.SongsActivity
import com.daotrung.myapplication.adapter.MusicAdapter
import com.daotrung.myapplication.adapter.MusicAlbumAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentMusicAlbumBinding
import com.daotrung.myapplication.model.Album
import com.daotrung.myapplication.model.MusicLocal
import com.google.gson.Gson
import com.simplemobiletools.commons.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import java.util.ArrayList

class MusicFragmentAlbum : BaseFragment() {

    private lateinit var binding : FragmentMusicAlbumBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMusicAlbumBinding.inflate(
            inflater,container,false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mLayout = LinearLayoutManager(mContext)
        binding.rvListAlbum.layoutManager = mLayout
        initView()
    }


    private fun initView() {

        ensureBackgroundThread {
            mActivity.runOnUiThread {
                val adapterSong = binding.rvListAlbum.adapter
                App.musicViewModel.getAlbum().observe(requireActivity(), Observer {
                    if (adapterSong == null) {
                        MusicAlbumAdapter(it, itemClick = {
                            Intent(mActivity, SongsActivity::class.java).apply {
                                putExtra(ALBUM, Gson().toJson(it))
                                mActivity.startActivity(this)
                            }
                        }).apply {
                            binding.rvListAlbum.adapter = this
                        }
                        if (mContext.areSystemAnimationsEnabled) {
                            binding.rvListAlbum.scheduleLayoutAnimation()
                        }
                    } else {
                        (adapterSong as MusicAlbumAdapter).updateItems(it as ArrayList<Album>)
                    }
                })
            }
        }
    }



}