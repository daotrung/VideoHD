package com.daotrung.myapplication.activity

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.SeekBar
import androidx.core.view.GestureDetectorCompat
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.databinding.ActivitySongPlayingBinding
import com.daotrung.myapplication.fragment.ListSongFragment
import com.daotrung.myapplication.model.Events
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.model.enum.PlaybackSetting
import com.daotrung.myapplication.service.MusicService
import com.daotrung.myapplication.util.*
import com.daotrung.myapplication.util.view.CircularSeekBar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.MEDIUM_ALPHA
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SongPlayingActivity : BaseActivity<ActivitySongPlayingBinding>(),
    ListSongFragment.SongListener {
    override fun binding(): ActivitySongPlayingBinding {
        return ActivitySongPlayingBinding.inflate(layoutInflater)
    }

    companion object {
        fun launch(context: Context, item: MusicLocal) {
            context.startActivity(Intent(context, SongPlayingActivity::class.java).apply {
                putExtra(CURRENT_SONG, Gson().toJson(item))
                putExtra(RESTART_PLAYER, true)
            })
        }
    }

    val songType = object : TypeToken<MusicLocal>() {}.type
    private val song by lazy {
        Gson().fromJson(intent.getStringExtra(CURRENT_SONG), songType) ?: MusicService.mCurrentSong
    }
    private val restart by lazy {
        intent.getBooleanExtra(RESTART_PLAYER, false)
    }

    private var bus: EventBus? = null
    private var isThirdPartyIntent = false
    private val SWIPE_DOWN_THRESHOLD = 100
    private lateinit var audioManager: AudioManager
    private lateinit var anim: ObjectAnimator

    private var mSongsFragment: ListSongFragment? = null

    override fun initView() {
        bus = EventBus.getDefault()
        bus!!.register(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setupController()
        setupListener()
        setupSeekbarVolume()
        mSongsFragment = ListSongFragment()
        mSongsFragment?.setSongListener(this)
        binding.imvBack.setOnClickListener {
            onBackPressed()
        }
        isThirdPartyIntent = intent.action == Intent.ACTION_VIEW
        binding.apply {
            arrayOf(imvShuffle, imvPrev, imvNext, imvRepeat).forEach {
                it.beInvisibleIf(isThirdPartyIntent)
            }

            arrayOf(imvBar, imvTop, tvMusicList).forEach {
                it.setOnClickListener {
                    showBottomSheetDialogFragment(mSongsFragment)
                }
            }
        }
        if (isThirdPartyIntent) {
            initThirdPartyIntent()
            return
        }
        if (song == null) {
            toast(R.string.unknown_error_occurred)
            finish()
            return
        }
        window.statusBarColor = Color.parseColor("#000000")
        setupViewSong(song!!)
        anim = AnimatorInflater.loadAnimator(this, R.animator.rotating) as ObjectAnimator
        anim.target = binding.imBgVolume
        anim.start()
        if (restart) {
            intent.removeExtra(RESTART_PLAYER)
            Intent(this, MusicService::class.java).apply {
                putExtra(TRACK_ID, song?.mediaStoreId)
                action = INIT
                try {
                    startService(this)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        } else {
            sendIntent(BROADCAST_STATUS)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        EventBus.getDefault().post(Events.Event(keyCode))
        return super.onKeyDown(keyCode, event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onKeyEvent(key: Events.Event) {
        if (key.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (binding.seekbarVolume.progress >= 0 && binding.seekbarVolume.progress < binding.seekbarVolume.max) {
                binding.seekbarVolume.progress += 1
            }
        } else if (key.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (binding.seekbarVolume.progress >= 1 && binding.seekbarVolume.progress <= binding.seekbarVolume.max) {
                binding.seekbarVolume.progress -= 1
            }
        }
    }

    private fun setupSeekbarVolume() {
        binding.seekbarVolume.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.seekbarVolume.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        binding.seekbarVolume.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                binding.seekbarVolume.progress = progress
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar?.progress!!, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
            }

        })
    }

    private fun initThirdPartyIntent() {
        val fileUri = intent.data
        Intent(this, MusicService::class.java).apply {
            data = fileUri
            action = INIT_PATH

            try {
                startService(this)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    private fun setupViewSong(song: MusicLocal) {
        binding.tvName.text = song.title
        binding.tvArtist.text = song.artist
        binding.seekbar.max = song.duration
        binding.tvDuration.text = song.duration.getFormattedDuration()
    }

    private fun setupController() {
        binding.imvShuffle.setOnClickListener { onClickShuffle() }
        binding.imvPrev.setOnClickListener {
            anim.cancel()
            anim.start()
            sendIntent(PREVIOUS)
        }
        binding.imvPlay.setOnClickListener { sendIntent(PLAYPAUSE) }
        binding.imvNext.setOnClickListener {
            anim.cancel()
            anim.start()
            sendIntent(NEXT)
        }
        binding.tvTime.setOnClickListener { sendIntent(SKIP_BACKWARD) }
        binding.tvDuration.setOnClickListener { sendIntent(SKIP_FORWARD) }
        binding.imvRepeat.setOnClickListener { onClickRepeat() }
        setupShuffle()
        setupRepeat()
        setupSeekbar()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListener() {
        val flingListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (velocityY > 0 && velocityY > velocityX && e2.y - e1.y > SWIPE_DOWN_THRESHOLD) {
                    finish()
                    binding.activitySongTopShadow.animate().alpha(0f).start()
                    overridePendingTransition(0, R.anim.slide_down)
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        }

        val gestureDetector = GestureDetectorCompat(this, flingListener)
        binding.activitySongHolder.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun onClickShuffle() {
        val isShuffleEnabled = !config.isShuffleEnabled
        config.isShuffleEnabled = isShuffleEnabled
        toast(if (isShuffleEnabled) R.string.shuffle_enabled else R.string.shuffle_disabled)
        setupShuffle()
        sendIntent(REFRESH_LIST)
    }

    private fun setupShuffle() {
        val isShuffleEnabled = config.isShuffleEnabled
        binding.imvShuffle.apply {
            applyColorFilter(if (isShuffleEnabled) getProperPrimaryColor() else getProperTextColor())
            alpha = if (isShuffleEnabled) 1f else MEDIUM_ALPHA
            contentDescription =
                getString(if (isShuffleEnabled) R.string.disable_shuffle else R.string.enable_shuffle)
        }
    }

    private fun onClickRepeat() {
        val newPlaybackSetting = config.playbackSetting.nextPlaybackOption
        config.playbackSetting = newPlaybackSetting

        toast(newPlaybackSetting.descriptionStringRes)
        setupRepeat()
    }

    private fun setupRepeat() {
        val playbackSetting = config.playbackSetting
        binding.imvRepeat.apply {
            contentDescription = getString(playbackSetting.contentDescriptionStringRes)
            setImageResource(playbackSetting.iconRes)

            val isRepeatOff = playbackSetting == PlaybackSetting.REPEAT_OFF

            alpha = if (isRepeatOff) MEDIUM_ALPHA else 1f
            applyColorFilter(if (isRepeatOff) getProperTextColor() else getProperPrimaryColor())
        }
    }

    private fun setupSeekbar() {

        binding.seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val formattedProgress = progress.getFormattedDuration()
                binding.tvTime.text = formattedProgress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Intent(this@SongPlayingActivity, MusicService::class.java).apply {
                    putExtra(PROGRESS, seekBar.progress)
                    action = SET_PROGRESS
                    startService(this)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        bus?.unregister(this)

        if (isThirdPartyIntent && !isChangingConfigurations) {
            sendIntent(FINISH_IF_NOT_PLAYING)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun progressUpdated(event: Events.ProgressUpdated) {
        binding.seekbar.progress = event.progress
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun songStateChanged(event: Events.SongStateChanged) {
        if (event.isPlaying) {
            anim.start()
            binding.imvPlay.setImageResource(R.drawable.ic_pause_song)
        } else {
            binding.imvPlay.setImageResource(R.drawable.ic_play_song)
            anim.pause()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun songChangedEvent(event: Events.SongChanged) {
        val track = event.song
        if (track == null) {
            finish()
        } else {
            setupViewSong(event.song)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun nextSongChangedEvent(event: Events.NextSongChanged) {
    }

    override fun songClick(song: MusicLocal) {
        Intent(this, MusicService::class.java).apply {
            putExtra(TRACK_ID, song.mediaStoreId)
            action = INIT
            try {
                startService(this)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

}