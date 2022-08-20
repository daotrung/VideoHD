package com.daotrung.myapplication.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Adapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.daotrung.myapplication.R
import com.daotrung.myapplication.interfaces.SongInterface
import com.daotrung.myapplication.model.Album
import com.daotrung.myapplication.model.Artist
import com.daotrung.myapplication.model.MusicLocal
import com.google.android.material.bottomsheet.BottomSheetDialog

object BottomSheetUtils {


    fun showBottomSheetMusic(
        context: Context,
        item: Any,
        callBack: SongInterface
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(R.layout.custom_bottom_sheet_more_video_list)

        bottomSheetDialog.show()
        val btnPlayNext = bottomSheetDialog.findViewById<LinearLayout>(R.id.play_next_item)
        val btnAddToPlaylist =
            bottomSheetDialog.findViewById<LinearLayout>(R.id.add_to_playlist_item)
        val btnSetRing = bottomSheetDialog.findViewById<LinearLayout>(R.id.set_ringtone_item)
        val btnDelete = bottomSheetDialog.findViewById<LinearLayout>(R.id.delete_item)
        val btnFav = bottomSheetDialog.findViewById<LinearLayout>(R.id.favourite_item)
        val btnShare = bottomSheetDialog.findViewById<LinearLayout>(R.id.share_item)
        val textPopupVideoName = bottomSheetDialog.findViewById<TextView>(R.id.name_video_popup)

        val titleToUse =
            (item as MusicLocal).title ?: (item as Album)?.title ?: (item as Artist).title ?: ""

        textPopupVideoName?.text = titleToUse
        btnPlayNext?.setOnClickListener {
            Toast.makeText(context, "Play Next", Toast.LENGTH_SHORT).show()
            callBack.playNext(item)
            bottomSheetDialog.dismiss()
        }
        btnAddToPlaylist?.setOnClickListener {
            Toast.makeText(context, "Add to play list", Toast.LENGTH_SHORT).show()
            callBack.addPlayList(item)
            bottomSheetDialog.dismiss()
        }
        btnSetRing?.setOnClickListener {
            callBack.setRingTone(item)
            bottomSheetDialog.dismiss()
        }
        btnDelete?.setOnClickListener {
            callBack.delete(item)
            bottomSheetDialog.dismiss()
        }
        btnFav?.setOnClickListener {
            callBack.setFavourite(item)
            bottomSheetDialog.dismiss()
        }
        btnShare?.setOnClickListener {
            callBack.share(item)
            bottomSheetDialog.dismiss()
        }

    }

    fun showBottomSheetTabVideoFolder(context: Context) {
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(R.layout.custom_bottom_sheet_more_video_list_folder)

        bottomSheetDialog.show()
        val btnPlayNext = bottomSheetDialog.findViewById<LinearLayout>(R.id.play_video_item_folder)
        val btnAddToPlaylist =
            bottomSheetDialog.findViewById<LinearLayout>(R.id.add_to_playlist_item_folder)
        val btnSetLock = bottomSheetDialog.findViewById<LinearLayout>(R.id.ic_private_lock)
        val btnDelete = bottomSheetDialog.findViewById<LinearLayout>(R.id.delete_item_video_folder)
        val btnRename = bottomSheetDialog.findViewById<LinearLayout>(R.id.rename_item_folder)

        btnPlayNext?.setOnClickListener {
            Toast.makeText(context, "Play List Video", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }
        btnAddToPlaylist?.setOnClickListener {
            Toast.makeText(context, "Add to play list", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }
        btnSetLock?.setOnClickListener {
            Toast.makeText(context, "Set Lock", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }
        btnDelete?.setOnClickListener {
            Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }
        btnRename?.setOnClickListener {
            Toast.makeText(context, "Rename", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }


    }
}