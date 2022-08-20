package com.daotrung.myapplication.activity

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AlbumColumns.ALBUM
import android.provider.MediaStore.Audio.AlbumColumns.ARTIST
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.abs.App.Companion.musicViewModel
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.adapter.MusicAdapter
import com.daotrung.myapplication.databinding.ActivityAlbumBinding
import com.daotrung.myapplication.databinding.ContentScrollingBinding
import com.daotrung.myapplication.interfaces.SongInterface
import com.daotrung.myapplication.model.Album
import com.daotrung.myapplication.model.Artist
import com.daotrung.myapplication.model.FolderMusic
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.commons.extensions.deleteFromMediaStore
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.isQPlus
import java.io.File
import java.util.ArrayList

class SongsActivity : BaseActivity<ActivityAlbumBinding>(), SongInterface {

    private val TYPE_PLAYLIST = 1
    private val TYPE_FOLDER = 2
    private val TYPE_ALBUM = 3
    private val TYPE_ARTIST = 4

    override fun binding(): ActivityAlbumBinding {
        return ActivityAlbumBinding.inflate(layoutInflater)
    }

    private lateinit var bindingRcv: ContentScrollingBinding
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var deletedSongUri: Uri? = null

    val albumType = object : TypeToken<Album>() {}.type
    private val currentAlbum by lazy {
        Gson().fromJson<Album>(intent.getStringExtra(ALBUM), albumType)
    }

    val artistType = object : TypeToken<Artist>() {}.type
    private val currentArtist by lazy {
        Gson().fromJson<Artist>(intent.getStringExtra(ARTIST), artistType)
    }

    val folderType = object : TypeToken<FolderMusic>() {}.type
    private val currentFolder by lazy {
        Gson().fromJson<FolderMusic>(intent.getStringExtra(FOLDER), folderType)
    }

    private var musicAdapter: MusicAdapter? = null
    private var TYPE = 0

    override fun initView() {
        window.statusBarColor = Color.rgb(24, 20, 40)
        setupClick()
        setSupportActionBar(binding.toolbar)
        bindingRcv = ContentScrollingBinding.bind(binding.root)
        val titleToUse = currentAlbum?.title ?: currentArtist?.title ?: currentFolder.title ?: ""
        binding.tvName.text = titleToUse
        val mLayout = LinearLayoutManager(this)
        bindingRcv.rcvSongs.layoutManager = mLayout
        if (currentAlbum != null) {
            TYPE = TYPE_ALBUM
            initViewAlbum()
        }

        if (currentArtist != null) {
            TYPE = TYPE_ARTIST
            initViewArtis()
        }

        if (currentFolder != null) {
            TYPE = TYPE_FOLDER
            initViewFolder()
        }

        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        deletedSongUri?.let { it1 -> deleteSongFromExternalStorage(it1, mediaID) }
                    }
                    Toast.makeText(this, "Song deleted successfully", Toast.LENGTH_SHORT)
                        .show()
                    App.getDB().songsDao().removeSong(mediaID)
                    musicViewModel.forceReload()
                }
            }
    }

    private fun initViewAlbum() {
        val options = RequestOptions()
            .error(R.drawable.ic_bg_folder)
        Glide.with(this)
            .load(currentAlbum.coverArt)
            .apply(options)
            .into(binding.imvAvatar)
        musicAdapter = MusicAdapter(itemClick = {
            SongPlayingActivity.launch(this, it)
        }, this).apply {
            bindingRcv.rcvSongs.adapter = this
        }
        if (areSystemAnimationsEnabled) {
            bindingRcv.rcvSongs.scheduleLayoutAnimation()
        }
        musicViewModel.getSongsAlbum(currentAlbum.id).observe(this, Observer { responseBody ->
            runOnUiThread {
                responseBody?.let {
                    musicAdapter?.sumblist(it)
                    resetQueueItems(it) {}
                }
            }
        })
    }

    private fun initViewArtis() {
        val options = RequestOptions()
            .error(R.drawable.ic_bg_folder)
        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        val albumArtUri = ContentUris.withAppendedId(artworkUri, currentArtist.albumArtId)
        Glide.with(this)
            .load(albumArtUri)
            .apply(options)
            .into(binding.imvAvatar)
        musicAdapter = MusicAdapter(itemClick = {
            SongPlayingActivity.launch(this, it)
        }, this).apply {
            bindingRcv.rcvSongs.adapter = this
        }
        if (areSystemAnimationsEnabled) {
            bindingRcv.rcvSongs.scheduleLayoutAnimation()
        }
        musicViewModel.getSongsArtis(currentArtist).observe(this, Observer { reponseBody ->
            runOnUiThread {
                reponseBody?.let {
                    musicAdapter?.sumblist(it)
                    resetQueueItems(it) {}
                }
            }
        })
    }

    private fun initViewFolder() {
        val showFilename = config.showFilename
        Glide.with(this).load(R.drawable.ic_bg_folder).into(binding.imvAvatar)
        val folderSongs =
            App.getDB().songsDao().getSongsFromFolder(currentFolder.title ?: "").map { song ->
                song.title = song.getProperTitle(showFilename)
                song
            }
        runOnUiThread {
            if (musicAdapter == null) {
                musicAdapter = MusicAdapter(itemClick = {
                    SongPlayingActivity.launch(this, it)
                }, this).apply {
                    sumblist(folderSongs)
                    bindingRcv.rcvSongs.adapter = this
                    resetQueueItems(folderSongs) {}
                }
                if (areSystemAnimationsEnabled) {
                    bindingRcv.rcvSongs.scheduleLayoutAnimation()
                }
            } else {
                musicAdapter?.sumblist(folderSongs)
            }
        }
    }

    private fun setupClick() {
        binding.imvBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun playNext(song: MusicLocal) {
    }

    override fun addPlayList(song: MusicLocal) {
    }

    override fun setRingTone(song: MusicLocal) {
        if (RingtoneManager.requiresDialog(this)) {
            RingtoneManager.showDialog(this)
        } else {
            if (VersionUtils.hasQ()) {
                RingtoneManager.setRingtone(this, song)
            } else {
                if (RingtoneManager.setAsRingtoneAndroid(this, File(song.path))) {
                    val message = getString(R.string.x_has_been_set_as_ringtone, song.title)
                    toast(message)
                } else {
                    val message = getString(R.string.x_fail_has_been_set_as_ringtone, song.title)
                    toast(message)
                }
            }
        }
    }

    private var mediaID: Long = 0
    override fun delete(song: MusicLocal) {
        mediaID = song.mediaStoreId
        deletedSongUri = Uri.parse(song.path)
        if (isQPlus()) {
            deleteSongFromExternalStorage(Uri.parse(song.path), song.mediaStoreId)
        } else deleteFromMediaStore(song.path) {
            if (it) {
                App.getDB().songsDao().removeSong(mediaID)
                toast(getString(R.string.deleted_x_songs, song.title))
                musicViewModel.forceReload()
            }
        }
    }

    override fun setFavourite(song: MusicLocal) {
    }

    override fun share(song: MusicLocal) {
        startActivity(
            Intent.createChooser(
                MusicUtil.createShareSongMultiFileIntent(listOf(song), this),
                null
            )
        )
    }

    private fun deleteSongFromExternalStorage(songUri: Uri, mediaStoreId: Long) {
        try {
            contentResolver.delete(songUri, null, null)
        } catch (e: Exception) {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val uris = arrayListOf<Uri>()
            val newUri = ContentUris.withAppendedId(uri, mediaStoreId)
            uris.add(newUri)
            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(
                        contentResolver,
                        uris
                    ).intentSender

                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val recoverableSecurityException = e as? RecoverableSecurityException
                    recoverableSecurityException?.userAction?.actionIntent?.intentSender
                }
                else -> null
            }
            intentSender?.let { sender ->
                intentSenderLauncher.launch(
                    IntentSenderRequest.Builder(sender).build()
                )
            }
        }
    }
}