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
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.activity.MainActivity
import com.daotrung.myapplication.activity.PlayVideoActivity
import com.daotrung.myapplication.model.VideoLocal
import com.daotrung.myapplication.databinding.ItemListVideoFragmentVideoBinding
import com.daotrung.myapplication.databinding.RenameFieldBinding
import com.daotrung.myapplication.fragment.VideoFragmentVideo
import com.daotrung.myapplication.model.PrivateVideoPath
import com.daotrung.myapplication.util.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.*
import kotlin.math.roundToInt

class VideoAdapter(
    private val context: Context,

    private val isFolder: Boolean = false
) :
    RecyclerView.Adapter<VideoAdapter.MyViewHolder>() {

    var listDataType = arrayListOf<VideoLocal>()

    inner class MyViewHolder(binding: ItemListVideoFragmentVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val img = binding.imgVideoMain
        val title = binding.txtTitleVideo
        val duration = binding.txtTimeVideo
        val size = binding.txtStorageVideo
        val imgMore = binding.imgMoreItem
        val root = binding.root
        val seekBar = binding.seekBarVideoContinue
        val layout_seekBar = binding.layoutSeekBar
        val txtPercentSeekbar = binding.txtPercentContinue
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemListVideoFragmentVideoBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = listDataType[position].nameVideo
        holder.duration.text = DateUtils.formatElapsedTime(listDataType[position].duration / 1000)
        holder.size.text =
            (((listDataType[position].size / 1021 * 0.001) * 100.0).roundToInt() / 100.0).toString() + " MB"
        Glide.with(context)
            .asBitmap()
            .load(listDataType[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.demo).centerCrop())
            .into(holder.img)


        // xu ly seekBar
        if (UseSharedPreferences.getBooleanPreferences(
                PREFS_SHOW_RESUME_STATUS, context,
                KEY_SHOW_RESUME_STATUS
            )
        ) {
            if (App.getDB().videoContinue()
                    .isVideoContinueIsExist(listDataType[position].nameVideo)
            ) {
                holder.seekBar.max = App.getDB().videoContinue()
                    .selectWithNameVideo(listDataType[position].nameVideo).totalCurrentVideo.toInt()
                holder.seekBar.progress = App.getDB().videoContinue()
                    .selectWithNameVideo(listDataType[position].nameVideo).lastCurrentVideo.toInt()

                val percent = holder.seekBar.progress.divideToPercent(holder.seekBar.max)

                Log.e("pro", percent.toString())
                holder.txtPercentSeekbar.text = "${percent}%"

                holder.layout_seekBar.visibility = View.VISIBLE
            } else {
                holder.layout_seekBar.visibility = View.GONE
            }
        } else {
            holder.layout_seekBar.visibility = View.GONE
        }


        holder.imgMore.setOnClickListener {
            setClickBottomSheetDialog(holder, position)

        }
        holder.root.setOnClickListener {

            when {
                listDataType[position].id.toString() == PlayVideoActivity.nowPlayingId -> {
                    Log.e("pos_", position.toString())
                    sendIntent(pos = position, ref = "NowPlaying")
                }
                isFolder -> {
                    Log.e("pos_", position.toString())
                    sendIntent(pos = position, ref = "FolderActivity")
                }
                VideoFragmentVideo.search -> {
                    Log.e("pos_", position.toString())
                    sendIntent(pos = position, ref = "SearchVideos")
                }
                else -> {
                    Log.e("pos_", position.toString())
                    sendIntent(pos = position, ref = "AllVideos")

                }
            }

        }


    }

    private fun setClickBottomSheetDialog(holder: VideoAdapter.MyViewHolder, position: Int) {
        val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val bsView: View = LayoutInflater.from(context).inflate(
            R.layout.custom_video_bottom_sheet_video_main,
            bottomSheetDialog.findViewById(R.id.bottom_sheet_background)
        )
        bsView.findViewById<LinearLayout>(R.id.play_video_main).setOnClickListener {
            when {
                listDataType[position].id.toString() == PlayVideoActivity.nowPlayingId -> {
                    Log.e("pos_", position.toString())
                    sendIntent(pos = position + 1, ref = "NowPlaying")
                }
                isFolder -> {
                    Log.e("pos_", position.toString())
                    sendIntent(pos = position + 1, ref = "FolderActivity")
                }
                VideoFragmentVideo.search -> {
                    Log.e("pos_", position.toString())
                    sendIntent(pos = position + 1, ref = "SearchVideos")
                }
                else -> {
                    Log.e("pos_", position.toString())
                    sendIntent(pos = position + 1, ref = "AllVideos")

                }
            }
            bottomSheetDialog.dismiss()

        }
        bsView.findViewById<LinearLayout>(R.id.set_rename_item_video_main).setOnClickListener {
            requestPermissionR()
            bottomSheetDialog.dismiss()
            val customDialogRF =
                LayoutInflater.from(context).inflate(R.layout.rename_field, holder.root, false)
            val bindingRF = RenameFieldBinding.bind(customDialogRF)
            val dialogRF = MaterialAlertDialogBuilder(context).setView(customDialogRF)
                .setCancelable(false)
                .setPositiveButton("Rename") { dialog, _ ->
                    val currentFile = File(listDataType[position].path)
                    val newName = bindingRF.renameField.text
                    if (newName != null && currentFile.exists() && newName.toString()
                            .isNotEmpty()
                    ) {
                        val newFile = File(
                            currentFile.parentFile,
                            newName.toString() + "." + currentFile.extension
                        )
                        if (currentFile.renameTo(newFile)) {
                            MediaScannerConnection.scanFile(
                                context, arrayOf(newFile.toString()),
                                arrayOf("video/*"), null
                            )
                            val oldName = VideoFragmentVideo.tempList[position].nameVideo
                            VideoFragmentVideo.tempList[position].nameVideo = newName.toString()
                            VideoFragmentVideo.tempList[position].path = newFile.path
                            VideoFragmentVideo.tempList[position].artUri = Uri.fromFile(newFile)

                            notifyItemChanged(position)

                            if (App.getDB().videoContinue().isVideoContinueIsExist(oldName)) {
                                App.getDB().videoContinue()
                                    .updateNameVideoContinue(newName.toString(), oldName)
                            }

                        } else {
                            Toast.makeText(context, "Permission Denied !!", Toast.LENGTH_SHORT)
                                .show()

                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            dialogRF.show()
            bindingRF.renameField.text = SpannableStringBuilder(listDataType[position].nameVideo)


        }
        bsView.findViewById<TextView>(R.id.name_video_main_popup).text =
            listDataType[position].nameVideo
        bsView.findViewById<LinearLayout>(R.id.share_item_video_main).setOnClickListener {
            bottomSheetDialog.dismiss()
            GetAllListFolderUtils.shareVideoFile(listDataType[position].path, context)
        }
        bsView.findViewById<LinearLayout>(R.id.delete_item_video_main).setOnClickListener {
            requestPermissionR()
            bottomSheetDialog.dismiss()
            val dialogDF = MaterialAlertDialogBuilder(context)
                .setTitle("Delete Video ?")
                .setMessage(listDataType[position].nameVideo)
                .setPositiveButton("Yes") { dialog, _ ->
                    val file = File(listDataType[position].path)

                    GetAllListFolderUtils.deleteFile(file, context)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            dialogDF.show()

        }
        bsView.findViewById<LinearLayout>(R.id.private_folder_item_video_main).setOnClickListener {
            requestPermissionR()
            saveVideoToInternalStorage(listDataType[position].path, position)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setContentView(bsView)
        bottomSheetDialog.show()
    }

    override fun getItemCount(): Int {
        return listDataType.size
    }

    private fun sendIntent(pos: Int, ref: String) {
        PlayVideoActivity.position = pos
        val intent = Intent(context, PlayVideoActivity::class.java)
        intent.putExtra(CLASS_ITEM_VIDEO_PLAY, ref)
        ContextCompat.startActivity(context, intent, null)
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

    fun setListData(list: ArrayList<VideoLocal>) {
        listDataType.clear()
        listDataType.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(searchList: ArrayList<VideoLocal>) {
        listDataType.clear()
        listDataType.addAll(searchList)
        notifyDataSetChanged()

    }

    @SuppressLint("NotifyDataSetChanged")
    fun saveVideoToInternalStorage(filePath: String, pos: Int) {

        val newfile: File
        try {
            val currentFile = File(filePath)
            val fileName = currentFile.name
            val cw = ContextWrapper(context)
            val directory = cw.getDir("videoDir", AppCompatActivity.MODE_PRIVATE)
            newfile = File(directory, fileName)

            // add with database room

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
                    listDataType.removeAt(pos)
                    VideoFragmentVideo.tempList.removeAt(pos)
                    notifyDataSetChanged()

                    Toast.makeText(context, "Video in private folder !!", Toast.LENGTH_SHORT).show()

                    GetAllListFolderUtils.scanFile(currentFile.parentFile.path, context)
                    App.getDB().privateVideoDao()
                        .insert(PrivateVideoPath(currentFile.toString(), newfile.toString()))

                    context.startActivity(Intent(context, MainActivity::class.java))

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

    fun Int.divideToPercent(divideTo: Int): Int {
        return if (divideTo == 0) 0
        else (this * 100 / divideTo)
    }


}
