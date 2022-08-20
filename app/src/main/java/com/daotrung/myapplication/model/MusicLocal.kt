package com.daotrung.myapplication.model

import android.provider.MediaStore
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.daotrung.myapplication.util.*
import com.daotrung.myapplication.util.AppUtils.getFilenameFromPath
import com.daotrung.myapplication.util.AppUtils.getFormattedDuration
import java.io.Serializable

@Entity
data class MusicLocal(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    val mediaStoreId: Long,
    var title: String,
    var artist: String,
    var path: String,
    var duration: Int,
    var album: String,
    val coverArt: String,
    val songId: Int,
    var playListId: Int,
    var folderName: String,
    val albumId: Long,
    var isCheck: Boolean = false
) : Serializable, Comparable<MusicLocal>, ListItem() {
    companion object {
        var sorting = 0
    }

    override fun compareTo(other: MusicLocal): Int {
        var res = when {
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
                    artist == MediaStore.UNKNOWN_STRING && artist != MediaStore.UNKNOWN_STRING -> 1
                    artist != MediaStore.UNKNOWN_STRING && artist == MediaStore.UNKNOWN_STRING -> -1
                    else -> AlphanumericComparator().compare(
                        artist.toLowerCase(),
                        other.artist.toLowerCase()
                    )
                }
            }
            else -> duration.compareTo(other.duration)
        }

        if (sorting and SORT_DESCENDING != 0) {
            res *= -1
        }

        return res
    }

    fun getBubbleText() = when {
        sorting and PLAYER_SORT_BY_TITLE != 0 -> title
        sorting and PLAYER_SORT_BY_ARTIST_TITLE != 0 -> artist
        else -> duration.getFormattedDuration()
    }

    fun getProperTitle(showFilename: Int): String {
        return when (showFilename) {
            SHOW_FILENAME_NEVER -> title
            SHOW_FILENAME_IF_UNAVAILABLE -> if (title == MediaStore.UNKNOWN_STRING) path.getFilenameFromPath() else title
            else -> path.getFilenameFromPath()
        }
    }
}
