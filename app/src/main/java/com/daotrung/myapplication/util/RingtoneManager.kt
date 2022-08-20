package com.daotrung.myapplication.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.daotrung.myapplication.R
import com.daotrung.myapplication.model.MusicLocal
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simplemobiletools.commons.extensions.toast
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object RingtoneManager {
    fun setRingtone(context: Context, song: MusicLocal) {
        val uri = getSongFileUri(song.mediaStoreId)
        val resolver = context.contentResolver
        try {
            val cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.TITLE),
                BaseColumns._ID + "=?",
                arrayOf(song.mediaStoreId.toString()), null
            )
            cursor.use { cursorSong ->
                if (cursorSong != null && cursorSong.count == 1) {
                    cursorSong.moveToFirst()
                    Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString())
                    val message = context
                        .getString(R.string.x_has_been_set_as_ringtone, cursorSong.getString(0))
                    context.toast(message)
                }
            }
        } catch (ignored: SecurityException) {
            LogInstance.e(ignored.message.toString())
        }
    }



    fun requiresDialog(context: Context): Boolean {
        if (VersionUtils.hasMarshmallow()) {
            if (!Settings.System.canWrite(context)) {
                return true
            }
        }
        return false
    }

    fun showDialog(context: Context) {
        return MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialogTheme)
            .setTitle(R.string.dialog_title_set_ringtone)
            .setMessage(R.string.dialog_message_set_ringtone)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = ("package:" + context.applicationContext.packageName).toUri()
                context.startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().show()
    }

    fun setAsRingtoneAndroid(context: Context,k: File) : Boolean {
        return try {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.TITLE, k.name)
            values.put(
                MediaStore.MediaColumns.MIME_TYPE,
                getMIMEType(k.absolutePath)
            ) //// getMIMEType(k.getAbsolutePath())
            values.put(MediaStore.MediaColumns.SIZE, k.length())
            values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name)
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val newUri: Uri? = context.contentResolver
                    .insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                try {
                    newUri?.let {
                        context.contentResolver.openOutputStream(it).use { os ->
                            val size = k.length().toInt()
                            val bytes = ByteArray(size)
                            try {
                                val buf = BufferedInputStream(FileInputStream(k))
                                buf.read(bytes, 0, bytes.size)
                                buf.close()
                                os?.write(bytes)
                                os?.close()
                                os?.flush()
                            } catch (e: IOException) {
                            }
                        }
                    }
                } catch (ignored: java.lang.Exception) {
                }
                RingtoneManager.setActualDefaultRingtoneUri(
                    context,
                    RingtoneManager.TYPE_RINGTONE,
                    Uri.fromFile(k)
                )
                true
            }else{
                values.put(MediaStore.MediaColumns.DATA, k.absolutePath)
                val uri = MediaStore.Audio.Media.getContentUriForPath(
                    k
                        .absolutePath
                )
                context.contentResolver.delete(
                    uri!!,
                    MediaStore.MediaColumns.DATA + "=\"" + k.absolutePath + "\"",
                    null
                )
                val newUri: Uri = context.contentResolver.insert(uri, values)!!
                RingtoneManager.setActualDefaultRingtoneUri(
                    context, RingtoneManager.TYPE_RINGTONE,
                    newUri
                )
                context.contentResolver
                    .insert(
                        MediaStore.Audio.Media.getContentUriForPath(
                            k
                                .absolutePath
                        )!!, values
                    )
                true
            }
        } catch (e: Exception) {
            return false
        }
    }

    fun getMIMEType(url: String?): String? {
        var mType: String? = null
        val mExtension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (mExtension != null) {
            mType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mExtension)
        }
        return mType
    }
}