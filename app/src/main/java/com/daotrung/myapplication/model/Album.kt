package com.daotrung.myapplication.model

import android.provider.MediaStore
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.daotrung.myapplication.util.PLAYER_SORT_BY_ARTIST_TITLE
import com.daotrung.myapplication.util.PLAYER_SORT_BY_TITLE
import com.daotrung.myapplication.util.SORT_DESCENDING


@Entity
data class Album(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val artist: String,
    val title: String,
    val coverArt: String,
    val year: Int,
    var trackCnt: Int,
    var artistId: Long
) : ListItem(), Comparable<Album> {
    companion object {
        var sorting = 0
    }

    override fun compareTo(other: Album): Int {
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
            sorting and PLAYER_SORT_BY_ARTIST_TITLE != 0 -> {
                when {
                    artist == MediaStore.UNKNOWN_STRING && other.artist != MediaStore.UNKNOWN_STRING -> 1
                    artist != MediaStore.UNKNOWN_STRING && other.artist == MediaStore.UNKNOWN_STRING -> -1
                    else -> AlphanumericComparator().compare(
                        artist.toLowerCase(),
                        other.artist.toLowerCase()
                    )
                }
            }
            else -> year.compareTo(other.year)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }

    fun getBubbleText() = when {
        sorting and PLAYER_SORT_BY_TITLE != 0 -> title
        sorting and PLAYER_SORT_BY_ARTIST_TITLE != 0 -> artist
        else -> year.toString()
    }
}