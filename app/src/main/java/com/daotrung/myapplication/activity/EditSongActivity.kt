package com.daotrung.myapplication.activity

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.Observer
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.abs.App.Companion.musicViewModel
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.adapter.MusicEditAdapter
import com.daotrung.myapplication.databinding.ActivityEditSongBinding
import com.daotrung.myapplication.interfaces.EditInterface
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.util.LogInstance
import com.daotrung.myapplication.util.MusicUtil
import com.daotrung.myapplication.util.MusicUtil.deleteFromMediaStore
import com.daotrung.myapplication.util.addSongPlayNext
import com.daotrung.myapplication.util.updateAllDatabases
import com.daotrung.myapplication.util.view.ScrollLinearLayoutManager
import com.simplemobiletools.commons.extensions.deleteFromMediaStore
import com.simplemobiletools.commons.extensions.toast

class EditSongActivity : BaseActivity<ActivityEditSongBinding>(), EditInterface {

    var adapter: MusicEditAdapter? = null
    val listSongs = mutableListOf<MusicLocal>()
    var mLayout: ScrollLinearLayoutManager? = null
    private var count = 0
    private var check = false
    override fun binding(): ActivityEditSongBinding {
        return ActivityEditSongBinding.inflate(layoutInflater)
    }

    override fun initView() {
        window.statusBarColor = Color.rgb(24, 20, 40)
        mLayout = ScrollLinearLayoutManager(this)
        binding.rcvSongs.layoutManager = mLayout
        adapter = MusicEditAdapter(this)
        binding.rcvSongs.adapter = adapter
        musicViewModel.getSongs().observe(this, Observer {
            listSongs.clear()
            listSongs.addAll(it)
            adapter?.sumblist(listSongs)
        })
        setNumberSongs()
        binding.imvCheck.setOnClickListener {
            selectAll()
        }
        binding.btnShare.setOnClickListener {
            share()
        }
        binding.btnDelete.setOnClickListener {
            delete()
        }
    }

    private fun setNumberSongs() {
        count = 0
        listSongs.forEach {
            if (it.isCheck) {
                count += 1
            }
        }
        binding.tvCount.text = String.format(getString(R.string.install_number_icon), count)
    }

    override fun onItemClick(item: MusicLocal, position: Int) {
        item.isCheck = !item.isCheck
        adapter?.notifyItemChanged(position, 1)
        setNumberSongs()
    }

    private fun selectAll() {
        if (check) {
            listSongs.forEach {
                it.isCheck = false
                check = false
            }
            binding.imvCheck.setImageResource(R.drawable.ic_check_box)
        } else {
            listSongs.forEach {
                it.isCheck = true
                check = true
            }
            binding.imvCheck.setImageResource(R.drawable.ic_un_check_box)
        }
        adapter?.notifyDataSetChanged()
        setNumberSongs()
    }

    private fun share() {
        val listTemp = mutableListOf<MusicLocal>()
        listSongs.forEach {
            if (it.isCheck) {
                listTemp.add(it)
            }
        }
        startActivity(
            Intent.createChooser(
                MusicUtil.createShareSongMultiFileIntent(listTemp, this),
                null
            )
        )
    }


    private fun delete() {
        val listTemp = mutableListOf<MusicLocal>()
        listSongs.forEach {
            if (it.isCheck) {
                listTemp.add(it)
            }
        }
        runOnUiThread {
            deleteFromMediaStore(listTemp) {
                if (it) {
                    listTemp.forEach { song ->
                        App.getDB().songsDao().removeSong(song.mediaStoreId)
                    }
                    toast(getString(R.string.deleted_songs))
                    updateAllDatabases {
                        musicViewModel.forceReload()
                    }
                }
            }
        }
    }

    private fun addPlayNext(){
        val listTemp = mutableListOf<MusicLocal>()
        listSongs.forEach {
            if (it.isCheck) {
                listTemp.add(it)
            }
        }
        addSongPlayNext(listTemp){
            LogInstance.e("successful")
        }
    }

}