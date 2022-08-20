package com.daotrung.myapplication.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.fragment.VideoFragmentVideo
import com.daotrung.myapplication.model.VideoLocal
import com.daotrung.myapplication.util.*
import com.simplemobiletools.commons.extensions.toInt
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class VideoViewModel : ViewModel(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main


    var videoRecentLiveData: MutableLiveData<List<VideoLocal>> = MutableLiveData()
    var videoAllLocal: MutableLiveData<ArrayList<VideoLocal>> = MutableLiveData()

    fun getVideoLocal(): MutableLiveData<List<VideoLocal>> {
        launch(Dispatchers.Main) {
            val data = App.getDB().videoRecent().getAll()
            videoRecentLiveData.value = withContext(Dispatchers.IO) {
                data
            }
        }
        return videoRecentLiveData
    }

    @SuppressLint("Range")
    fun getAllVideos(context: Context): MutableLiveData<ArrayList<VideoLocal>> {
        val tempList = ArrayList<VideoLocal>()
        tempList.clear()
        launch(Dispatchers.Main) {
            val projection = arrayOf(
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DURATION
            )
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                MediaStore.Video.Media.DATE_ADDED + " DESC"
            )

            if (cursor != null)
                if (cursor.moveToNext()) {
                    do {
                        val titleC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                        val idC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                        val folderC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                        val sizeC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                                .toLong()
                        val pathC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                        var durationC: Long = 0L
                        try {
                            durationC =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                                    .toLong()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        try {
                            val file = File(pathC)
                            file.mkdir()
                            val artUric = Uri.fromFile(file)
                            if (durationC != 0L) {
                                val video = VideoLocal(
                                    nameVideo = titleC,
                                    id = idC.toInt(),
                                    size = sizeC,
                                    path = pathC,
                                    duration = durationC,
                                    folderName = folderC,
                                    artUri = artUric
                                )
                                if (file.exists()) {
                                    val videoSizeFilter = UseSharedPreferences.getStringPreferences(
                                        PREF_SIZE_FILTER, context,
                                        KEY_SIZE_FILTER
                                    )

                                    when (videoSizeFilter) {
                                        "5M" -> {
                                            if (byteIntoHumanReadable.setTo(video.size)
                                                    ?.toInt()!! > 5
                                            )
                                                tempList.add(video)
                                        }
                                        "10M" -> {
                                            if (byteIntoHumanReadable.setTo(video.size)
                                                    ?.toInt()!! > 10
                                            )
                                                tempList.add(video)
                                        }
                                        "20M" -> {
                                            if (byteIntoHumanReadable.setTo(video.size)
                                                    ?.toInt()!! > 20
                                            )
                                                tempList.add(video)
                                        }
                                        "30M" -> {
                                            if (byteIntoHumanReadable.setTo(video.size)
                                                    ?.toInt()!! > 30
                                            )
                                                tempList.add(video)
                                        }
                                        else -> {
                                            tempList.add(video)
                                        }
                                    }

                                }


                            }
                        } catch (e: Exception) {

                        }
                    } while (cursor.moveToNext())
                    cursor.close()
                }
            videoAllLocal.value = withContext(Dispatchers.IO) {
                tempList
            }
        }
        return videoAllLocal
    }


    fun forceReload(context: Context) = launch(Dispatchers.Main) {
        getAllVideos(context)
    }
}