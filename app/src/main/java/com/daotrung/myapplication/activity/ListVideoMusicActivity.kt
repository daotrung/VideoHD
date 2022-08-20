package com.daotrung.myapplication.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.adapter.PlaylistAdapter
import com.daotrung.myapplication.adapter.PlaylistVideoOrMusicAdapter
import com.daotrung.myapplication.databinding.ActivityListVideoMusicBinding

class ListVideoMusicActivity : BaseActivity<ActivityListVideoMusicBinding>() {

    private var playlistVideoOrMusicAdapter:PlaylistVideoOrMusicAdapter ?= null
    private var idPlaylist : Int = 0
    override fun binding(): ActivityListVideoMusicBinding {
        return ActivityListVideoMusicBinding.inflate(layoutInflater)
    }

    override fun initView() {

        idPlaylist = intent.getIntExtra("idPlaylist",0)
        val mLayout = LinearLayoutManager(this)
        binding.rvListVideoOrMusic.layoutManager = mLayout
        playlistVideoOrMusicAdapter = PlaylistVideoOrMusicAdapter(this)
        binding.rvListVideoOrMusic.adapter = playlistVideoOrMusicAdapter
        App.playListViewModel.getListVideoMusicWithId(idPlaylist).observe(this, Observer {
                responseBody ->
            this.runOnUiThread {
                responseBody?.let {
                    playlistVideoOrMusicAdapter!!.updateList(it)
                }
            }
        })

        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.addVideoMusicToPlaylist.setOnClickListener {
             Toast.makeText(this,"Toast Add Video Music",Toast.LENGTH_SHORT).show()
        }
    }

}