package com.daotrung.myapplication.service

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.activity.MainActivity
import com.daotrung.myapplication.model.Events
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.model.enum.PlaybackSetting
import com.daotrung.myapplication.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt


class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener,
    AudioManager.OnAudioFocusChangeListener {
    companion object {
        private const val PROGRESS_UPDATE_INTERVAL = 1000L
        private const val MAX_CLICK_DURATION = 700L
        private const val FAST_FORWARD_SKIP_MS = 10000
        private const val NOTIFICATION_CHANNEL = "music_video_hd"
        private const val NOTIFICATION_ID = 123

        var mCurrentSong: MusicLocal? = null
        var mListSongs = ArrayList<MusicLocal>()
        var mPlayer: MediaPlayer? = null
        var mEqualizer: Equalizer? = null
        var mBassBoost: BassBoost? = null
        var mVisualizer: Virtualizer? = null

        private var mHeadsetPlugReceiver = BroadReceiver()
        private var mProgressHandler = Handler()
        private var mSleepTimer: CountDownTimer? = null
        private var mAudioManager: AudioManager? = null
        private var mRetriedTrackCount = 0
        private var mPlaybackSpeed = 1f

        @SuppressLint("StaticFieldLeak")
        private var mOreoFocusHandler: OreoAudioFocusHandler? = null

        private var mWasPlayingAtFocusLost = false
        private var mPlayOnPrepare = true
        private var mIsThirdPartyIntent = false
        private var mIntentUri: Uri? = null
        private var mMediaSession: MediaSessionCompat? = null
        private var mIsServiceInitialized = false
        private var mPrevAudioFocusState = 0
        private var mSetProgressOnPrepare = 0

        fun getIsPlaying() = mPlayer?.isPlaying == true
    }

    private var mClicksCnt = 0
    private val mRemoteControlHandler = Handler()
    private val mRunnable = Runnable {
        if (mClicksCnt == 0) {
            return@Runnable
        }

        when (mClicksCnt) {
            1 -> handlePlayPause()
            2 -> handleNext()
            else -> handlePrevious()
        }
        mClicksCnt = 0
    }

    override fun onCreate() {
        super.onCreate()
        mMediaSession = MediaSessionCompat(this, "MusicService")
        mMediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        mMediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                handleMediaButton(mediaButtonEvent)
                return true
            }
        })

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (isOreoPlus()) {
            mOreoFocusHandler = OreoAudioFocusHandler(applicationContext)
        }

        if (!isQPlus() && !hasPermission(PERMISSION_WRITE_STORAGE)) {
            EventBus.getDefault().post(Events.NoStoragePermission())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyPlayer()
        mMediaSession?.isActive = false
        mEqualizer?.release()
        mEqualizer = null
        mBassBoost?.release()
        mBassBoost = null
        mVisualizer?.release()
        mVisualizer = null
        mSleepTimer?.cancel()
        config.sleepInTS = 0L
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (!isQPlus() && !hasPermission(PERMISSION_WRITE_STORAGE)) {
            return START_NOT_STICKY
        }

        focusGained()

        val action = intent.action
        if (isOreoPlus() && action != NEXT && action != PREVIOUS && action != PLAYPAUSE) {
            notificationAndroid8()
        }

        when (action) {
            INIT -> handleInit(intent)
            INIT_PATH -> handleInitPath(intent)
            INIT_QUEUE -> handleInitQueue()
            PREVIOUS -> handlePrevious()
            PAUSE -> pauseSong()
            PLAYPAUSE -> handlePlayPause()
            NEXT -> handleNext()
            PLAY_SONG -> playSong(intent)
            EDIT -> handleEdit(intent)
            FINISH -> handleFinish()
            FINISH_IF_NOT_PLAYING -> finishIfNotPlaying()
            REFRESH_LIST -> handleRefreshList()
            UPDATE_NEXT_TRACK -> broadcastNextSongChange()
            SET_PROGRESS -> handleSetProgress(intent)
            SKIP_BACKWARD -> skip(false)
            SKIP_FORWARD -> skip(true)
            START_SLEEP_TIMER -> startSleepTimer()
            STOP_SLEEP_TIMER -> stopSleepTimer()
            BROADCAST_STATUS -> broadcastPlayerStatus()
//            SET_PLAYBACK_SPEED -> setPlaybackSpeed()
            UPDATE_QUEUE_SIZE -> updateQueueSize()
        }

        MediaButtonReceiver.handleIntent(mMediaSession!!, intent)
        setupNotification()
        return START_NOT_STICKY
    }

    private fun focusGained() {
        mWasPlayingAtFocusLost = false
        mPrevAudioFocusState = AudioManager.AUDIOFOCUS_GAIN
    }

    private fun initService(intent: Intent?) {
        mListSongs.clear()
        mCurrentSong = null
        if (mIsThirdPartyIntent && mIntentUri != null) {
            val path = getRealPathFromURI(mIntentUri!!) ?: ""
            val track = RoomHelper(this).getTrackFromPath(path)
            if (track != null) {
                if (track.title.isEmpty()) {
                    track.title = mIntentUri?.toString()?.getFilenameFromPath() ?: ""
                }

                if (track.duration == 0) {
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(this, mIntentUri)
                        track.duration =
                            (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                                .toInt() / 1000f).roundToInt()
                    } catch (ignored: Exception) {
                    }
                }

                mListSongs.add(track)
            }
        } else {
            mListSongs = getQueuedTracks()

            val wantedTrackId = intent?.getLongExtra(TRACK_ID, -1L)
            mCurrentSong = mListSongs.firstOrNull { it.mediaStoreId == wantedTrackId }
            checkTrackOrder()
        }

        mWasPlayingAtFocusLost = false
        initMediaPlayerIfNeeded()
        setupNotification()
        mIsServiceInitialized = true
    }

    private fun handleInit(intent: Intent? = null) {
        mIsThirdPartyIntent = false
        ensureBackgroundThread {
            initService(intent)
            val wantedTrackId = intent?.getLongExtra(TRACK_ID, -1L) ?: -1L
            mPlayOnPrepare = true
            setTrack(wantedTrackId)
        }
    }

    private fun handleInitPath(intent: Intent) {
        mIsThirdPartyIntent = true
        if (mIntentUri != intent.data) {
            mIntentUri = intent.data
            initService(intent)
            initTracks()
        } else {
            updateUI()
        }
    }

    private fun handleInitQueue() {
        ensureBackgroundThread {
            val unsortedTracks = getQueuedTracks()

            mListSongs.clear()
            val queuedItems = App.getDB().queueDao().getAll()
            queuedItems.forEach { queueItem ->
                unsortedTracks.firstOrNull { it.mediaStoreId == queueItem.trackId }?.apply {
                    mListSongs.add(this)
                }
            }

            checkTrackOrder()
            val currentQueueItem =
                queuedItems.firstOrNull { it.isCurrent } ?: queuedItems.firstOrNull()
            if (currentQueueItem != null) {
                mCurrentSong =
                    mListSongs.firstOrNull { it.mediaStoreId == currentQueueItem.trackId }
                        ?: return@ensureBackgroundThread
                mPlayOnPrepare = false
                mSetProgressOnPrepare = currentQueueItem.lastPosition
                setTrack(mCurrentSong!!.mediaStoreId)
            }
        }
    }

    private fun handlePrevious() {
        mPlayOnPrepare = true
        playPreviousTrack()
    }

    private fun handlePlayPause() {
        mPlayOnPrepare = true
        if (getIsPlaying()) {
            pauseSong()
        } else {
            resumeTrack()
        }
    }

    private fun handleNext() {
        mPlayOnPrepare = true
        setupNextTrack()
    }

    private fun handleEdit(intent: Intent) {
        mCurrentSong = intent.getSerializableExtra(EDITED_TRACK) as MusicLocal
        songChanged()
    }

    private fun finishIfNotPlaying() {
        if (!getIsPlaying()) {
            handleFinish()
        }
    }

    private fun handleFinish() {
        broadcastSongProgress(0)
        stopSelf()
    }

    private fun handleRefreshList() {
        ensureBackgroundThread {
            mListSongs = getQueuedTracks()
            checkTrackOrder()
            EventBus.getDefault().post(Events.QueueUpdated(mListSongs))
            broadcastNextSongChange()
        }
    }

    private fun handleSetProgress(intent: Intent) {
        if (mPlayer != null) {
            val progress = intent.getIntExtra(PROGRESS, mPlayer!!.currentPosition / 1000)
            updateProgress(progress)
        }
    }

    private fun setupTrack() {
        if (mIsThirdPartyIntent) {
            initMediaPlayerIfNeeded()

            try {
                mPlayer!!.apply {
                    reset()
                    setDataSource(applicationContext, mIntentUri!!)
                    prepare()
                    start()
                }
                requestAudioFocus()

                val track = mListSongs.first()
                mListSongs.clear()
                mListSongs.add(track)
                mCurrentSong = track
                updateUI()
            } catch (ignored: Exception) {
            }
        } else {
            mPlayOnPrepare = false
            setupNextTrack()
        }
    }

    private fun initTracks() {
        if (mCurrentSong == null) {
            setupTrack()
        }
        updateUI()
    }

    private fun updateUI() {
        if (mPlayer != null) {
            EventBus.getDefault().post(Events.QueueUpdated(mListSongs))
            broadcastTrackChange()

            val secs = mPlayer!!.currentPosition / 1000
            broadcastSongProgress(secs)
        }
        songStateChanged(getIsPlaying())
    }

    private fun initMediaPlayerIfNeeded() {
        if (mPlayer != null) {
            return
        }

        mPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MusicService)
            setOnCompletionListener(this@MusicService)
            setOnErrorListener(this@MusicService)
        }
        setupEqualizer()
    }

    private fun setupEqualizer() {
        if (mPlayer == null) {
            return
        }

        try {
            val preset = config.equalizerPreset
            val bass = config.bassStrength
            val virtualizer = config.virtualizerStrength
            mEqualizer = Equalizer(0, mPlayer!!.audioSessionId)
            mBassBoost = BassBoost(0, mPlayer!!.audioSessionId)
            mVisualizer = Virtualizer(0, mPlayer!!.audioSessionId)
            if (!mEqualizer!!.enabled) {
                mEqualizer!!.enabled = true
            }
            if (!mBassBoost!!.enabled) {
                mBassBoost!!.enabled = true
            }
            if (!mVisualizer!!.enabled) {
                mVisualizer!!.enabled = true
            }

            if (preset != EQUALIZER_PRESET_CUSTOM) {
                mEqualizer!!.usePreset(preset.toShort())
            } else {
                val minValue = mEqualizer!!.bandLevelRange[0]
                val bandType = object : TypeToken<HashMap<Short, Int>>() {}.type
                val equalizerBands =
                    Gson().fromJson<HashMap<Short, Int>>(config.equalizerBands, bandType)
                        ?: HashMap()

                for ((key, value) in equalizerBands) {
                    val newValue = value + minValue
                    if (mEqualizer!!.getBandLevel(key) != newValue.toShort()) {
                        mEqualizer!!.setBandLevel(key, newValue.toShort())
                    }
                }
            }

            if (bass != EQUALIZER_PRESET_CUSTOM) {
                val bassBoostSettingTemp = mBassBoost!!.properties
                val bassBoostSetting = BassBoost.Settings(bassBoostSettingTemp.toString())
                try {
                    bassBoostSetting.strength = bass.toShort()
                    mBassBoost!!.properties = bassBoostSetting
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (virtualizer != EQUALIZER_PRESET_CUSTOM) {
                val virtualizerSettingTemp = mVisualizer!!.properties
                val virtualizerSetting = Virtualizer.Settings(virtualizerSettingTemp.toString())
                try {
                    virtualizerSetting.strength = virtualizer.toShort()
                    mVisualizer!!.properties = virtualizerSetting
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
        }
    }

    // make sure tracks don't get duplicated in the queue, if they exist in multiple playlists
    private fun getQueuedTracks(): ArrayList<MusicLocal> {
        val tracks = ArrayList<MusicLocal>()
        val allTracks = App.getDB().songsDao().getAll()
        val wantedIds = App.getDB().queueDao().getAll().map { it.trackId }

        // make sure we fetch the songs in the order they were displayed in
        val wantedTracks = ArrayList<MusicLocal>()
        for (wantedId in wantedIds) {
            val wantedTrack = allTracks.firstOrNull { it.mediaStoreId == wantedId }
            if (wantedTrack != null) {
                wantedTracks.add(wantedTrack)
                continue
            }
        }

        tracks.addAll(wantedTracks)
        return tracks.distinctBy { it.mediaStoreId }.toMutableList() as ArrayList<MusicLocal>
    }

    private fun checkTrackOrder() {
        if (config.isShuffleEnabled) {
            mListSongs.shuffle()

            if (mCurrentSong != null) {
                mListSongs.remove(mCurrentSong)
                mListSongs.add(0, mCurrentSong!!)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun setupNotification() {
        val title = mCurrentSong?.title ?: ""
        val artist = mCurrentSong?.artist ?: ""
        val playPauseIcon =
            if (getIsPlaying()) R.drawable.ic_pause_vector else R.drawable.ic_play_vector

        var notifWhen = 0L
        var showWhen = false
        var usesChronometer = false
        var ongoing = false
        if (getIsPlaying()) {
            notifWhen = System.currentTimeMillis() - (mPlayer?.currentPosition ?: 0)
            showWhen = true
            usesChronometer = true
            ongoing = true
        }

        if (isOreoPlus()) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = resources.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
                enableLights(false)
                enableVibration(false)
                notificationManager.createNotificationChannel(this)
            }
        }

        val notificationDismissedIntent =
            Intent(this, NotificationDismissedReceiver::class.java).apply {
                action = NOTIFICATION_DISMISSED
            }

        val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val notificationDismissedPendingIntent =
            PendingIntent.getBroadcast(this, 0, notificationDismissedIntent, flags)

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.imv_music)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setWhen(notifWhen)
            .setShowWhen(showWhen)
            .setUsesChronometer(usesChronometer)
            .setContentIntent(getContentIntent())
            .setOngoing(ongoing)
            .setChannelId(NOTIFICATION_CHANNEL)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mMediaSession?.sessionToken)
            )
            .setDeleteIntent(notificationDismissedPendingIntent)
            .addAction(
                R.drawable.ic_previous_vector,
                getString(R.string.previous),
                getIntent(PREVIOUS)
            )
            .addAction(playPauseIcon, getString(R.string.playpause), getIntent(PLAYPAUSE))
            .addAction(R.drawable.ic_next_vector, getString(R.string.next), getIntent(NEXT))


        try {
            startForeground(NOTIFICATION_ID, notification.build())

            Handler(Looper.getMainLooper()).postDelayed({
                val isFocusLost =
                    mPrevAudioFocusState == AudioManager.AUDIOFOCUS_LOSS || mPrevAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                val isPlaybackStoppedAfterFocusLoss = mWasPlayingAtFocusLost && isFocusLost
                if (!getIsPlaying() && !isPlaybackStoppedAfterFocusLoss) {
                    stopForeground(false)
                }
            }, 200L)
        } catch (e: ForegroundServiceStartNotAllowedException) {
        }
    }

    private fun getContentIntent(): PendingIntent {
        val contentIntent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_MUTABLE)
    }

    private fun getIntent(action: String): PendingIntent {
        val intent = Intent(this, ControlActionsListener::class.java)
        intent.action = action
        return PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )
    }


    @SuppressLint("NewApi")
    private fun notificationAndroid8() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = resources.getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_LOW
        NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
            enableLights(false)
            enableVibration(false)
            notificationManager.createNotificationChannel(this)
        }

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setContentTitle("")
            .setContentText("")
            .setSmallIcon(R.drawable.imv_music)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setChannelId(NOTIFICATION_CHANNEL)
            .setCategory(Notification.CATEGORY_SERVICE)

        startForeground(NOTIFICATION_ID, notification.build())
    }

    private fun getNewTrackId(): Long {
        return when (mListSongs.size) {
            0 -> -1L
            1 -> mListSongs.first().mediaStoreId
            else -> {
                val currentTrackIndex =
                    mListSongs.indexOfFirstOrNull { it.mediaStoreId == mCurrentSong?.mediaStoreId }
                if (currentTrackIndex != null) {
                    val nextTrack = mListSongs[(currentTrackIndex + 1) % mListSongs.size]
                    nextTrack.mediaStoreId
                } else {
                    -1L
                }
            }
        }
    }

    private fun isEndOfPlaylist(): Boolean {
        return when (mListSongs.size) {
            0, 1 -> true
            else -> mCurrentSong?.mediaStoreId == mListSongs.last().mediaStoreId
        }
    }

    private fun playPreviousTrack() {
        if (mListSongs.isEmpty()) {
            handleEmptyPlaylist()
            return
        }

        initMediaPlayerIfNeeded()

        // play the previous track if we are less than 5 secs into it, else restart
        val currentTrackIndex =
            mListSongs.indexOfFirstOrNull { it.mediaStoreId == mCurrentSong?.mediaStoreId } ?: 0
        if (currentTrackIndex == 0 || mPlayer!!.currentPosition > 5000) {
            restartTrack()
        } else {
            val previousTrack = mListSongs[currentTrackIndex - 1]
            setTrack(previousTrack.mediaStoreId)
        }
    }

    private fun pauseSong() {
        initMediaPlayerIfNeeded()
        mPlayer!!.pause()
        songStateChanged(false)
    }

    private fun resumeTrack() {
        if (mListSongs.isEmpty()) {
            handleEmptyPlaylist()
            return
        }

        initMediaPlayerIfNeeded()

        if (mCurrentSong == null) {
            setupNextTrack()
        } else {
            mPlayer!!.start()
            requestAudioFocus()
        }

        setupEqualizer()
        songStateChanged(true)
//        setPlaybackSpeed()
    }

    private fun setupNextTrack() {
        if (mIsThirdPartyIntent) {
            setupTrack()
        } else {
            setTrack(getNewTrackId())
        }
    }

    private fun restartTrack() {
        if (mCurrentSong != null) {
            setTrack(mCurrentSong!!.mediaStoreId)
        }
    }

    private fun playSong(intent: Intent) {
        if (mIsThirdPartyIntent) {
            setupTrack()
        } else {
            mPlayOnPrepare = true
            val trackId = intent.getLongExtra(TRACK_ID, 0L)
            setTrack(trackId)
            broadcastTrackChange()
        }

        mMediaSession?.isActive = true
    }

    private fun setTrack(wantedTrackId: Long) {
        if (mListSongs.isEmpty()) {
            handleEmptyPlaylist()
            return
        }

        initMediaPlayerIfNeeded()
        mPlayer?.reset() ?: return
        mCurrentSong = mListSongs.firstOrNull { it.mediaStoreId == wantedTrackId } ?: return

        try {
            val trackUri = if (mCurrentSong!!.mediaStoreId == 0L) {
                Uri.fromFile(File(mCurrentSong!!.path))
            } else {
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mCurrentSong!!.mediaStoreId
                )
            }

            mPlayer!!.setDataSource(applicationContext, trackUri)
            mPlayer!!.prepareAsync()
            songChanged()
        } catch (e: IOException) {
            if (mCurrentSong != null) {
                val trackToDelete = mCurrentSong
                ensureBackgroundThread {
                    App.getDB().songsDao().removeSong(trackToDelete!!.mediaStoreId)
                }
            }

            if (mRetriedTrackCount < 3) {
                mRetriedTrackCount++
                setupNextTrack()
            }
        } catch (ignored: Exception) {
        }
    }

    private fun handleEmptyPlaylist() {
        mPlayer?.pause()
        abandonAudioFocus()
        mCurrentSong = null
        songChanged()
        songStateChanged(false)

        if (!mIsServiceInitialized) {
            handleInit()
        }
    }

    private val musicBind: IBinder = this.MusicBinder()
    override fun onBind(intent: Intent): IBinder {
        initMediaPlayerIfNeeded()
        return musicBind
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService

        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (!config.autoplay) {
            return
        }

        val playbackSetting = config.playbackSetting

        mPlayOnPrepare = when (playbackSetting) {
            PlaybackSetting.REPEAT_OFF -> !isEndOfPlaylist()
            PlaybackSetting.REPEAT_PLAYLIST, PlaybackSetting.REPEAT_TRACK -> true
            PlaybackSetting.STOP_AFTER_CURRENT_TRACK -> false
        }

        when (config.playbackSetting) {
            PlaybackSetting.REPEAT_OFF -> {
                if (isEndOfPlaylist()) {
                    broadcastSongProgress(0)
                    setupNextTrack()
                } else {
                    setupNextTrack()
                }
            }
            PlaybackSetting.REPEAT_PLAYLIST -> setupNextTrack()
            PlaybackSetting.REPEAT_TRACK -> restartTrack()
            PlaybackSetting.STOP_AFTER_CURRENT_TRACK -> {
                broadcastSongProgress(0)
                restartTrack()
            }
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mPlayer!!.reset()
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        mRetriedTrackCount = 0
        if (mPlayOnPrepare) {
            mp.start()
            requestAudioFocus()

            if (isMarshmallowPlus()) {
                try {
                    mp.playbackParams = mp.playbackParams.setSpeed(config.playbackSpeed)
                } catch (e: Exception) {
                }
            }

            if (mIsThirdPartyIntent) {
                songChanged()
            }
        } else if (mSetProgressOnPrepare > 0) {
            mPlayer?.seekTo(mSetProgressOnPrepare)
            broadcastSongProgress(mSetProgressOnPrepare / 1000)
            mSetProgressOnPrepare = 0
        }

        songStateChanged(getIsPlaying())
        setupNotification()
    }

    private fun songChanged() {
        broadcastTrackChange()
        updateMediaSession()
        updateMediaSessionState()
    }

    private fun updateMediaSession() {

        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mCurrentSong?.album ?: "")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mCurrentSong?.artist ?: "")
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mCurrentSong?.title ?: "")
            .putString(
                MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                mCurrentSong?.mediaStoreId?.toString()
            )
            .putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                (mCurrentSong?.duration?.toLong() ?: 0L) * 1000
            )
            .putLong(
                MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER,
                mListSongs.indexOf(mCurrentSong).toLong() + 1
            )
            .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, mListSongs.size.toLong())
            .build()

        mMediaSession?.setMetadata(metadata)
    }

    private fun updateMediaSessionState() {
        val builder = PlaybackStateCompat.Builder()
        val playbackState = if (getIsPlaying()) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }

        builder.setState(playbackState, mPlayer?.currentPosition?.toLong() ?: 0L, mPlaybackSpeed)
        try {
            mMediaSession?.setPlaybackState(builder.build())
        } catch (ignored: Exception) {
        }
    }

    private fun updateQueueSize() {
        updateMediaSession()
    }

    private fun broadcastTrackChange() {
        Handler(Looper.getMainLooper()).post {
            EventBus.getDefault().post(Events.SongChanged(mCurrentSong))
            broadcastNextSongChange()
        }

        ensureBackgroundThread {
            App.getDB().queueDao().resetCurrent()
            if (mCurrentSong != null && mPlayer != null) {
                App.getDB().queueDao().saveCurrentTrack(mCurrentSong!!.mediaStoreId, 0)
            }
        }
    }


    private fun broadcastNextSongChange() {
//        setPlaybackSpeed()
        Handler(Looper.getMainLooper()).post {
            val currentTrackIndex =
                mListSongs.indexOfFirstOrNull { it.mediaStoreId == mCurrentSong?.mediaStoreId }
            if (currentTrackIndex != null) {
                val nextTrack = mListSongs[(currentTrackIndex + 1) % mListSongs.size]
                EventBus.getDefault().post(Events.NextSongChanged(nextTrack))
            }
        }
    }

    private fun broadcastSongProgress(progress: Int) {
        EventBus.getDefault().post(Events.ProgressUpdated(progress))
        updateMediaSessionState()
    }

    private fun destroyPlayer() {
        if (!mIsThirdPartyIntent) {
            val position = mPlayer?.currentPosition ?: 0
            ensureBackgroundThread {
                try {
                    App.getDB().queueDao().resetCurrent()

                    if (mCurrentSong != null) {
                        App.getDB().queueDao()
                            .saveCurrentTrack(mCurrentSong!!.mediaStoreId, position)
                    }

                    mListSongs.forEachIndexed { index, track ->
                        App.getDB().queueDao().setOrder(track.mediaStoreId, index)
                    }
                } catch (ignored: Exception) {
                }

                mCurrentSong = null
            }
        } else {
            mCurrentSong = null
        }

        mPlayer?.stop()
        mPlayer?.release()
        mPlayer = null

        songStateChanged(false)
        songChanged()

        stopForeground(true)
        stopSelf()
        mIsThirdPartyIntent = false
        mIsServiceInitialized = false
        abandonAudioFocus()
    }

    private fun requestAudioFocus() {
        if (isOreoPlus()) {
            mOreoFocusHandler?.requestAudioFocus(this)
        } else {
            mAudioManager?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        if (isOreoPlus()) {
            mOreoFocusHandler?.abandonAudioFocus()
        } else {
            mAudioManager?.abandonAudioFocus(this)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> audioFocusGained()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> duckAudio()
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> audioFocusLost()
        }
        mPrevAudioFocusState = focusChange
    }

    private fun audioFocusLost() {
        if (getIsPlaying()) {
            mWasPlayingAtFocusLost = true
            pauseSong()
        } else {
            mWasPlayingAtFocusLost = false
        }
    }

    private fun audioFocusGained() {
        if (mWasPlayingAtFocusLost) {
            if (mPrevAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                unduckAudio()
            } else {
                resumeTrack()
            }
        }

        mWasPlayingAtFocusLost = false
    }

    private fun duckAudio() {
        mPlayer?.setVolume(0.3f, 0.3f)
        mWasPlayingAtFocusLost = getIsPlaying()
    }

    private fun unduckAudio() {
        mPlayer?.setVolume(1f, 1f)
    }

    private fun updateProgress(progress: Int) {
        mPlayer!!.seekTo(progress * 1000)
        resumeTrack()
    }

    private fun songStateChanged(isPlaying: Boolean) {
        handleProgressHandler(isPlaying)
        setupNotification()
        broadcastSongStateChange(isPlaying)
        if (isPlaying) {
            val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
            filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            registerReceiver(mHeadsetPlugReceiver, filter)
        } else {
            try {
                unregisterReceiver(mHeadsetPlugReceiver)
            } catch (ignored: IllegalArgumentException) {
            }
        }
    }

    private fun handleProgressHandler(isPlaying: Boolean) {
        if (isPlaying) {
            mProgressHandler.post(object : Runnable {
                override fun run() {
                    if (mPlayer?.isPlaying == true) {
                        val secs = mPlayer!!.currentPosition / 1000
                        broadcastSongProgress(secs)
                    }
                    mProgressHandler.removeCallbacksAndMessages(null)
                    mProgressHandler.postDelayed(
                        this,
                        (PROGRESS_UPDATE_INTERVAL / mPlaybackSpeed).toLong()
                    )
                }
            })
        } else {
            mProgressHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun broadcastSongStateChange(isPlaying: Boolean) {
        EventBus.getDefault().post(Events.SongStateChanged(isPlaying))
    }

    private fun skip(forward: Boolean) {
        val curr = mPlayer?.currentPosition ?: return
        val newProgress = if (forward) curr + FAST_FORWARD_SKIP_MS else curr - FAST_FORWARD_SKIP_MS
        mPlayer!!.seekTo(newProgress)
        resumeTrack()
    }


    private fun startSleepTimer() {
        val millisInFuture = config.sleepInTS - System.currentTimeMillis() + 1000L
        mSleepTimer?.cancel()
        mSleepTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                EventBus.getDefault().post(Events.SleepTimerChanged(seconds))
            }

            override fun onFinish() {
                EventBus.getDefault().post(Events.SleepTimerChanged(0))
                config.sleepInTS = 0
                sendIntent(FINISH)
            }
        }
        mSleepTimer?.start()
    }

    private fun stopSleepTimer() {
        config.sleepInTS = 0
        mSleepTimer?.cancel()
    }

    // used at updating the widget at create or resize
    private fun broadcastPlayerStatus() {
        broadcastTrackChange()
        broadcastNextSongChange()
        broadcastSongProgress((mPlayer?.currentPosition ?: 0) / 1000)
        broadcastSongStateChange(mPlayer?.isPlaying ?: false)
    }

    private fun handleMediaButton(mediaButtonEvent: Intent) {
        if (mediaButtonEvent.action == Intent.ACTION_MEDIA_BUTTON) {
            val swapPrevNext = config.swapPrevNext
            val event =
                mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return
            if (event.action == KeyEvent.ACTION_UP) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY -> resumeTrack()
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> pauseSong()
                    KeyEvent.KEYCODE_MEDIA_STOP -> pauseSong()
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> handlePlayPause()
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> if (swapPrevNext) handleNext() else handlePrevious()
                    KeyEvent.KEYCODE_MEDIA_NEXT -> if (swapPrevNext) handlePrevious() else handleNext()
                    KeyEvent.KEYCODE_HEADSETHOOK -> {
                        mClicksCnt++

                        mRemoteControlHandler.removeCallbacks(mRunnable)
                        if (mClicksCnt >= 3) {
                            mRemoteControlHandler.post(mRunnable)
                        } else {
                            mRemoteControlHandler.postDelayed(mRunnable, MAX_CLICK_DURATION)
                        }
                    }
                }
            }
        }
    }
}