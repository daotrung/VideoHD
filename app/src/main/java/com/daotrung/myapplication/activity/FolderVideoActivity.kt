package com.daotrung.myapplication.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore
import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.adapter.VideoAdapter
import com.daotrung.myapplication.databinding.ActivityFolderVideoBinding
import com.daotrung.myapplication.model.VideoLocal
import com.daotrung.myapplication.util.POSITION_FOLDER_VIDEO
import com.daotrung.myapplication.util.GetAllListFolderUtils
import kotlinx.android.synthetic.main.activity_folder_video.*
import kotlinx.android.synthetic.main.fragment_video_video.*
import java.io.File


class FolderVideoActivity : BaseActivity<ActivityFolderVideoBinding>() {

    var pos: Int = 0

    companion object {
        lateinit var currentFolderVideos: ArrayList<VideoLocal>
    }

    private var adapter: VideoAdapter? = null
    override fun binding(): ActivityFolderVideoBinding {
        return ActivityFolderVideoBinding.inflate(layoutInflater)
    }

    override fun initView() {

        getIntentFromAdapter()
        setAdapter()
        setOnClickListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter() {

        currentFolderVideos = GetAllListFolderUtils.getAllVideos(
            this,
            GetAllListFolderUtils.getAllVideosInFolder(this)[pos].id
        )
        binding.rvFolderVideoList.setHasFixedSize(true)
        binding.rvFolderVideoList.setItemViewCacheSize(10)
        binding.rvFolderVideoList.layoutManager = LinearLayoutManager(this@FolderVideoActivity)
        adapter = VideoAdapter(this@FolderVideoActivity, isFolder = true)
        binding.rvFolderVideoList.adapter = adapter
        adapter?.setListData(currentFolderVideos)

    }

    private fun getIntentFromAdapter() {
        pos = intent.getIntExtra(POSITION_FOLDER_VIDEO, 0)
        binding.txtNameFolderTextView.text =
            GetAllListFolderUtils.getAllVideosInFolder(this)[pos].folderName
    }

    private fun setOnClickListener() {
        binding.icLeftBackFolderVideo.setOnClickListener {
            onBackPressed()
        }

    }


}