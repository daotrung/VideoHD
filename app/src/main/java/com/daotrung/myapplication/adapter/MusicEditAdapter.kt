package com.daotrung.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.daotrung.myapplication.R
import com.daotrung.myapplication.base.BaseViewHolder
import com.daotrung.myapplication.databinding.ItemSongEditBinding
import com.daotrung.myapplication.interfaces.EditInterface
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.util.AppUtils


class MusicEditAdapter(val callback: EditInterface) : RecyclerView.Adapter<BaseViewHolder<*>>() {
    private var data = mutableListOf<MusicLocal>()
    fun sumblist(list: List<MusicLocal>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class MyViewHolder(viewItem: ItemSongEditBinding) :
        BaseViewHolder<MusicLocal>(viewItem.root) {
        val binding = viewItem
        override fun bind(item: MusicLocal) {
            binding.apply {
                tvMusicName.text = item.title
                tvNameSinger.text = item.artist
                val options = RequestOptions()
                    .error(R.drawable.imv_music)
                    .transform(CenterCrop(), RoundedCorners(AppUtils.dpToPx(10)))
                Glide.with(this.root.context)
                    .load(item.coverArt)
                    .apply(options)
                    .into(imvAvatar)
                icCheck.setOnClickListener {
                    callback.onItemClick(item, layoutPosition)
                }

                if (item.isCheck) {
                    icCheck.setImageResource(R.drawable.ic_un_check_box)
                } else {
                    icCheck.setImageResource(R.drawable.ic_check_box)
                }
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val binding = ItemSongEditBinding.inflate(
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

}