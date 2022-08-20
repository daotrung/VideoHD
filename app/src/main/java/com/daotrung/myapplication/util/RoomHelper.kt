package com.daotrung.myapplication.util

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.model.MusicLocal
import com.simplemobiletools.commons.extensions.getArtist
import com.simplemobiletools.commons.extensions.getDuration
import com.simplemobiletools.commons.extensions.getTitle
import com.simplemobiletools.commons.helpers.isQPlus

class RoomHelper(val context: Context) {
    fun insertTracksWithPlaylist(tracks: ArrayList<MusicLocal>) {
        App.getDB().songsDao().insertAll(tracks)
    }

    fun getTrackFromPath(path: String): MusicLocal? {
        val songs = getTracksFromPaths(arrayListOf(path), 0)
        return songs.firstOrNull()
    }

    private fun getTracksFromPaths(paths: List<String>, playlistId: Int): ArrayList<MusicLocal> {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        )

        if (isQPlus()) {
            projection.add(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
        }

        val pathsMap = HashSet<String>()
        paths.mapTo(pathsMap) { it }

        val ITEMS_PER_GROUP = 50
        val songs = ArrayList<MusicLocal>(paths.size)
//        val showFilename = context.config.showFilename

        val parts = paths.size / ITEMS_PER_GROUP
        for (i in 0..parts) {
            val sublist = paths.subList(i * ITEMS_PER_GROUP, Math.min((i + 1) * ITEMS_PER_GROUP, paths.size))
            val questionMarks = getQuestionMarks(sublist.size)
            val selection = "${MediaStore.Audio.Media.DATA} IN ($questionMarks)"
            val selectionArgs = sublist.toTypedArray()

            context.queryCursor(uri, projection.toTypedArray(), selection, selectionArgs) { cursor ->
                val mediaStoreId = cursor.getLongValue(MediaStore.Audio.Media._ID)
                val title = cursor.getStringValue(MediaStore.Audio.Media.TITLE)
                val artist = cursor.getStringValue(MediaStore.Audio.Media.ARTIST)
                val path = cursor.getStringValue(MediaStore.Audio.Media.DATA)
                val duration = cursor.getIntValue(MediaStore.Audio.Media.DURATION) / 1000
                val album = cursor.getStringValue(MediaStore.Audio.Media.ALBUM)
                val albumId = cursor.getLongValue(MediaStore.Audio.Media.ALBUM_ID)
                val coverArt = ContentUris.withAppendedId(artworkUri, albumId).toString()
                val folderName = if (isQPlus()) {
                    cursor.getStringValue(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME) ?: MediaStore.UNKNOWN_STRING
                } else {
                    ""
                }

                val song = MusicLocal(0, mediaStoreId, title, artist, path, duration, album, coverArt, playlistId, 0, folderName, albumId)
//                song.title = song.getProperTitle(showFilename)
                songs.add(song)
                pathsMap.remove(path)
            }
        }

        pathsMap.forEach {
            val unknown = MediaStore.UNKNOWN_STRING
            val title = context.getTitle(it) ?: unknown
            val song = MusicLocal(0, 0, title, context.getArtist(it) ?: unknown, it, context.getDuration(it) ?: 0, "", "", playlistId, 0, "", 0)
//            song.title = song.getProperTitle(showFilename)
            songs.add(song)
        }

        return songs
    }

    private fun getQuestionMarks(cnt: Int) = "?" + ",?".repeat(Math.max(cnt - 1, 0))
}