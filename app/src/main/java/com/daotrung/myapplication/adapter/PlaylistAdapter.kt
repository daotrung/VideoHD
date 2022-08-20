package com.daotrung.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daotrung.myapplication.activity.ListVideoMusicActivity
import com.daotrung.myapplication.base.BaseViewHolder
import com.daotrung.myapplication.databinding.ItemListMusicFragmentSongBinding
import com.daotrung.myapplication.model.PlayListEntity
import com.daotrung.myapplication.databinding.ItemPlaylistBinding
class PlaylistAdapter (private val context: Context):RecyclerView.Adapter<BaseViewHolder<*>>(){

    private var data = mutableListOf<PlayListEntity>()
    private var listItem = mutableListOf<PlayListEntity>()

    fun updateList(list: List<PlayListEntity>){
        data.clear()
        data.addAll(list)
        listItem = data
        notifyDataSetChanged()
    }
    inner class MyViewHolderPlaylist(playlistItem : ItemPlaylistBinding):BaseViewHolder<PlayListEntity>(playlistItem.root){
        val binding = playlistItem
        override fun bind(item: PlayListEntity) {
            binding.apply {
                txtTitleNamePlaylist.text = item.playListName
                txtTotalPlayList.text = item.trackCount.toString()
                itemView.setOnClickListener {
                     val intent = Intent(context,ListVideoMusicActivity::class.java)
                     intent.putExtra("idPlaylist",item.playListId)
                     context.startActivity(intent)
                }
            }
        }



    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val binding = ItemPlaylistBinding.inflate(
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