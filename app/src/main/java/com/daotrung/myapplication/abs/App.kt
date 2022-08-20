package com.daotrung.myapplication.abs

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.daotrung.myapplication.database.AppDatabase
import com.daotrung.myapplication.util.LogInstance
import com.daotrung.myapplication.viewmodel.MusicViewModel
import com.daotrung.myapplication.viewmodel.PlayListViewModel
import com.daotrung.myapplication.viewmodel.VideoViewModel
import com.tencent.mmkv.MMKV

/**
//                       _ooOoo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                       O\ = /O
//                   ____/`---'\____
//                 .   ' \\| |// `.
//                  / \\||| : |||// \
//                / _||||| -:- |||||- \
//                  | | \\\ - /// | |
//                | \_| ''\---/'' | |
//                 \ .-\__ `-` ___/-. /
//              ______`. .' /--.--\ `. . __
//           ."" '< `.___\_<|>_/___.' >'"".
//          | | : `- \`.;`\ _ /`;.`/ - ` : | |
//            \ \ `-. \_ __\ /__ _/ .-` / /
//    ======`-.____`-.___\_____/___.-`____.-'======
//                       `=---='
//
//    .............................................
//             DEBUG             DEBUG
 * =====================================================
 * BUG +=1
 * =====================================================
 */
class App : Application() {
    companion object {
        lateinit var context: Context
        lateinit var musicViewModel: MusicViewModel
        lateinit var videoViewModel: VideoViewModel
        lateinit var playListViewModel: PlayListViewModel
        private lateinit var db: AppDatabase
        fun getDB() = db
        lateinit var mmkv: MMKV
    }

    override fun onCreate() {
        super.onCreate()
        context = this.applicationContext
        musicViewModel = MusicViewModel()
        videoViewModel = VideoViewModel()
        playListViewModel = PlayListViewModel()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "data")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
        MMKV.initialize(this)
        mmkv = MMKV.defaultMMKV()
    }
}