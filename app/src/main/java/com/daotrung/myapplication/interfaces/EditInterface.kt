package com.daotrung.myapplication.interfaces

import com.daotrung.myapplication.model.MusicLocal

interface EditInterface {
    fun onItemClick(item: MusicLocal, position: Int)
}