package com.daotrung.myapplication.database

import android.net.Uri
import androidx.room.TypeConverter

class DataConverters {
    @TypeConverter
    fun fromString(value: String?): Uri? {
        return if (value == null) null else Uri.parse(value)
    }
    @TypeConverter
    fun toString(uri: Uri?): String? {
        return uri?.toString()
    }
}