package com.daotrung.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VideoContinue(

    @PrimaryKey(autoGenerate = false)
    var nameVideoContinue : String ,
    var lastCurrentVideo : Long ,
    var totalCurrentVideo : Long

)

