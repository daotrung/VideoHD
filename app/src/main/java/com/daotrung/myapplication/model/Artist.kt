package com.daotrung.myapplication.model

import android.provider.MediaStore
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.daotrung.myapplication.util.PLAYER_SORT_BY_ALBUM_COUNT
import com.daotrung.myapplication.util.PLAYER_SORT_BY_TITLE
import com.daotrung.myapplication.util.SORT_DESCENDING


@Entity
data class Artist(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    var albumCnt: Int,
    var trackCnt: Int,
    var albumArtId: Long
) : Comparable<Artist> {
    companion object {
        var sorting = 0
    }

    override fun compareTo(other: Artist): Int {
        var result = when {
            sorting and PLAYER_SORT_BY_TITLE != 0 -> {
                when {
                    title == MediaStore.UNKNOWN_STRING && other.title != MediaStore.UNKNOWN_STRING -> 1
                    title != MediaStore.UNKNOWN_STRING && other.title == MediaStore.UNKNOWN_STRING -> -1
                    else -> AlphanumericComparator().compare(
                        title.toLowerCase(),
                        other.title.toLowerCase()
                    )
                }
            }
            sorting and PLAYER_SORT_BY_ALBUM_COUNT != 0 -> albumCnt.compareTo(other.albumCnt)
            else -> trackCnt.compareTo(other.trackCnt)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }

    fun getBubbleText() = when {
        sorting and PLAYER_SORT_BY_TITLE != 0 -> title
        sorting and PLAYER_SORT_BY_ALBUM_COUNT != 0 -> albumCnt.toString()
        else -> trackCnt.toString()
    }
}
