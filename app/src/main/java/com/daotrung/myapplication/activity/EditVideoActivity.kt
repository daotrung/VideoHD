package com.daotrung.myapplication.activity

import android.content.ContextWrapper
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.adapter.VideoEditAdapter
import com.daotrung.myapplication.databinding.ActivityEditVideoBinding
import com.daotrung.myapplication.model.VideoLocal
import com.daotrung.myapplication.util.GetAllListFolderUtils
import com.daotrung.myapplication.util.POSITION_FOLDER_VIDEO_EDIT
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.*

class EditVideoActivity : BaseActivity<ActivityEditVideoBinding>() {

    var pos: Int = 0
    var checkBox: Boolean = false

    companion object {
        lateinit var editCurrentFolderVideo: ArrayList<VideoLocal>
    }

    override fun binding(): ActivityEditVideoBinding {
        return ActivityEditVideoBinding.inflate(layoutInflater)
    }

    override fun initView() {

        getIntentFromAdapter()
        setAdapter()
        setOnClickListener()
    }

    private fun setAdapter() {

        editCurrentFolderVideo = GetAllListFolderUtils.getAllVideos(
            this,
            GetAllListFolderUtils.getAllVideosInFolder(this)[pos].id
        )
        binding.rvTabEditVideo.setHasFixedSize(true)
        binding.rvTabEditVideo.setItemViewCacheSize(10)
        binding.rvTabEditVideo.layoutManager = LinearLayoutManager(this@EditVideoActivity)
        binding.rvTabEditVideo.adapter = VideoEditAdapter(
            this@EditVideoActivity,
            editCurrentFolderVideo, isFolder = true
        )
    }

    private fun getIntentFromAdapter() {
        pos = intent.getIntExtra(POSITION_FOLDER_VIDEO_EDIT, 0)
        binding.folderSelected.text =
            GetAllListFolderUtils.getAllVideosInFolder(this)[pos].folderName
    }

    private fun setOnClickListener() {
        binding.imgBackVideoEdit.setOnClickListener {
            onBackPressed()
        }

        binding.ckbAllSelected.setOnClickListener {
            if (!checkBox) {
                binding.imgCheckSelectedAll.setImageResource(R.drawable.ic_check_box)
                checkBox = true

                val dialogDF = MaterialAlertDialogBuilder(this)
                    .setTitle("Edit Video ?")
                    .setMessage("You want selected all video in private ?")
                    .setPositiveButton("Yes") { dialog, _ ->
                            for (i in 0..editCurrentFolderVideo.size) {
                                saveVideoToInternalStorage(editCurrentFolderVideo[i].path)
                            }
                        dialog.dismiss()
                        this.finish()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialogDF.show()
            } else {
                binding.imgCheckSelectedAll.setImageResource(R.drawable.ic_un_check_box)
                checkBox = false
            }
        }

    }

    fun saveVideoToInternalStorage(filePath: String) {
        requestPermissionR()
        val newfile: File
        try {
            val currentFile = File(filePath)
            val fileName = currentFile.name
            val cw = ContextWrapper(this)
            val directory = cw.getDir("videoDir", AppCompatActivity.MODE_PRIVATE)
            newfile = File(directory, fileName)
            if (currentFile.exists()) {
                val `in`: InputStream = FileInputStream(currentFile)
                val out: OutputStream = FileOutputStream(newfile)

                // Copy the bits from instream to outstream

                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()
                // delete file
                val delWithPath = File(filePath)
                if (delWithPath.exists() && delWithPath.delete()) {
                    delWithPath.delete()
                    MediaScannerConnection.scanFile(
                        this,
                        arrayOf(delWithPath.path),
                        arrayOf("video/*"),
                        null
                    )
                    Toast.makeText(this, "All Video in Private Folder !!! ", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Video saving failed. Source file missing.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun requestPermissionR() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${this.applicationContext.packageName}")
                ContextCompat.startActivity(this, intent, null)
            }
        }
    }


}