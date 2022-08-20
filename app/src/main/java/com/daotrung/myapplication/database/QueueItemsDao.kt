package com.daotrung.myapplication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daotrung.myapplication.model.QueueItem

@Dao
interface QueueItemsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(queueItems: List<QueueItem>)

    @Query("SELECT * FROM QueueItem ORDER BY trackOrder")
    fun getAll(): List<QueueItem>

    @Query("UPDATE QueueItem SET isCurrent = 0")
    fun resetCurrent()

    @Query("UPDATE QueueItem SET isCurrent = 1, lastPosition = :lastPosition WHERE trackId = :trackId")
    fun saveCurrentTrack(trackId: Long, lastPosition: Int)

    @Query("UPDATE QueueItem SET trackOrder = :order WHERE trackId = :trackId")
    fun setOrder(trackId: Long, order: Int)

    @Query("DELETE FROM QueueItem WHERE trackId = :trackId")
    fun removeQueueItem(trackId: Long)

    @Query("DELETE FROM QueueItem")
    fun deleteAllItems()
}