package com.daotrung.myapplication.adapter

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.daotrung.myapplication.R
import com.daotrung.myapplication.base.BaseViewHolder
import com.daotrung.myapplication.databinding.ItemListMusicFragmentAlbumBinding
import com.daotrung.myapplication.model.FolderMusic

class MusicFolderAdapter(private val data: List<FolderMusic>, val itemClick: (Any) -> Unit) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    inner class MyViewHolder(val view: ItemListMusicFragmentAlbumBinding) :
        BaseViewHolder<FolderMusic>(view.root) {
        val binding = view
        override fun bind(item: FolderMusic) {
            binding.apply {
                txtNumberSongAlbum.text = "${item.trackCount} bài hát"
                txtTitleAlbum.text = item.title
                itemView.setOnClickListener {
                    itemClick.invoke(item)
                }
                imgMoreItemMusicAlbum.visibility = GONE
                Glide.with(itemView.context).load(R.drawable.ic_avt_folder).into(imgMusicMainAlbum)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            ItemListMusicFragmentAlbumBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return MyViewHolder(view)
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        (holder as MyViewHolder).bind(data[position])
    }
}