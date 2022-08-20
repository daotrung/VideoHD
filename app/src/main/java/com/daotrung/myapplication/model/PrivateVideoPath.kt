package com.daotrung.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PrivateVideoPath(
    @PrimaryKey(autoGenerate = false)
    var pathVideoIn : String ,

    var pathVideoOut : String
)
