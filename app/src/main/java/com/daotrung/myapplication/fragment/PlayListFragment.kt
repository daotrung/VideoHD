package com.daotrung.myapplication.fragment

import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.activity.SongPlayingActivity
import com.daotrung.myapplication.adapter.MusicAdapter
import com.daotrung.myapplication.adapter.PlaylistAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentPlayListBinding
import com.daotrung.myapplication.databinding.RenameFieldBinding
import com.daotrung.myapplication.databinding.NewPlaylistBinding
import com.daotrung.myapplication.model.PlayListEntity
import com.daotrung.myapplication.util.GetAllListFolderUtils
import com.daotrung.myapplication.util.resetQueueItems
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


class PlayListFragment : BaseFragment() {

    private lateinit var binding: FragmentPlayListBinding
    var count = 0
    companion object {
        fun newInstance(): PlayListFragment {
            return PlayListFragment()
        }
    }
    private var playlistAdapter: PlaylistAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        super.onViewCreated(view, savedInstanceState)
        setOnClickListener()

    }

    private fun setOnClickListener() {

         binding.addPlaylist.setOnClickListener {
             setAddPlaylist()
         }
    }

    private fun setAddPlaylist() {
        diloagEditNamePlaylist()
    }

    private fun diloagEditNamePlaylist() {
        val customDialogRF =
            LayoutInflater.from(context).inflate(R.layout.new_playlist, binding.root, false)
        val bindingRF = NewPlaylistBinding.bind(customDialogRF)
        val dialogRF = MaterialAlertDialogBuilder(requireContext()).setView(customDialogRF)
            .setCancelable(false)
            .setMessage("Create New Playlist")
            .setPositiveButton("New Playlist ") { dialog, _ ->
                if(bindingRF.renameList.text.toString() ==  ""){
                    App.playListViewModel.insertPlayList("New Playlist ${count++}")
                    App.playListViewModel.getPlayList().observe(requireActivity(), Observer {
                            responseBody ->
                        mActivity.runOnUiThread {
                            responseBody?.let {
                                playlistAdapter?.updateList(it)
                            }
                        }
                    })
                }else{
                    App.playListViewModel.insertPlayList(bindingRF.renameList.text.toString())
                    App.playListViewModel.getPlayList().observe(requireActivity(), Observer {
                            responseBody ->
                        mActivity.runOnUiThread {
                            responseBody?.let {
                                playlistAdapter?.updateList(it)
                            }
                        }
                    })
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialogRF.show()
    }


    private fun initView() {
        val mLayout = LinearLayoutManager(mContext)
        binding.rvPlaylist.layoutManager = mLayout
        playlistAdapter = PlaylistAdapter(requireContext())
        binding.rvPlaylist.adapter = playlistAdapter
        App.playListViewModel.getPlayList().observe(requireActivity(), Observer {
            responseBody ->
            mActivity.runOnUiThread {
                responseBody?.let {
                    playlistAdapter?.updateList(it)
                }
            }
        })
    }

}