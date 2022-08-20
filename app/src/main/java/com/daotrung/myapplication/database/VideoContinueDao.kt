package com.daotrung.myapplication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daotrung.myapplication.model.VideoContinue

@Dao
interface VideoContinueDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(videoContinue: VideoContinue)

    @Query("SELECT * FROM VideoContinue WHERE nameVideoContinue = :nameVideo")
    fun selectWithNameVideo(nameVideo:String):VideoContinue

    @Query("DELETE FROM VideoContinue WHERE nameVideoContinue = :nameVideo")
    fun deleteWithNameVideo(nameVideo: String)

    @Query("UPDATE VideoContinue SET lastCurrentVideo = :lastCurrent WHERE nameVideoContinue = :nameVideo")
    fun updateLastCurrentVideo(nameVideo: String,lastCurrent:Long)

    @Query("UPDATE VideoContinue SET nameVideoContinue = :newNameVideo WHERE nameVideoContinue = :oldNameVideo")
    fun updateNameVideoContinue(newNameVideo:String ,oldNameVideo : String)

    @Query("SELECT EXISTS(SELECT * FROM VideoContinue WHERE nameVideoContinue = :nameVideo)")
    fun isVideoContinueIsExist(nameVideo: String) : Boolean

}