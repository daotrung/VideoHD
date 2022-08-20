package com.daotrung.myapplication.database

import androidx.room.*
import com.daotrung.myapplication.model.MusicLocal

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(track: MusicLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tracks: List<MusicLocal>)

    @Query("SELECT * FROM MusicLocal")
    fun getAll(): List<MusicLocal>

    @Query("SELECT * FROM MusicLocal WHERE playListId = :playlistId")
    fun getSongsFromPlaylist(playlistId: Int): List<MusicLocal>

    @Query("SELECT * FROM MusicLocal WHERE albumId = :albumId")
    fun getSongsFromAlbum(albumId: Long): List<MusicLocal>

    @Query("SELECT COUNT(*) FROM MusicLocal WHERE playListId = :playlistId")
    fun getSongsCountFromPlaylist(playlistId: Int): Int

    @Query("SELECT * FROM MusicLocal WHERE folderName = :folderName COLLATE NOCASE GROUP BY mediaStoreId")
    fun getSongsFromFolder(folderName: String): List<MusicLocal>

    @Query("SELECT * FROM MusicLocal WHERE mediaStoreId = :mediaStoreId")
    fun getSongWithMediaStoreId(mediaStoreId: Long): MusicLocal?

    @Delete
    fun removeSongsFromPlaylists(songs: List<MusicLocal>)

    @Query("DELETE FROM MusicLocal WHERE mediaStoreId = :mediaStoreId")
    fun removeSong(mediaStoreId: Long)

    @Query("DELETE FROM MusicLocal WHERE playListId = :playlistId")
    fun removePlaylistSongs(playlistId: Int)

    @Query("UPDATE MusicLocal SET path = :newPath, artist = :artist, title = :title WHERE path = :oldPath")
    fun updateSongInfo(newPath: String, artist: String, title: String, oldPath: String)

    @Query("UPDATE MusicLocal SET coverArt = :coverArt WHERE mediaStoreId = :id")
    fun updateCoverArt(coverArt: String, id: Long)

    @Query("UPDATE MusicLocal SET folderName = :folderName WHERE mediaStoreId = :id")
    fun updateFolderName(folderName: String, id: Long)

    @Query("DELETE FROM MusicLocal")
    fun delete()
}