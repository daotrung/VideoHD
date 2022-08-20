package com.daotrung.myapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.daotrung.myapplication.R
import com.daotrung.myapplication.activity.PlayVideoActivity
import com.daotrung.myapplication.databinding.ItemSelectedEditVideoBinding
import com.daotrung.myapplication.fragment.VideoFragmentVideo
import com.daotrung.myapplication.model.VideoLocal
import com.daotrung.myapplication.util.CLASS_ITEM_VIDEO_PLAY
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.*

class VideoEditAdapter(
    private val context: Context,
    private var videoList: ArrayList<VideoLocal>,
    private val isFolder: Boolean = false
) :
    RecyclerView.Adapter<VideoEditAdapter.MyViewHolder>() {

    private var checkBox: Boolean = false

    inner class MyViewHolder(binding: ItemSelectedEditVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val img = binding.imgVideoMain
        val title = binding.txtTitleVideoEdit
        val duration = binding.txtTimeVideoEdit
        val size = binding.txtStorageVideoEdit
        val imgEdit = binding.imgSelectedVideoEdit
        val root = binding.mainRoot
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemSelectedEditVideoBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = videoList[position].nameVideo
        holder.duration.text = DateUtils.formatElapsedTime(videoList[position].duration / 1000)
        holder.size.text =
            (Math.round((videoList[position].size / 1021 * 0.001) * 100.0) / 100.0).toString() + " MB"
        Glide.with(context)
            .asBitmap()
            .load(videoList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.demo).centerCrop())
            .into(holder.img)

        holder.root.setOnClickListener {
            if (!checkBox) {
                holder.imgEdit.setImageResource(R.drawable.ic_check_box)

                val dialogDF = MaterialAlertDialogBuilder(context)
                    .setTitle("Edit Video ?")
                    .setMessage("You want selected video in private ?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        saveVideoToInternalStorage(videoList[position].path, position)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialogDF.show()
                checkBox = true
            } else {
                holder.imgEdit.setImageResource(R.drawable.ic_un_check_box)
                checkBox = false
            }

        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    private fun requestPermissionR() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${context.applicationContext.packageName}")
                ContextCompat.startActivity(context, intent, null)
            }
        }
    }

    fun saveVideoToInternalStorage(filePath: String, pos: Int) {
        requestPermissionR()
        val newfile: File
        try {
            val currentFile = File(filePath)
            val fileName = currentFile.name
            val cw = ContextWrapper(context)
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
                        context,
                        arrayOf(delWithPath.path),
                        arrayOf("video/*"),
                        null
                    )
                    VideoFragmentVideo.tempList.removeAt(pos)
                    notifyItemRangeRemoved(0, pos + 2)
                    Toast.makeText(context, "Video in private folder !!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Video saving failed. Source file missing.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


}