package com.daotrung.myapplication.interfaces

import com.daotrung.myapplication.model.MusicLocal

interface SongInterface {
    fun playNext(song: MusicLocal)
    fun addPlayList(song: MusicLocal)
    fun setRingTone(song: MusicLocal)
    fun delete(song: MusicLocal)
    fun setFavourite(song: MusicLocal)
    fun share(song: MusicLocal)
}