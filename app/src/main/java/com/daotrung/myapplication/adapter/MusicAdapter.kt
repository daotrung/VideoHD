package com.daotrung.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.daotrung.myapplication.R
import com.daotrung.myapplication.base.BaseViewHolder
import com.daotrung.myapplication.databinding.ItemListMusicFragmentSongBinding
import com.daotrung.myapplication.interfaces.SongInterface
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.util.AppUtils
import com.daotrung.myapplication.util.BottomSheetUtils
import com.simplemobiletools.commons.extensions.getFormattedDuration

class MusicAdapter(val itemClick: (MusicLocal) -> Unit, val callBack: SongInterface) :
    RecyclerView.Adapter<BaseViewHolder<*>>(), Filterable {
    private var data = mutableListOf<MusicLocal>()
    private var listTemp = mutableListOf<MusicLocal>()
    fun sumblist(list: List<MusicLocal>) {
        data.clear()
        data.addAll(list)
        listTemp = data
        notifyDataSetChanged()
    }

    inner class MyViewHolder(viewItem: ItemListMusicFragmentSongBinding) :
        BaseViewHolder<MusicLocal>(viewItem.root) {
        val binding = viewItem
        override fun bind(item: MusicLocal) {
            binding.apply {
                tvMusicName.text = item.title
                tvNameSinger.text = item.artist
                txtTimeSong.text = item.duration.getFormattedDuration()
                val options = RequestOptions()
                    .error(R.drawable.imv_music)
                    .transform(CenterCrop(), RoundedCorners(AppUtils.dpToPx(10)))

                Glide.with(this.root.context)
                    .load(item.coverArt)
                    .apply(options)
                    .into(imvAvatar)
                itemView.setOnClickListener {
                    itemClick.invoke(item)
                }
                imgMoreItemMusic.setOnClickListener {
                    BottomSheetUtils.showBottomSheetMusic(
                        itemView.context,
                        item, callBack
                    )
                }
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val binding = ItemListMusicFragmentSongBinding.inflate(
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

    override fun getFilter(): Filter {
        return searchFilter
    }

    private val searchFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val charString = constraint.toString()
            data = if (charString.isEmpty()) {
                listTemp
            } else {
                val filteredList: MutableList<MusicLocal> = ArrayList()
                for (item in listTemp) {
                    if (item.title.toLowerCase()
                            .contains(charString.toLowerCase().trim())
                    ) {
                        filteredList.add(item)
                    }
                }
                filteredList
            }
            val results = FilterResults()
            results.values = data
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            data = results.values as MutableList<MusicLocal>
            notifyDataSetChanged()
        }
    }
}