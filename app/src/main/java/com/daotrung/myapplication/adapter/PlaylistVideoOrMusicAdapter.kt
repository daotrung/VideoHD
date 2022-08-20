package com.daotrung.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daotrung.myapplication.base.BaseViewHolder
import com.daotrung.myapplication.databinding.ItemListVideoMusicInListPlayBinding
import com.daotrung.myapplication.model.PlayListEntity
import com.daotrung.myapplication.model.PlaylistWithVideoMusic

class PlaylistVideoOrMusicAdapter (private val context: Context):
    RecyclerView.Adapter<BaseViewHolder<*>>(){

    private var data = mutableListOf<PlaylistWithVideoMusic>()
    private var listItem = mutableListOf<PlaylistWithVideoMusic>()

    fun updateList(list: List<PlaylistWithVideoMusic>){
        data.clear()
        data.addAll(list)
        listItem = data
        notifyDataSetChanged()
    }
    inner class MyViewHolderPlaylist(playlistItem : ItemListVideoMusicInListPlayBinding):
        BaseViewHolder<PlaylistWithVideoMusic>(playlistItem.root){
        val binding = playlistItem

        override fun bind(item: PlaylistWithVideoMusic) {
            binding.apply {
                txtMusicOrVideoName.text = item.nameVideoOrMusic
            }
        }


    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val binding = ItemListVideoMusicInListPlayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolderPlaylist(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        (holder as MyViewHolderPlaylist).bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}