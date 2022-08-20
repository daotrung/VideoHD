package com.daotrung.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.daotrung.myapplication.activity.EditVideoActivity
import com.daotrung.myapplication.activity.FolderVideoActivity
import com.daotrung.myapplication.databinding.ItemChooseFolderInVideoPrivateBinding
import com.daotrung.myapplication.model.VideoFolder
import com.daotrung.myapplication.util.POSITION_FOLDER_VIDEO
import com.daotrung.myapplication.util.POSITION_FOLDER_VIDEO_EDIT

class VideoFoloderEditAdapter (private val context : Context, private val videoFolder : ArrayList<VideoFolder>):
    RecyclerView.Adapter<VideoFoloderEditAdapter.MyViewHolder>(){

    inner class MyViewHolder(binding: ItemChooseFolderInVideoPrivateBinding): RecyclerView.ViewHolder(binding.root) {
        val img = binding.imgEditFolderVideoPrivate
        val name = binding.nameFolderPrivateVideo
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemChooseFolderInVideoPrivateBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = videoFolder[position].folderName

        holder.root.setOnClickListener {
            val intent = Intent(context, EditVideoActivity::class.java)
            intent.putExtra(POSITION_FOLDER_VIDEO_EDIT,position)
            ContextCompat.startActivity(context,intent,null)
        }
    }

    override fun getItemCount(): Int {
        return videoFolder.size
    }
}