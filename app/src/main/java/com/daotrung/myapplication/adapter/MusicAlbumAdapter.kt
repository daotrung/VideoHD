package com.daotrung.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.daotrung.myapplication.R
import com.daotrung.myapplication.base.BaseViewHolder
import com.daotrung.myapplication.databinding.ItemListMusicFragmentAlbumBinding
import com.daotrung.myapplication.databinding.ItemListMusicFragmentSongBinding
import com.daotrung.myapplication.model.Album
import com.daotrung.myapplication.model.Artist
import com.daotrung.myapplication.util.AppUtils
import com.daotrung.myapplication.util.BottomSheetUtils
import java.util.ArrayList


class MusicAlbumAdapter(var data: List<Album>, val itemClick: (Any) -> Unit) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    inner class MyViewHolder(viewItem: ItemListMusicFragmentAlbumBinding) :
        BaseViewHolder<Album>(viewItem.root) {
        val binding = viewItem
        override fun bind(item: Album) {
            binding.apply {
                txtNumberSongAlbum.text = "${item.trackCnt} bài hát"
                txtTitleAlbum.text = item.title
                itemView.setOnClickListener {
                    itemClick.invoke(item)
                }
                imgMoreItemMusicAlbum.visibility = View.GONE
                val options = RequestOptions()
                    .error(R.drawable.ic_avt_album)
                    .transform(CenterCrop(), RoundedCorners(AppUtils.dpToPx(10)))
                Glide.with(itemView.context)
                    .load(item.coverArt)
                    .apply(options)
                    .into(imgMusicMainAlbum)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val binding = ItemListMusicFragmentAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        (holder as MyViewHolder).bind(data[position])
    }

    private var textToHighlight = ""
    fun updateItems(
        newItems: ArrayList<Album>,
        highlightText: String = "",
        forceUpdate: Boolean = false
    ) {
        if (forceUpdate || newItems.hashCode() != data.hashCode()) {
            data = newItems.clone() as ArrayList<Album>
            textToHighlight = highlightText
            notifyDataSetChanged()
        } else if (textToHighlight != highlightText) {
            textToHighlight = highlightText
            notifyDataSetChanged()
        }
    }
}