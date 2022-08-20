package com.daotrung.myapplication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daotrung.myapplication.model.Album
import com.daotrung.myapplication.model.PrivateVideoPath
import com.daotrung.myapplication.model.VideoLocal

@Dao
interface PrivateVideoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(privateVideoPath: PrivateVideoPath)

    @Query("SELECT * FROM PrivateVideoPath")
    fun getAll(): List<PrivateVideoPath>

    @Query("SELECT * FROM PrivateVideoPath WHERE pathVideoOut = :path")
    fun selectWithId(path: String):PrivateVideoPath

    @Query("DELETE FROM PrivateVideoPath WHERE pathVideoOut = :path")
    fun deleteVideoPathPrivate(path: String)

}