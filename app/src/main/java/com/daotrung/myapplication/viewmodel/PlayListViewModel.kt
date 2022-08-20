package com.daotrung.myapplication.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.model.PlayListEntity
import com.daotrung.myapplication.model.PlaylistWithVideoMusic
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class PlayListViewModel : ViewModel(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    var playListLiveData: MutableLiveData<List<PlayListEntity>> = MutableLiveData()
    var playListVideoOrMusicLiveData : MutableLiveData<List<PlaylistWithVideoMusic>> = MutableLiveData()
    fun getPlayList(): MutableLiveData<List<PlayListEntity>> {
        launch(Dispatchers.Main) {
            playListLiveData.value = withContext(Dispatchers.IO) {
                App.getDB().playListDao().getAll()
            }
        }
        return playListLiveData
    }

    fun insertPlayList(name: String) {
        launch(Dispatchers.Main) {
            val playListEntity = PlayListEntity(0, name)
            withContext(Dispatchers.IO) {
                App.getDB().playListDao().insert(playListEntity)
            }
        }
    }

    fun insertPlayListVideoMusic(type:String , path : String , name: String){
        launch(Dispatchers.Main){
            val playlistWithVideoMusic = PlaylistWithVideoMusic(0,0,0,type,path,name)
            withContext(Dispatchers.IO){
                App.getDB().playListVideoOrMusic().insert(playlistWithVideoMusic)
            }
        }
    }
    fun getListVideoMusicWithId(id : Int): MutableLiveData<List<PlaylistWithVideoMusic>>{
        launch(Dispatchers.Main){
            playListVideoOrMusicLiveData.value = withContext(Dispatchers.IO){
                App.getDB().playListVideoOrMusic().getAllVideoOrMusicWithId(id)
            }
        }
        return playListVideoOrMusicLiveData
    }


}