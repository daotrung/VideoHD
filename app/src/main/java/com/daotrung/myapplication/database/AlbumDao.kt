package com.daotrung.myapplication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daotrung.myapplication.model.Album

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(album: Album): Long

    @Query("SELECT * FROM Album")
    fun getAll(): List<Album>

    @Query("SELECT * FROM Album WHERE artistId = :artistId")
    fun getArtistAlbums(artistId: Long): List<Album>

    @Query("DELETE FROM Album WHERE id = :id")
    fun deleteAlbum(id: Long)
}