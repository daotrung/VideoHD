package com.daotrung.myapplication.model

import android.net.Uri

data class VideoPrivate(
    val nameVideoPrivate: String,
    val duration: String? = null,
    val size: Long = 0,
    val artUri: Uri
)