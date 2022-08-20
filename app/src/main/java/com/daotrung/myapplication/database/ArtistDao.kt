package com.daotrung.myapplication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daotrung.myapplication.model.Artist

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(artist: Artist): Long

    @Query("SELECT * FROM Artist")
    fun getAll(): List<Artist>

    @Query("DELETE FROM Artist WHERE id = :id")
    fun deleteArtist(id: Long)
}