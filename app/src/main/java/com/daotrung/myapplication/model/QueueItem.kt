package com.daotrung.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class QueueItem(
    @PrimaryKey(autoGenerate = true)
    var trackId: Long,
    var trackOrder: Int,
    var isCurrent: Boolean,
    var lastPosition: Int
)