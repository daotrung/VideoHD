package com.daotrung.myapplication.database

import androidx.room.*
import com.daotrung.myapplication.model.PlayListEntity

@Dao
interface PlayListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlist: PlayListEntity): Long

    @Delete
    fun deletePlaylists(playlists: List<PlayListEntity?>)

    @Query("SELECT * FROM PlayListEntity")
    fun getAll(): List<PlayListEntity>

    @Query("SELECT * FROM PlayListEntity WHERE playListName = :title COLLATE NOCASE")
    fun getPlaylistWithTitle(title: String): PlayListEntity?

    @Query("SELECT * FROM PlayListEntity WHERE playListId = :id")
    fun getPlaylistWithId(id: Int): PlayListEntity?

    @Update
    fun update(playlist: PlayListEntity)
}