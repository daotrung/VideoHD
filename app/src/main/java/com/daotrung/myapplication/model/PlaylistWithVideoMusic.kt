package com.daotrung.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlaylistWithVideoMusic (
    @PrimaryKey(autoGenerate = true)
    var idPlayList : Int = 0,
    var idVideoOrMusic : Long ,
    var playListId: Int,
    var model : String,
    var nameVideoOrMusic : String ,
    var path : String ,
): Comparable<PlaylistWithVideoMusic> {
    constructor() : this(0,0,0,"","","")

    override fun compareTo(other: PlaylistWithVideoMusic): Int {
        TODO("Not yet implemented")
    }
}

