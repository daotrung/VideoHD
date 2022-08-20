package com.daotrung.myapplication.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import com.daotrung.myapplication.model.VideoFolder
import com.daotrung.myapplication.model.VideoLocal
import java.io.File
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.widget.Toast
import androidx.core.content.ContextCompat

import com.daotrung.myapplication.activity.MainActivity
import com.daotrung.myapplication.fragment.VideoFragmentVideo


object GetAllListFolderUtils {

    var totalFolder : Int = 0
    @SuppressLint("Range")
    fun getAllVideosInFolder(mActivity:Activity): ArrayList<VideoFolder>{
        val folderList = ArrayList<VideoFolder>()
        val tempList = ArrayList<VideoLocal>()
        val tempFolderList = ArrayList<String>()


        val projection = arrayOf(
            MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE, MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION, MediaStore.Video.Media.BUCKET_ID)
        val cursor = mActivity.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,null,null,
            MediaStore.Video.Media.DATE_ADDED + " DESC")

        if(cursor != null)
            if(cursor.moveToNext()){
                do{
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val folderIdC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)).toLong()
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    var durationC : Long = 0L
                    try {
                        durationC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)).toLong()
                    }catch (e : Exception){
                        e.printStackTrace()
                    }

                    try {
                        val file = File(pathC)
                        val artUric = Uri.fromFile(file)
                        if (durationC != 0L){
                            val video = VideoLocal(nameVideo = titleC , id = idC.toInt() , size = sizeC ,
                                path = pathC , duration = durationC , folderName = folderC, artUri = artUric)
                            if(file.exists())
                                tempList.add(video)
                        }

                        if(!tempFolderList.contains(folderC)){
                            var count = 0
                            tempFolderList.add(folderC)
                            for(temp in tempList){
                                 if(temp.folderName == folderC){
                                     val filePath = File(temp.artUri.toFile().parentFile.toString())
                                     scanFile(filePath.path,mActivity)
                                     count = filePath.list().size
                                 }
                            }
                            folderList.add(VideoFolder(id = folderIdC , folderName = folderC , totalVideo = count ))
                        }
                    }catch (e: Exception){

                    }
                }while (cursor.moveToNext())
                cursor?.close()
            }
        return folderList
    }

    @SuppressLint("Range")
     fun getAllVideos(mActivity: Activity,folderId : String): ArrayList<VideoLocal>{
        val templist = ArrayList<VideoLocal>()
        val projection = arrayOf(
            MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE, MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION)

        val selection = MediaStore.Video.Media.BUCKET_ID + " like? "
        val cursor = mActivity.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,selection, arrayOf(folderId),
            MediaStore.Video.Media.DATE_ADDED + " DESC")

        if(cursor != null)
            if(cursor.moveToNext()){
                do{
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)).toLong()
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    var durationC : Long = 0L
                    try {
                        durationC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)).toLong()
                    }catch (e : Exception){
                        e.printStackTrace()
                    }

                    try {
                        val file = File(pathC)
                        val artUric = Uri.fromFile(file)
                        if (durationC != 0L){
                            val video = VideoLocal(nameVideo = titleC , id = idC.toInt() , size = sizeC ,
                                path = pathC , duration = durationC , folderName = folderC, artUri = artUric)
                            if(file.exists())
                                templist.add(video)

                        }
                    }catch (e: Exception){

                    }
                }while (cursor.moveToNext())
                cursor?.close()


            }
        return templist
    }


    fun scanFile(path: String,context: Context) {
        MediaScannerConnection.scanFile(
            context, arrayOf(path), null
        ) { path, uri -> Log.i("TAG", "Finished scanning $path") }
    }

    fun deleteFile(file : File,context: Context) {
        if (file.exists() && file.delete()) {
            file.delete()
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.path),
                arrayOf("video/*"),
                null
            )
        } else {
            Toast.makeText(context, "Permission Denied !!", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareVideoFile(path: String,context: Context){
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "video/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
        ContextCompat.startActivity(
            context,
            Intent.createChooser(shareIntent, "Sharing Video File !!"),
            null
        )
    }

}