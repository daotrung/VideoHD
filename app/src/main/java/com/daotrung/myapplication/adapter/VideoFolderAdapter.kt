package com.daotrung.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.daotrung.myapplication.activity.FolderVideoActivity
import com.daotrung.myapplication.model.VideoFolder
import com.daotrung.myapplication.util.BottomSheetUtils
import com.daotrung.myapplication.databinding.ItemListVideoFolderFragmentVideoBinding
import com.daotrung.myapplication.util.POSITION_FOLDER_VIDEO
class VideoFolderAdapter (private val context : Context ,private val videoFolder : ArrayList<VideoFolder>):
    RecyclerView.Adapter<VideoFolderAdapter.MyViewHolder>(){

    inner class MyViewHolder(binding: ItemListVideoFolderFragmentVideoBinding):RecyclerView.ViewHolder(binding.root) {
        val img = binding.imgVideoFolderMain
        val name = binding.txtTitleVideoFolder
        val totalVideo = binding.txtNumberVideoFolder
        val imgMore = binding.imgMoreItemFolder
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemListVideoFolderFragmentVideoBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = videoFolder[position].folderName
        holder.totalVideo.text = videoFolder[position].totalVideo.toString()
        holder.imgMore.setOnClickListener {
            BottomSheetUtils.showBottomSheetTabVideoFolder(context)
        }

        holder.root.setOnClickListener {
            val intent = Intent(context,FolderVideoActivity::class.java)
            intent.putExtra(POSITION_FOLDER_VIDEO,position)
            ContextCompat.startActivity(context,intent,null)
        }
    }

    override fun getItemCount(): Int {
        return videoFolder.size
    }
}