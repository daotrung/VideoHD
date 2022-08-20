package com.daotrung.myapplication.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.load.engine.Resource
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.databinding.ActivityPlayVideoBinding
import com.daotrung.myapplication.fragment.VideoFragmentVideo
import com.daotrung.myapplication.model.VideoContinue
import com.daotrung.myapplication.model.VideoLocal
import com.daotrung.myapplication.model.VideoPrivate
import com.daotrung.myapplication.util.*
import com.daotrung.myapplication.viewmodel.VideoViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import androidx.lifecycle.Observer
import kotlin.math.abs

class PlayVideoActivity : BaseActivity<ActivityPlayVideoBinding>(),AudioManager.OnAudioFocusChangeListener,GestureDetector.OnGestureListener{

    private lateinit var runnable: Runnable
    private lateinit var gestureDetectorCompat: GestureDetectorCompat
    private var audioManager : AudioManager? = null
    private var brightness = 0
    private var volumetitle = 0
    private lateinit var videoViewModel: VideoViewModel
    companion object {
         lateinit var playerList : ArrayList<VideoLocal>
         lateinit var playerListPrivate : ArrayList<VideoPrivate>
         var position : Int = -1
         lateinit var player : ExoPlayer
         private var repeat : Boolean = false
         private var isFullScreen : Boolean = false
         private var isLocked : Boolean = false
          var nowPlayingId : String = ""

    }
    override fun binding(): ActivityPlayVideoBinding {

        return ActivityPlayVideoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        videoViewModel = VideoViewModel()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setTheme(R.style.playerVideoActivity)
        initializeLayout()
        setOnClickListener()
        gestureDetectorCompat = GestureDetectorCompat(this,this)
    }

    private fun setOnClickListener() {


        binding.icBackVideoPlay.setOnClickListener {
            finish()
        }
        binding.imbPlayPause.setOnClickListener {
             if(player.isPlaying)
                 pauseVideo()
             else
                 playVideo()
        }
        binding.imgNext.setOnClickListener {
            nextPrevVideo()
        }
        binding.imbPrev.setOnClickListener {
            nextPrevVideo(false)
        }

        when(UseSharedPreferences.getStringPreferences(PREFS_TIME_VIDEO,this, KEY_TIME_VIDEO)){
            "5s"  ->
                setReplayAndFowardTime(5000)
            "10s" ->
                setReplayAndFowardTime(10000)
            "30s" ->
                setReplayAndFowardTime(30000)
            else ->
                setReplayAndFowardTime(10000)

        }


        binding.imbRepeat.setOnClickListener {
            if(repeat){
                repeat = false
                player.repeatMode = Player.REPEAT_MODE_OFF
                binding.imbRepeat.setImageResource(R.drawable.ic_repeat_play_off)

            }else{
                repeat = true
                player.repeatMode = Player.REPEAT_MODE_ONE
                binding.imbRepeat.setImageResource(R.drawable.ic_repeat_play)
            }
        }

        binding.imgFullScreen.setOnClickListener {
            if(isFullScreen){
                isFullScreen = false
                playInFullScreen(enable = false)
            }else{
                isFullScreen = true
                playInFullScreen(enable = true)
            }
        }

        binding.openLockPlayVideo.setOnClickListener {
            if(!isLocked){
                // HIDING
                isLocked = true
                binding.pvPlayerView.hideController()
                binding.pvPlayerView.useController = false
                binding.openLockPlayVideo.setImageResource(R.drawable.ic_lock_vector)
            }
            else{
                isLocked = false
                binding.pvPlayerView.useController = true
                binding.pvPlayerView.showController()
                binding.openLockPlayVideo.setImageResource(R.drawable.lock_open_icon)
            }
        }


    }

    private fun setReplayAndFowardTime(positionMs : Long) {
        binding.imbReplay.setOnClickListener {
            player.seekTo(player.currentPosition - positionMs)
        }
        binding.imbForward.setOnClickListener {
            player.seekTo(player.currentPosition + positionMs)

        }
    }

    private fun playVideo() {
        binding.imbPlayPause.setImageResource(R.drawable.ic_pause_video_icon)
        player.play()
    }
    private fun pauseVideo() {
        binding.imbPlayPause.setImageResource(R.drawable.ic_play_icon_video)
        player.pause()
        updateLastCurrent()
    }
    private fun nextPrevVideo(isNext : Boolean = true){
        updateLastCurrent()
        if(isNext)

            setPosition(isIncrement = true)
        else
            setPosition()
        setUpExoPlayer()

    }

    fun updateLastCurrent(){
        if(App.getDB().videoContinue().isVideoContinueIsExist(playerList[position].nameVideo)){

            // update
            App.getDB().videoContinue().updateLastCurrentVideo(playerList[position].nameVideo,
                player.currentPosition)
        }else{

            // insert
            App.getDB().videoContinue().insert(VideoContinue(playerList[position].nameVideo, player.currentPosition,
                player.duration))
        }
    }
    private fun setPosition(isIncrement : Boolean = false){

        if(!repeat){
           if(isIncrement){
              if(playerList.size-1 == position)
                 position = 0
              else
                 position++
           }else{
              if(position == 0)
                  position = playerList.size-1

               else
                --position

           }
        }
    }

    private fun initializeLayout() {
        when(intent.getStringExtra(CLASS_ITEM_VIDEO_PLAY)){
            "AllVideos" -> {
                playerList = ArrayList()
                videoViewModel.getAllVideos(this).observe(this, Observer {
                    playerList.addAll(it)
                    setUpExoPlayer()
                })


            }
            "FolderActivity" -> {
                playerList = ArrayList()
                playerList.addAll(FolderVideoActivity.currentFolderVideos)
                setUpExoPlayer()
            }
            "SearchVideos" -> {
                playerList = ArrayList()
                playerList.addAll(VideoFragmentVideo.searchList)
                setUpExoPlayer()
            }
            "NowPlaying" -> {
                binding.videoTitle.text = playerList[position].nameVideo
                binding.videoTitle.isSelected = true
                binding.pvPlayerView.player = player
                playVideo()
                playInFullScreen(isFullScreen)
                setVisibility()
            }
            "AllPrivateVideos" -> {
                playerListPrivate = ArrayList()
                playerListPrivate.addAll(PrivateVideoActivity.arrayList)
                try {
                    player.release()
                }catch (e:Exception){}

                val trackSelector = DefaultTrackSelector(this)
                trackSelector.setParameters(trackSelector
                    .buildUponParameters()
                    .setMaxVideoSize(300,300)
                    .setForceHighestSupportedBitrate(true)
                )

                player = ExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build()
                binding.pvPlayerView.player = player
                binding.videoTitle.text = playerListPrivate[position].nameVideoPrivate
                val mediaItem = MediaItem.fromUri(playerListPrivate[position].artUri)
                player.setMediaItem(mediaItem)
                player.prepare()
                playVideo()
                player.addListener(object : Player.Listener{
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        if(playbackState == Player.STATE_ENDED)
                            nextPrevVideo()

                    }
                })
                playInFullScreen(isFullScreen)
                setVisibility()
                nowPlayingId = playerListPrivate[position].toString()
            }
        }
        if(repeat)
            binding.imbRepeat.setImageResource(R.drawable.ic_repeat_play)

        else
            binding.imbRepeat.setImageResource(R.drawable.ic_repeat_play_off)

    }

    private fun setUpExoPlayer() {
        try {
            player.release()
        }catch (e:Exception){}
        player = ExoPlayer.Builder(this).build()
        binding.pvPlayerView.player = player
        binding.videoTitle.text = playerList[position].nameVideo

        val mediaItem = MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        if(App.getDB().videoContinue().isVideoContinueIsExist(playerList[position].nameVideo)){
            player.seekTo(App.getDB().videoContinue().selectWithNameVideo(playerList[position].nameVideo).lastCurrentVideo)
        }
        playVideo()
        player.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if(playbackState == Player.STATE_ENDED)
                    nextPrevVideo()
            }
        })
        playInFullScreen(isFullScreen)
        setVisibility()
        nowPlayingId = playerList[position].id.toString()
        App.getDB().videoRecent().insert(playerList[position])

        player.videoScalingMode = android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT

    }

    private fun playInFullScreen(enable:Boolean){
        if(enable){
            binding.imgFullScreen.setImageResource(R.drawable.ic_ful_exit)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }else{
            binding.imgFullScreen.setImageResource(R.drawable.ic_maximize)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        }
    }



    private fun setVisibility(){
        runnable = Runnable {
            if(binding.pvPlayerView.isControllerVisible)
                changeVisibility(View.VISIBLE)
            else
                changeVisibility(View.GONE)
            Handler(Looper.getMainLooper()).postDelayed(runnable,300)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun changeVisibility(visibility : Int){
        setChangeImageReplayAndForwadTime()
        binding.topController.visibility = visibility
        binding.bottomController.visibility = visibility
        binding.imbPlayPause.visibility = visibility
        binding.imbForward.visibility = visibility
        binding.imbReplay.visibility = visibility

        if(isLocked) binding.openLockPlayVideo.visibility = View.VISIBLE
        else binding.openLockPlayVideo.visibility = visibility

        binding.pvPlayerView.setOnTouchListener { _, motionEvent ->
            gestureDetectorCompat.onTouchEvent(motionEvent)
            if(motionEvent.action == MotionEvent.ACTION_UP){
                binding.brightnessIcon.visibility = View.GONE
                binding.VolumeIcon.visibility = View.GONE
            }
            return@setOnTouchListener false
        }
    }

    private fun setChangeImageReplayAndForwadTime() {
        when(UseSharedPreferences.getStringPreferences(PREFS_TIME_VIDEO,this, KEY_TIME_VIDEO)){
            "5s"  -> {
                binding.imbReplay.setImageResource(R.drawable.ic_replay_5)
                binding.imbForward.setImageResource(R.drawable.ic_forward_5)
            }
            "10s" -> {
                binding.imbReplay.setImageResource(R.drawable.ic_replay)
                binding.imbForward.setImageResource(R.drawable.ic_forward)
            }
            "30s" ->{
                binding.imbReplay.setImageResource(R.drawable.ic_replay_30)
                binding.imbForward.setImageResource(R.drawable.ic_forward_30)
            }

        }
    }


    override fun onDestroy() {
        updateLastCurrent()
        super.onDestroy()
        player.pause()
    }

    override fun onDown(e: MotionEvent?):Boolean = false

    override fun onShowPress(e: MotionEvent?) = Unit

    override fun onSingleTapUp(e: MotionEvent?): Boolean = false

    override fun onLongPress(e: MotionEvent?) = Unit

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean = false

    override fun onScroll(
        event: MotionEvent?,
        event1: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {

        val sWidth = Resources.getSystem().displayMetrics.widthPixels

        if(abs(distanceX) < abs(distanceY)){
            if(event!!.x <sWidth/2){
                // brightness
                binding.brightnessIcon.visibility = View.VISIBLE
                binding.VolumeIcon.visibility = View.GONE
                val increase = distanceY > 0
                val newValue = if(increase) brightness + 1 else brightness - 1
                if(newValue in 0..30) brightness = newValue
                binding.brightnessIcon.text = brightness.toString()
                UseSharedPreferences.putIntPreferences(PREF_BRIGHTNESS_VALUE,this,
                    KEY_BRIGHTNESS_VALUE,brightness)
                    setScreenBrightNess(brightness)

            }else{
                // volume
                binding.brightnessIcon.visibility = View.GONE
                binding.VolumeIcon.visibility = View.VISIBLE
                val maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val increase = distanceY > 0
                val newValue = if(increase) volumetitle + 1 else volumetitle - 1
                if(newValue in 0..maxVolume) volumetitle = newValue
                binding.VolumeIcon.text = volumetitle.toString()
                audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC,volumetitle,0)
            }
        }
        return true
    }

    private fun setScreenBrightNess(value : Int){
        val d = 1.0f/30
        val lp = this.window.attributes
        lp.screenBrightness = d * value
        this.window.attributes = lp
    }

    override fun onResume() {
        super.onResume()
        if(audioManager == null) audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager!!.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)

        if(brightness !=0){
            if(UseSharedPreferences.getBooleanPreferences(PREF_BRIGHTNESS,this, KEY_BRIGHTNESS)){
                setScreenBrightNess(UseSharedPreferences.getIntPreferences(PREF_BRIGHTNESS_VALUE,this,
                    KEY_BRIGHTNESS_VALUE))
            }
            setScreenBrightNess(brightness)
        }
        Log.e("tag","onResume")

    }

    override fun onAudioFocusChange(focusChange: Int) {
//        if(focusChange <=0) pauseVideo()
    }



}
