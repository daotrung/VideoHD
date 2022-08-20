package com.daotrung.myapplication.fragment

import android.annotation.SuppressLint
import android.content.ContextWrapper
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.activity.PlayVideoActivity
import com.daotrung.myapplication.adapter.VideoAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentVideoVideoBinding
import com.daotrung.myapplication.model.VideoLocal
import com.daotrung.myapplication.util.*
import com.daotrung.myapplication.viewmodel.VideoViewModel
import com.simplemobiletools.commons.extensions.toInt
import kotlinx.android.synthetic.main.fragment_video_video.*
import java.io.File
import kotlin.math.roundToInt

class VideoFragmentVideo : BaseFragment() {


    private lateinit var binding: FragmentVideoVideoBinding
    var adapter: VideoAdapter? = null
    private lateinit var videoViewModel: VideoViewModel

    companion object {
        val tempList = ArrayList<VideoLocal>()
        var searchList = ArrayList<VideoLocal>()
        var search: Boolean = false


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoVideoBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoViewModel = VideoViewModel()
        checkLastVideoPlayer()
        initView()
    }

    private fun initView() {
        setAdapter()
        setOnclickListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter() {
        binding.rvListTabVideo.setHasFixedSize(true)
        binding.rvListTabVideo.setItemViewCacheSize(10)
        adapter = VideoAdapter(requireContext())
        binding.rvListTabVideo.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListTabVideo.adapter = adapter
        videoViewModel.getAllVideos(requireContext()).observe(_mActivity, Observer {responseBody->
            _mActivity.runOnUiThread {
                responseBody?.let {
                    adapter?.setListData(it)
                }
            }
        })
        if (!binding.rvListTabVideo.isComputingLayout && rvListTabVideo.scrollState == SCROLL_STATE_IDLE)
            binding.rvListTabVideo.adapter?.notifyDataSetChanged()
    }

    private fun setOnclickListener() {
        binding.nowPlayingBtn.setOnClickListener {
            val intent = Intent(requireContext(), PlayVideoActivity::class.java)
            intent.putExtra(CLASS_ITEM_VIDEO_PLAY, "NowPlaying")
            startActivity(intent)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        binding.rvListTabVideo.adapter?.notifyDataSetChanged()
        super.onResume()
        checkLastVideoPlayer()

    }


    private fun checkLastVideoPlayer() {
        val checkVi = UseSharedPreferences.getBooleanPreferences(
            PREFS_SHOW_LAST_PLAY_VIDEO, requireContext(),
            KEY_SHOW_LAST_PVIDEO
        )
        if (PlayVideoActivity.position != -1 && checkVi)
            binding.nowPlayingBtn.visibility = View.VISIBLE
    }
}