package com.daotrung.myapplication.database

import androidx.room.*
import com.daotrung.myapplication.model.VideoLocal
@Dao
interface RecentVideoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(videoLocal: VideoLocal)

    @Query("SELECT * FROM VideoLocal")
    fun getAll(): List<VideoLocal>

    @Query("DELETE FROM VideoLocal WHERE id = :id")
    fun deleteVideoRecent(id: String)
}