package com.daotrung.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore.Audio.AlbumColumns.ARTIST
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.abs.App.Companion.musicViewModel
import com.daotrung.myapplication.activity.SongsActivity
import com.daotrung.myapplication.adapter.MusicArtistAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentMusicArtistBinding
import com.daotrung.myapplication.model.Artist
import com.daotrung.myapplication.util.config
import com.google.gson.Gson
import com.simplemobiletools.commons.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.commons.helpers.ensureBackgroundThread


class MusicFragmentArtist : BaseFragment() {

    private lateinit var binding: FragmentMusicArtistBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicArtistBinding.inflate(
            inflater,container,false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        Artist.sorting = mContext.config.artistSorting
        val mLayout = LinearLayoutManager(mContext)
        binding.rvListArtis.layoutManager = mLayout
        musicViewModel.getArtists().observe(requireActivity(), Observer {
            loadArtist(it)
        })
    }

    private fun loadArtist(artists: ArrayList<Artist>) {
        ensureBackgroundThread {
            mActivity.runOnUiThread {
                val adapter = binding.rvListArtis.adapter
                if(adapter == null){
                    MusicArtistAdapter(artists, itemClick = {
                        Intent(mActivity, SongsActivity::class.java).apply {
                            putExtra(ARTIST, Gson().toJson(it))
                            mActivity.startActivity(this)
                        }
                    }).apply {
                        binding.rvListArtis.adapter = this
                    }
                    if (mActivity.areSystemAnimationsEnabled) {
                        binding.rvListArtis.scheduleLayoutAnimation()
                    }
                }else{
                    val olderItems =(adapter as MusicArtistAdapter).data
                    if (olderItems.sortedBy { it.id }.hashCode() != artists.sortBy { it.id }.hashCode()){
                        adapter.updateItems(artists)
                    }
                }
            }
        }
    }

}