package com.daotrung.myapplication.adapter

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
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
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.activity.PlayVideoActivity
import com.daotrung.myapplication.activity.PrivateVideoActivity
import com.daotrung.myapplication.model.VideoPrivate
import com.daotrung.myapplication.databinding.ItemListVideoFragmentVideoBinding
import com.daotrung.myapplication.databinding.RenameFieldBinding
import com.daotrung.myapplication.fragment.VideoFragmentVideo
import com.daotrung.myapplication.model.PrivateVideoPath
import com.daotrung.myapplication.model.VideoLocal
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.*
import com.daotrung.myapplication.activity.MainActivity
import com.daotrung.myapplication.util.*


class VideoPrivateAdapter(private val context: Context, private var videoList : ArrayList<VideoPrivate>):RecyclerView.Adapter<VideoPrivateAdapter.MyViewHolder>(){
    inner class MyViewHolder(binding:ItemListVideoFragmentVideoBinding):RecyclerView.ViewHolder(binding.root){
        val img = binding.imgVideoMain
        val title = binding.txtTitleVideo
        val duration = binding.txtTimeVideo
        val size = binding.txtStorageVideo
        val imgMore = binding.imgMoreItem
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemListVideoFragmentVideoBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = videoList[position].nameVideoPrivate
        holder.duration.text = videoList[position].duration
        holder.size.text = (Math.round((videoList[position].size/1021*0.001)*100.0)/100.0).toString()+" MB"
        Glide.with(context)
            .asBitmap()
            .load(videoList[position].artUri.path)
            .apply(RequestOptions().placeholder(R.drawable.demo).centerCrop())
            .into(holder.img)

        holder.imgMore.setOnClickListener {
            setBottomSheetClick(holder,position)
        }
        holder.root.setOnClickListener {
            sendIntent(position,"AllPrivateVideos")
        }
    }

    private fun setBottomSheetClick(holder: VideoPrivateAdapter.MyViewHolder, position: Int) {
        val bottomSheetDialog = BottomSheetDialog(context,R.style.BottomSheetDialogTheme)
        val bsView : View = LayoutInflater.from(context).inflate(R.layout.custom_video_private_bottom_sheet,bottomSheetDialog.findViewById(R.id.bottom_sheet_background))
        val textView = bsView.findViewById<TextView>(R.id.name_item_video_private)

        // set name video
        textView.text = videoList[position].nameVideoPrivate
        // play video
        bsView.findViewById<LinearLayout>(R.id.play_item_video_private).setOnClickListener {
            sendIntent(position,"AllPrivateVideos")
            bottomSheetDialog.dismiss()
        }
        bsView.findViewById<LinearLayout>(R.id.delete_item_video_private).setOnClickListener {
            requestPermissionR()
            bottomSheetDialog.dismiss()
            val dialogDF = MaterialAlertDialogBuilder(context)
                .setTitle("Delete Video ?")
                .setMessage(videoList[position].nameVideoPrivate)
                .setPositiveButton("Yes"){dialog,_->
                    val file = File(videoList[position].artUri.toString())
                    GetAllListFolderUtils.deleteFile(file,context)
                }
                .setNegativeButton("Cancel"){dialog,_->
                    dialog.dismiss()
                }
                .create()
            dialogDF.show()
        }

        bsView.findViewById<LinearLayout>(R.id.open_item_video_private_folder).setOnClickListener {
            requestPermissionR()
            bottomSheetDialog.dismiss()
            openVideoPrivate(videoList[position].artUri.path,position)

        }

        bottomSheetDialog.setContentView(bsView)
        bottomSheetDialog.show()
    }

    private fun openVideoPrivate(path: String?, position: Int) {
        if (path != null) {
            try {
                val currentFile = File(path)

                val newfile = File(App.getDB().privateVideoDao().selectWithId(path).pathVideoIn)

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
                    val delWithPath = File(path)
                    if (delWithPath.exists() && delWithPath.delete()) {
                        delWithPath.delete()

                        videoList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position,position+1)
                        Toast.makeText(context, "Open Lock Video !!", Toast.LENGTH_SHORT).show()

                        // scan file
                        GetAllListFolderUtils.scanFile(App.getDB().privateVideoDao().selectWithId(path).pathVideoIn,context)
                        // delete with dataRoom after open
                        App.getDB().privateVideoDao().deleteVideoPathPrivate(path)

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


    private fun sendIntent(pos:Int , ref : String){
        PlayVideoActivity.position = pos
        val intent = Intent(context, PlayVideoActivity::class.java)
        intent.putExtra(CLASS_ITEM_VIDEO_PLAY,ref)
        ContextCompat.startActivity(context,intent,null)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    private fun requestPermissionR(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if(!Environment.isExternalStorageManager()){
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${context.applicationContext.packageName}")
                ContextCompat.startActivity(context,intent,null)
            }
        }
    }

}