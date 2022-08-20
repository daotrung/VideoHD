package com.daotrung.myapplication.activity

import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App.Companion.musicViewModel
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.adapter.MusicAdapter
import com.daotrung.myapplication.adapter.VideoAdapter
import com.daotrung.myapplication.databinding.ActivitySearchVideoBinding
import com.daotrung.myapplication.fragment.VideoFragmentVideo
import com.daotrung.myapplication.interfaces.SongInterface
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.util.LogInstance
import com.daotrung.myapplication.util.resetQueueItems
import com.daotrung.myapplication.viewmodel.VideoViewModel

class SearchVideoActivity : BaseActivity<ActivitySearchVideoBinding>(), SongInterface {

    private lateinit var adapter: VideoAdapter
    private lateinit var adapterSong: MusicAdapter

    private lateinit var videoViewModel: VideoViewModel
    override fun binding(): ActivitySearchVideoBinding {
        return ActivitySearchVideoBinding.inflate(layoutInflater)
    }

    private val type by lazy {
        intent.getIntExtra("TYPE", 0)
    }

    override fun initView() {
        videoViewModel = VideoViewModel()
        binding.rvListSearchVideo.setHasFixedSize(true)
        binding.rvListSearchVideo.setItemViewCacheSize(10)
        binding.rvListSearchVideo.layoutManager = LinearLayoutManager(this)
        if (type == 1) {
            setupSongView()
        } else {
            setRecyclerview()
        }
    }

    private fun setRecyclerview() {
        adapter = VideoAdapter(this)
        binding.rvListSearchVideo.adapter = adapter
        videoViewModel.getAllVideos(this).observe(this, Observer {
            adapter.setListData(it)
        })
    }

    private fun setupSongView() {
        adapterSong = MusicAdapter(itemClick = {
            SongPlayingActivity.launch(this, it)
        }, this)
        binding.rvListSearchVideo.adapter = adapterSong
        musicViewModel.getSongs().observe(this, Observer {
            resetQueueItems(it) {}
            adapterSong.sumblist(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.search_view_menu_video, menu)
        val searchItem = menu?.findItem(R.id.search_view_video)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText != null) {
                    if (type == 1) {
                        adapterSong.filter.filter(newText.toString().trim())
                    } else {
                        VideoFragmentVideo.searchList = ArrayList()
                        for (video in VideoFragmentVideo.tempList) {
                            if (video.nameVideo.lowercase().contains(newText.lowercase()))
                                VideoFragmentVideo.searchList.add(video)

                        }
                        VideoFragmentVideo.search = true
                        adapter.updateList(searchList = VideoFragmentVideo.searchList)
                    }

                }

                return true
            }


        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun playNext(song: MusicLocal) {
    }

    override fun addPlayList(song: MusicLocal) {
    }

    override fun setRingTone(song: MusicLocal) {
    }

    override fun delete(song: MusicLocal) {
    }

    override fun setFavourite(song: MusicLocal) {
    }

    override fun share(song: MusicLocal) {
    }

}