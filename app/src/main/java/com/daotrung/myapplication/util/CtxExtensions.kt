package com.daotrung.myapplication.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.abs.App.Companion.getDB
import com.daotrung.myapplication.base.ConfigBase
import com.daotrung.myapplication.model.Album
import com.daotrung.myapplication.model.Artist
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.model.QueueItem
import com.daotrung.myapplication.service.MusicService
import com.simplemobiletools.commons.extensions.getFileUri
import com.simplemobiletools.commons.extensions.getIsPathDirectory
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.helpers.isQPlus
import java.io.File


val Context.config: ConfigBase get() = ConfigBase.newInstance(applicationContext)
fun getSongFileUri(songId: Long): Uri {
    return ContentUris.withAppendedId(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        songId
    )
}

fun timeFormat(duration: Int): String {
    return when {
        duration / 60 == 0 -> {
            if (duration < 10) {
                "00:0$duration"
            } else {
                "00:$duration"
            }
        }
        duration / 60 < 10 -> {
            val minutes = duration / 60
            val seconds = duration % 60
            if (seconds < 10) {
                "0$minutes:0$seconds"
            } else "0$minutes:$seconds"
        }
        else -> {
            val minutes = duration / 60
            val seconds = duration % 60
            if (seconds < 10) {
                "$minutes:0$seconds"
            } else "$minutes:$seconds"
        }
    }
}

fun Context.resetQueueItems(newTracks: List<MusicLocal>, callback: () -> Unit) {
    ensureBackgroundThread {
        getDB().queueDao().deleteAllItems()
        addQueueItems(newTracks, callback)
    }
}

fun Activity.addSongPlayNext(tracks: List<MusicLocal>, callback: () -> Unit) {
    addQueueItems(tracks) {
        tracks.forEach { track ->
            if (MusicService.mListSongs.none { it.mediaStoreId == track.mediaStoreId }) {
                MusicService.mListSongs.add(track)
            }
        }

        runOnUiThread {
            callback()
        }
    }
}
fun Context.addQueueItems(newTracks: List<MusicLocal>, callback: () -> Unit) {
    ensureBackgroundThread {
        val itemsToInsert = ArrayList<QueueItem>()
        var order = 0
        newTracks.forEach {
            val queueItem = QueueItem(it.mediaStoreId, order++, false, 0)
            itemsToInsert.add(queueItem)
        }

        getDB().songsDao().insertAll(newTracks)
        getDB().queueDao().insertAll(itemsToInsert)
        sendIntent(UPDATE_QUEUE_SIZE)
        callback()
    }
}

fun Context.removeQueueItems(tracks: List<MusicLocal>, callback: () -> Unit) {
    ensureBackgroundThread {
        tracks.forEach {
            getDB().queueDao().removeQueueItem(it.mediaStoreId)
            MusicService.mListSongs.remove(it)
        }
        callback()
    }
}

fun Context.queryCursor(
    uri: Uri,
    projection: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    showErrors: Boolean = false,
    callback: (cursor: Cursor) -> Unit
) {
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    callback(cursor)
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {
        if (showErrors) {
            toast(e.message.toString())
        }
    }
}


fun Context.updateAllDatabases(callback: () -> Unit) {
    ensureBackgroundThread {
        updateCachedArtists { artists ->
            updateCachedAlbums(artists)
            callback()
        }
    }
}

fun Context.updateCachedAlbums(artists: ArrayList<Artist>) {
    val albums = ArrayList<Album>()
    artists.forEach { artist ->
        albums.addAll(getAlbumsSync(artist))
    }

    albums.forEach { album ->
        if (album.title.length > 1) {
            getDB().albumsDao().insert(album)
        }
    }

    val cachedAlbums = getDB().albumsDao().getAll() as ArrayList<Album>
    val newIds = albums.map { it.id }
    val idsToRemove = arrayListOf<Long>()
    cachedAlbums.forEach { album ->
        if (!newIds.contains(album.id)) {
            idsToRemove.add(album.id)
        }
    }

    idsToRemove.forEach {
        getDB().albumsDao().deleteAlbum(it)
    }

    updateCachedTracks(albums)
}

fun Context.updateCachedTracks(albums: ArrayList<Album>) {
    getDB().songsDao().delete()
    val tracks = ArrayList<MusicLocal>()
    albums.forEach { album ->
        tracks.addAll(getAlbumTracksSync(album.id))
    }
    tracks.forEach { track ->
        if (track.duration != 0) {
            getDB().songsDao().insert(track)
        }
    }
    val cachedTracks = getDB().songsDao().getAll()
    val newIds = tracks.map { it.mediaStoreId }
    val idsToRemove = arrayListOf<Long>()
    cachedTracks.forEach { track ->
        if (!newIds.contains(track.mediaStoreId)) {
            idsToRemove.add(track.mediaStoreId)
        }
    }
    idsToRemove.forEach {
        getDB().songsDao().removeSong(it)
    }

}

fun Context.getAlbumTracksSync(albumId: Long): ArrayList<MusicLocal> {
    val tracks = ArrayList<MusicLocal>()
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val projection = arrayListOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.TRACK
    )

    if (isQPlus()) {
        projection.add(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
    }

    val selection = "${MediaStore.Audio.Albums.ALBUM_ID} = ?"
    val selectionArgs = arrayOf(albumId.toString())
    val coverUri = ContentUris.withAppendedId(artworkUri, albumId)
    val coverArt = coverUri.toString()
    val showFilename = config.showFilename

    queryCursor(
        uri,
        projection.toTypedArray(),
        selection,
        selectionArgs,
        showErrors = true
    ) { cursor ->
        val id = cursor.getLongValue(MediaStore.Audio.Media._ID)
        val title = cursor.getStringValue(MediaStore.Audio.Media.TITLE)
        val duration = cursor.getIntValue(MediaStore.Audio.Media.DURATION) / 1000
        val trackId = cursor.getIntValue(MediaStore.Audio.Media.TRACK) % 1000
        val path = cursor.getStringValue(MediaStore.Audio.Media.DATA)
        val artist =
            cursor.getStringValue(MediaStore.Audio.Media.ARTIST) ?: MediaStore.UNKNOWN_STRING
        val album = cursor.getStringValue(MediaStore.Audio.Media.ALBUM)
        val folderName = File(path).parentFile.name
        if (duration > 0) {
            val track = MusicLocal(
                0,
                id,
                title,
                artist,
                path,
                duration,
                album,
                coverArt,
                0,
                trackId,
                folderName,
                albumId
            )
            track.title = track.getProperTitle(showFilename)
            tracks.add(track)
        }
    }

    return tracks
}


fun Context.updateCachedArtists(callback: (artists: ArrayList<Artist>) -> Unit) {
    val artists = getArtistsSync()
    artists.forEach { artist ->
        getDB().artistsDao().insert(artist)
    }

    // remove invalid artists from cache
    val cachedArtists = getDB().artistsDao().getAll() as ArrayList<Artist>
    val newIds = artists.map { it.id }
    val idsToRemove = arrayListOf<Long>()
    cachedArtists.forEach { artist ->
        if (!newIds.contains(artist.id)) {
            idsToRemove.add(artist.id)
        }
    }

    idsToRemove.forEach {
        getDB().artistsDao().deleteArtist(it)
    }

    callback(artists)
}

fun Context.getArtistsSync(): ArrayList<Artist> {
    val artists = ArrayList<Artist>()
    val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.Artists._ID,
        MediaStore.Audio.Artists.ARTIST
    )

    queryCursor(uri, projection, showErrors = true) { cursor ->
        val id = cursor.getLongValue(MediaStore.Audio.Artists._ID)
        val title =
            cursor.getStringValue(MediaStore.Audio.Artists.ARTIST) ?: MediaStore.UNKNOWN_STRING
        var artist = Artist(id, title, 0, 0, 0)
        artist = fillArtistExtras(this, artist)
        if (artist.albumCnt > 0 && artist.trackCnt > 0) {
            artists.add(artist)
        }
    }

    return artists
}


private fun fillArtistExtras(context: Context, artist: Artist): Artist {
    val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Audio.Albums._ID)
    val selection = "${MediaStore.Audio.Albums.ARTIST_ID} = ?"
    val selectionArgs = arrayOf(artist.id.toString())

    artist.albumCnt = context.getAlbumsCount(artist)

    context.queryCursor(uri, projection, selection, selectionArgs) { cursor ->
        val albumId = cursor.getLongValue(MediaStore.Audio.Albums._ID)
        if (artist.albumArtId == 0L) {
            artist.albumArtId = albumId
        }
        if (context.getAlbumTracksCount(albumId) > 0) {
            artist.trackCnt += context.getAlbumTracksCount(albumId)
        }
    }

    return artist
}

fun Context.getAlbumsSync(artist: Artist): ArrayList<Album> {
    val albums = ArrayList<Album>()
    val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.Albums._ID,
        MediaStore.Audio.Albums.ARTIST,
        MediaStore.Audio.Albums.FIRST_YEAR,
        MediaStore.Audio.Albums.ALBUM
    )

    var selection = "${MediaStore.Audio.Albums.ARTIST} = ?"
    var selectionArgs = arrayOf(artist.title)

    if (isQPlus()) {
        selection = "${MediaStore.Audio.Albums.ARTIST_ID} = ?"
        selectionArgs = arrayOf(artist.id.toString())
    }

    queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
        val id = cursor.getLongValue(MediaStore.Audio.Albums._ID)
        val artistName =
            cursor.getStringValue(MediaStore.Audio.Albums.ARTIST) ?: MediaStore.UNKNOWN_STRING
        val title = cursor.getStringValue(MediaStore.Audio.Albums.ALBUM)
        val coverArt = ContentUris.withAppendedId(artworkUri, id).toString()
        val year = cursor.getIntValue(MediaStore.Audio.Albums.FIRST_YEAR)
        val trackCnt = getAlbumTracksCount(id)
        if (title.length > 1) {
            val album = Album(id, artistName, title, coverArt, year, trackCnt, artist.id)
            albums.add(album)
        }
    }

    return albums
}

fun Context.getAlbums(artist: Artist, callback: (artists: ArrayList<Album>) -> Unit) {
    ensureBackgroundThread {
        val albums = getAlbumsSync(artist)
        callback(albums)
    }
}

fun Context.getAlbumsCount(artist: Artist): Int {
    val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Audio.Albums._ID)
    var selection = "${MediaStore.Audio.Albums.ARTIST} = ?"
    var selectionArgs = arrayOf(artist.title)

    if (isQPlus()) {
        selection = "${MediaStore.Audio.Albums.ARTIST_ID} = ?"
        selectionArgs = arrayOf(artist.id.toString())
    }

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            return cursor.count
        }
    } catch (e: Exception) {
        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
    }

    return 0
}

fun Context.getAlbumTracksCount(albumId: Long): Int {
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Audio.Media._ID)
    val selection = "${MediaStore.Audio.Albums.ALBUM_ID} = ?"
    val selectionArgs = arrayOf(albumId.toString())

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            val projection2 = arrayOf(MediaStore.Audio.Media.DURATION)
            var count = 0
            queryCursor(
                uri,
                projection2,
                selection,
                selectionArgs,
                showErrors = true
            ) { cursor ->
                val duration = cursor.getIntValue(MediaStore.Audio.Media.DURATION) / 1000
                if (duration > 0) {
                    count += 1
                }
            }
            return count
        }
    } catch (e: Exception) {
        toast(e.message.toString())
    }
    return 0
}

@SuppressLint("NewApi")
fun Context.sendIntent(action: String) {
    Intent(this, MusicService::class.java).apply {
        this.action = action
        try {
            if (isOreoPlus()) {
                startForegroundService(this)
            } else {
                startService(this)
            }
        } catch (ignored: Exception) {
        }
    }
}







