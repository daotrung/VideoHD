package com.daotrung.myapplication.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.model.Album
import com.daotrung.myapplication.model.Artist
import com.daotrung.myapplication.model.FolderMusic
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.util.config
import com.daotrung.myapplication.util.getAlbumTracksSync
import com.daotrung.myapplication.util.getAlbums
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MusicViewModel : ViewModel(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    var songLiveData: MutableLiveData<List<MusicLocal>> = MutableLiveData()
    var albumsLiveData: MutableLiveData<List<Album>> = MutableLiveData()
    var songsAlbumLiveData: MutableLiveData<List<MusicLocal>> = MutableLiveData()
    var artistLiveData: MutableLiveData<ArrayList<Artist>> = MutableLiveData()
    var songsArtistLiveData: MutableLiveData<ArrayList<MusicLocal>> = MutableLiveData()
    var folderLiveData: MutableLiveData<ArrayList<FolderMusic>> = MutableLiveData()

    fun getSongs(): MutableLiveData<List<MusicLocal>> {
        launch(Dispatchers.Main) {
            val albums = ArrayList<Album>()
            val artists = App.getDB().artistsDao().getAll()
            artists.forEach { artist ->
                albums.addAll(App.getDB().albumsDao().getArtistAlbums(artist.id))
            }
            var songs = ArrayList<MusicLocal>()
            albums.forEach { album ->
                songs.addAll(App.getDB().songsDao().getSongsFromAlbum(album.id))
            }
            songs = songs.distinctBy { "${it.path}/${it.mediaStoreId}" }
                .toMutableList() as ArrayList<MusicLocal>
            MusicLocal.sorting = App.context.config.songSorting
            songs.sort()
            songLiveData.value = withContext(Dispatchers.IO) {
                songs
            }
        }
        return songLiveData
    }

    fun getAlbum(): MutableLiveData<List<Album>> {
        launch(Dispatchers.Main) {
            val albums = ArrayList<Album>()
            val artists = App.getDB().artistsDao().getAll()
            artists.forEach { artist ->
                albums.addAll(App.getDB().albumsDao().getArtistAlbums(artist.id))
            }
            albumsLiveData.value = withContext(Dispatchers.IO) {
                albums
            }
        }
        return albumsLiveData
    }

    fun getSongsAlbum(albumId: Long): MutableLiveData<List<MusicLocal>> {
        val data = App.context.getAlbumTracksSync(albumId)
        launch(Dispatchers.Main) {
            songsAlbumLiveData.value = withContext(Dispatchers.IO) {
                data
            }
        }
        return songsAlbumLiveData
    }

    fun getArtists(): MutableLiveData<ArrayList<Artist>> {
        launch(Dispatchers.Main) {
            val data = App.getDB().artistsDao().getAll() as ArrayList<Artist>
            artistLiveData.value = withContext(Dispatchers.IO) {
                data
            }
        }
        return artistLiveData
    }

    fun getSongsArtis(artist: Artist): MutableLiveData<ArrayList<MusicLocal>> {
        launch(Dispatchers.Main) {
            val songsToAdd = ArrayList<MusicLocal>()
            App.context.getAlbums(artist) { albums ->
                albums.forEach {
                    val song = App.context.getAlbumTracksSync(it.id)
                    songsToAdd.addAll(song)
                }
            }
            songsArtistLiveData.value = withContext(Dispatchers.IO) {
                songsToAdd
            }
        }
        return songsArtistLiveData
    }

    fun getFolder(): MutableLiveData<ArrayList<FolderMusic>> {
        launch(Dispatchers.Main) {
            val albums = ArrayList<Album>()
            val artists = App.getDB().artistsDao().getAll()
            artists.forEach { artist ->
                albums.addAll(App.getDB().albumsDao().getArtistAlbums(artist.id))
            }
            var songs = ArrayList<MusicLocal>()
            albums.forEach { album ->
                songs.addAll(App.getDB().songsDao().getSongsFromAlbum(album.id))
            }
            songs = songs.distinctBy {
                "${it.path}/${it.mediaStoreId}"
            }.toMutableList() as ArrayList<MusicLocal>
            MusicLocal.sorting = App.context.config.songSorting
            songs.sort()

            val foldersMap = songs.groupBy { it.folderName }
            val folders = ArrayList<FolderMusic>()
            for ((title, folderSong) in foldersMap) {
                val folder = FolderMusic(title, folderSong.size)
                folders.add(folder)
                if (!App.context.config.wereSongFoldersAdded) {
                    folderSong.forEach {
                        App.getDB().songsDao().updateFolderName(title, it.mediaStoreId)
                    }
                }
            }

            App.context.config.wereSongFoldersAdded = true
            FolderMusic.sorting = App.context.config.folderSorting
            folders.sort()
            folderLiveData.value = withContext(Dispatchers.IO) {
                folders
            }
        }
        return folderLiveData
    }

    fun forceReload() = launch(Dispatchers.Main) {
        getSongs()
        getAlbum()
        getArtists()
        getFolder()
    }
}