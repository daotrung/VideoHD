package com.daotrung.myapplication.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App.Companion.musicViewModel
import com.daotrung.myapplication.adapter.MusicAdapter
import com.daotrung.myapplication.databinding.FragmentMusicSongBinding
import com.daotrung.myapplication.interfaces.SongInterface
import com.daotrung.myapplication.model.MusicLocal
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ListSongFragment : BottomSheetDialogFragment(), SongInterface {

    private var mSongListener: SongListener? = null

    interface SongListener {
        fun songClick(song: MusicLocal)
    }

    fun setSongListener(songListener: SongListener) {
        mSongListener = songListener
    }

    private lateinit var binding: FragmentMusicSongBinding

    private val mBottomSheetDialogFragment: BottomSheetBehavior.BottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        }

    @SuppressLint("RestrictedApi", "ResourceAsColor")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        binding = FragmentMusicSongBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        val params = binding.root.layoutParams
        if (params != null && params is BottomSheetBehavior<*>) {
            params.setBottomSheetCallback(mBottomSheetDialogFragment)
        }
        val mLayout = LinearLayoutManager(context)
        binding.rvListTabSong.layoutManager = mLayout
        val adapter = MusicAdapter(itemClick = {
            mSongListener?.songClick(it)
            dialog.dismiss()
        }, this)
        binding.rvListTabSong.adapter = adapter
        musicViewModel.getSongs().observe(requireActivity(), Observer {
            adapter.sumblist(it)
        })
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