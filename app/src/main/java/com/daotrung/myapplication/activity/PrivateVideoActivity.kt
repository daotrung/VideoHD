package com.daotrung.myapplication.activity


import android.content.ContextWrapper
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.adapter.VideoFoloderEditAdapter
import com.daotrung.myapplication.adapter.VideoPrivateAdapter
import com.daotrung.myapplication.databinding.ActivityPrivateVideoBinding
import com.daotrung.myapplication.model.VideoPrivate
import com.daotrung.myapplication.util.GetAllListFolderUtils
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import java.io.File

class PrivateVideoActivity : BaseActivity<ActivityPrivateVideoBinding>() {

    private lateinit var balloon: Balloon

    companion object {
        var arrayList: ArrayList<VideoPrivate> = ArrayList()
    }

    override fun binding(): ActivityPrivateVideoBinding {
        return ActivityPrivateVideoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        popUpCreate()
        setOnClickListener()
        setAdapter()

    }

    private fun setAdapter() {
        binding.rvSecretList.setHasFixedSize(true)
        binding.rvSecretList.setItemViewCacheSize(10)
        binding.rvSecretList.layoutManager = LinearLayoutManager(this)
        binding.rvSecretList.adapter = VideoPrivateAdapter(this, getAllListInList())
    }

    private fun popUpCreate() {
        balloon = Balloon.Builder(this)
            .setLayout(R.layout.custom_popup_circle_private_list)
            .setWidthRatio(0.55f)
            .setHeight(BalloonSizeSpec.WRAP)
            .setIsVisibleArrow(false)
            .setPadding(12)
            .setCornerRadius(15f)
            .setBackgroundColorResource(R.color.white)
            .setBalloonAnimation(BalloonAnimation.CIRCULAR)
            .build()
        // setOnClick
        binding.icAddCircle.setOnClickListener {
            val rvListBalloonPrivateVideo: RecyclerView =
                balloon.getContentView().findViewById(R.id.rvListBalloonPrivateVideo)
            rvListBalloonPrivateVideo.setHasFixedSize(true)
            rvListBalloonPrivateVideo.setItemViewCacheSize(10)
            rvListBalloonPrivateVideo.layoutManager = LinearLayoutManager(this)
            rvListBalloonPrivateVideo.adapter =
                VideoFoloderEditAdapter(this, GetAllListFolderUtils.getAllVideosInFolder(this))
            balloon.showAlignBottom(binding.icAddCircle)
        }
        val rv: RelativeLayout = balloon.getContentView().findViewById(R.id.root_main)

        rv.setOnClickListener {
            balloon.dismiss()
        }

    }

    private fun setOnClickListener() {
        binding.icLeftBack.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

    }

    private fun getAllListInList(): ArrayList<VideoPrivate> {
        arrayList.clear()
        val cw = ContextWrapper(this)
        val directory = cw.getDir("videoDir", AppCompatActivity.MODE_PRIVATE)
        val file = directory.listFiles()
        file.forEach {
            arrayList.add(VideoPrivate(it.name, getDuration(it), it.length(), Uri.parse(it.path)))
        }
        return arrayList
    }

    private fun getDuration(file: File): String? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file.absolutePath)
        val durationStr =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return formateMilliSeccond(durationStr!!.toLong())
    }

    fun formateMilliSeccond(milliseconds: Long): String? {
        var finalTimerString = ""
        var secondsString = ""

        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        return finalTimerString
    }


}