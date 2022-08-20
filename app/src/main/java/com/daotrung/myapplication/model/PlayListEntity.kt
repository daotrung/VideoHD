package com.daotrung.myapplication.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.daotrung.myapplication.util.PLAYER_SORT_BY_TITLE
import com.simplemobiletools.commons.helpers.AlphanumericComparator
import com.simplemobiletools.commons.helpers.SORT_DESCENDING

@Entity
data class PlayListEntity(
    @PrimaryKey(autoGenerate = true)
    var playListId: Int,
    var playListName: String? = null,
    @Ignore var trackCount: Int = 0

) : Comparable<PlayListEntity> {
    constructor() : this(0, "",0)

    companion object {
        var sorting = 0
    }

    override fun compareTo(other: PlayListEntity): Int {
        var result = when {
            sorting and PLAYER_SORT_BY_TITLE != 0 -> AlphanumericComparator().compare(
                playListName!!.toLowerCase(),
                other.playListName!!.toLowerCase()
            )
            else -> trackCount.compareTo(other.trackCount)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }

    fun getBubbleText() = when {
        sorting and PLAYER_SORT_BY_TITLE != 0 -> playListName
        else -> trackCount.toString()
    }
}