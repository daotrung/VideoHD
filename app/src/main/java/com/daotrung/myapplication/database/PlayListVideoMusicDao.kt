package com.daotrung.myapplication.database

import androidx.room.*
import com.daotrung.myapplication.model.PlayListEntity
import com.daotrung.myapplication.model.PlaylistWithVideoMusic

@Dao
interface PlayListVideoMusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlistWithVideoMusic: PlaylistWithVideoMusic): Long

    @Delete
    fun delete(playlists: List<PlaylistWithVideoMusic?>)

    @Query("SELECT * FROM PlaylistWithVideoMusic")
    fun getAll(): List<PlaylistWithVideoMusic>

    @Query("SELECT * FROM PlaylistWithVideoMusic WHERE playListId = :id")
    fun getAllVideoOrMusicWithId(id: Int):List<PlaylistWithVideoMusic>

    @Update
    fun update(playlist: PlayListEntity)
}