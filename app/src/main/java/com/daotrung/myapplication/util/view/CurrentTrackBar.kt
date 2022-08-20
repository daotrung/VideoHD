package com.daotrung.myapplication.util.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.provider.MediaStore
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.daotrung.myapplication.R
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.util.PLAYPAUSE
import com.daotrung.myapplication.util.sendIntent
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.view_current_song.view.*


class CurrentTrackBar(context: Context, attributeSet: AttributeSet) :
    RelativeLayout(context, attributeSet) {


    fun updateColors() {
        background = ColorDrawable(context.getProperBackgroundColor())
        current_song_label.setTextColor(context.getProperTextColor())
        current_song_play_pause.setOnClickListener {
            context.sendIntent(PLAYPAUSE)
        }
    }

    fun updateCurrentTrack(track: MusicLocal?) {
        if (track == null) {
            fadeOut()
            return
        } else {
            fadeIn()
        }

        val artist =
            if (track.artist.trim().isNotEmpty() && track.artist != MediaStore.UNKNOWN_STRING) {
                " • ${track.artist}"
            } else {
                ""
            }

        current_song_label.text = "${track.title}$artist"
        val cornerRadius = resources.getDimension(R.dimen.rounded_corner_radius_small).toInt()
        val currentTrackPlaceholder = resources.getColoredDrawableWithColor(
            R.drawable.ic_headset,
            context.getProperTextColor()
        )
        val options = RequestOptions()
            .error(currentTrackPlaceholder)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))

        Glide.with(this)
            .load(track.coverArt)
            .apply(options)
            .into(findViewById(R.id.current_song_image))
    }

    fun updateTrackState(isPlaying: Boolean) {
        current_song_play_pause.updatePlayPauseIcon(isPlaying, context.getProperTextColor())
    }
}