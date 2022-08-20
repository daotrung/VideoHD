package com.daotrung.myapplication.adapter

import android.content.ContentUris
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.daotrung.myapplication.R
import com.daotrung.myapplication.base.BaseViewHolder
import com.daotrung.myapplication.databinding.ItemListMusicFragmentAlbumBinding
import com.daotrung.myapplication.model.Artist
import com.daotrung.myapplication.util.AppUtils
import com.daotrung.myapplication.util.BottomSheetUtils
import java.util.ArrayList

class MusicArtistAdapter( var data: List<Artist>, val itemClick: (Any) -> Unit) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {
    inner class MyViewHolder(val view: ItemListMusicFragmentAlbumBinding) :
        BaseViewHolder<Artist>(view.root) {
        val binding = view
        override fun bind(item: Artist) {
            binding.apply {
                txtNumberSongAlbum.text = "${item.trackCnt} bài hát"
                txtTitleAlbum.text = item.title
                itemView.setOnClickListener {
                    itemClick.invoke(item)
                }
                imgMoreItemMusicAlbum.visibility = View.GONE
                val options = RequestOptions()
                    .error(R.drawable.ic_avt_artist)
                    .transform(CenterCrop(), RoundedCorners(AppUtils.dpToPx(10)))
                val artworkUri = Uri.parse("content://media/external/audio/albumart")
                val albumArtUri = ContentUris.withAppendedId(artworkUri, item.albumArtId)
                Glide.with(itemView.context)
                    .load(albumArtUri)
                    .apply(options)
                    .into(imgMusicMainAlbum)
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
    private var textToHighlight = ""
    fun updateItems(newItems: ArrayList<Artist>, highlightText: String = "", forceUpdate: Boolean = false) {
        if (forceUpdate || newItems.hashCode() != data.hashCode()) {
            data = newItems.clone() as ArrayList<Artist>
            textToHighlight = highlightText
            notifyDataSetChanged()
        } else if (textToHighlight != highlightText) {
            textToHighlight = highlightText
            notifyDataSetChanged()
        }
    }
}