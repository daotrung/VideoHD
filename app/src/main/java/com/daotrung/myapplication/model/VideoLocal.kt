package com.daotrung.myapplication.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.daotrung.myapplication.database.DataConverters

@Entity
data class VideoLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var nameVideo :  String,
    val duration : Long = 0,
    val size : Long = 0,
    var path : String,
    @TypeConverters(DataConverters::class)
    var artUri : Uri,
    val folderName : String

)

