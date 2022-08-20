package com.daotrung.myapplication.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.daotrung.myapplication.model.MusicLocal
import com.simplemobiletools.commons.extensions.getIsPathDirectory
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import java.io.File


object MusicUtil {
    fun createShareSongMultiFileIntent(song: List<MusicLocal>, context: Context): Intent? {
        val files: ArrayList<Uri> = ArrayList()
        return try {
            for (path in song) {
                val uri = FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName,
                    File(path.path)
                )
                files.add(uri)
            }
            Intent().setAction(Intent.ACTION_SEND_MULTIPLE).putExtra(
                Intent.EXTRA_STREAM,
                files
            ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).setType("audio/*")
        } catch (e: IllegalArgumentException) {
            for (path in song) {
                val uri = getSongFileUri(path.mediaStoreId)
                files.add(uri)
            }
            Intent().setAction(Intent.ACTION_SEND_MULTIPLE).putExtra(
                Intent.EXTRA_STREAM,
                files
            ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).setType("audio/*")
        }
    }

    fun milliHzToString(milliHz: Int): String {
        if (milliHz < 1000) return ""
        return if (milliHz < 1000000) "" + milliHz / 1000 + "Hz" else "" + milliHz / 1000000 + "kHz"
    }

    fun Context.deleteFromMediaStore(
        song: List<MusicLocal>,
        callback: ((needsRescan: Boolean) -> Unit)? = null
    ) {
        song.forEach {
            if (getIsPathDirectory(it.path)) {
                callback?.invoke(false)
                return
            }
        }

        ensureBackgroundThread {
            try {
                val where = "${MediaStore.MediaColumns.DATA} = ?"
                val files: ArrayList<Uri> = ArrayList()
                val args: ArrayList<String> = ArrayList()
                for (path in song) {
                    val uri = getSongFileUri(path.mediaStoreId)
                    files.add(uri)
                    args.add(path.path)
                }
                files.forEach { uri ->
                    args.forEach {
                        val success = contentResolver.delete(uri, where, arrayOf(it)) != 1
                        callback?.invoke(success)
                    }
                }
            } catch (ignored: Exception) {
                Log.e("ATG", ignored.message.toString())
            }
            callback?.invoke(true)
        }
    }
}