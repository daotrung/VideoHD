package com.daotrung.myapplication.util

import android.app.Activity
import com.daotrung.myapplication.dialog.DialogSelectPlayList
import com.daotrung.myapplication.model.MusicLocal
import com.simplemobiletools.commons.helpers.ensureBackgroundThread

fun Activity.addSongsToPlaylist(tracks: List<MusicLocal>, callback: () -> Unit) {
    DialogSelectPlayList(this) { playlistId ->
        val tracksToAdd = ArrayList<MusicLocal>()
        tracks.forEach {
            it.id = 0
            it.playListId = playlistId
            tracksToAdd.add(it)
        }

        ensureBackgroundThread {
            RoomHelper(this).insertTracksWithPlaylist(tracksToAdd)

            runOnUiThread {
                callback()
            }
        }
    }
}