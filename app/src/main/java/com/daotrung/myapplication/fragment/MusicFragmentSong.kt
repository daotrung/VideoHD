package com.daotrung.myapplication.fragment

import android.app.Activity.RESULT_OK
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns.TRACK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.abs.App.Companion.musicViewModel
import com.daotrung.myapplication.activity.SongPlayingActivity
import com.daotrung.myapplication.adapter.MusicAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentMusicSongBinding
import com.daotrung.myapplication.interfaces.SongInterface
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.util.*
import com.daotrung.myapplication.util.AppUtils.hideLoading
import com.daotrung.myapplication.util.AppUtils.showLoading
import com.google.gson.Gson
import com.simplemobiletools.commons.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.commons.extensions.deleteFromMediaStore
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isQPlus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MusicFragmentSong : BaseFragment(), SongInterface {

    private lateinit var binding: FragmentMusicSongBinding
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicSongBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    private var musicAdapter: MusicAdapter? = null
    private var deletedSongUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        deletedSongUri?.let { it1 -> deleteSongFromExternalStorage(it1, mediaID) }
                    }
                    requireActivity().showLoading("Delete loading")
                    App.getDB().songsDao().removeSong(mediaID)
                    Toast.makeText(mContext, "Song deleted successfully", Toast.LENGTH_SHORT).show()
                    mActivity.updateAllDatabases {
                        musicViewModel.forceReload()
                        hideLoading()
                    }
                } else {
                    Toast.makeText(mContext, "Song couldn't be deleted", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun initView() {
        val mLayout = LinearLayoutManager(mContext)
        binding.rvListTabSong.layoutManager = mLayout
        musicAdapter = MusicAdapter(itemClick = {
            SongPlayingActivity.launch(requireContext(), it)
        }, this)
        binding.rvListTabSong.adapter = musicAdapter
        musicViewModel.getSongs().observe(requireActivity(), Observer { responseBody ->
            mActivity.runOnUiThread {
                responseBody?.let {
                    musicAdapter?.sumblist(it)
                    requireActivity().resetQueueItems(it) {
                    }
                }
            }
        })
    }

    override fun playNext(song: MusicLocal) {
        mActivity.addSongPlayNext(listOf(song)) {
        }
    }

    override fun addPlayList(song: MusicLocal) {
        _mActivity.addSongsToPlaylist(listOf(song)) {
        }
    }

    override fun setRingTone(song: MusicLocal) {
        if (RingtoneManager.requiresDialog(mContext)) {
            RingtoneManager.showDialog(mContext)
        } else {
            if (VersionUtils.hasQ()) {
                RingtoneManager.setRingtone(mContext, song)
            } else {
                if (RingtoneManager.setAsRingtoneAndroid(mContext, File(song.path))) {
                    val message = mContext
                        .getString(R.string.x_has_been_set_as_ringtone, song.title)
                    mContext.toast(message)
                } else {
                    val message = mContext
                        .getString(R.string.x_fail_has_been_set_as_ringtone, song.title)
                    mContext.toast(message)
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
        } else requireActivity().deleteFromMediaStore(song.path) {
            if (it) {
                App.getDB().songsDao().removeSong(mediaID)
                requireContext().toast(getString(R.string.deleted_x_songs, song.title))
                mActivity.updateAllDatabases {
                    musicViewModel.forceReload()
                }
            }
        }
    }


    override fun setFavourite(song: MusicLocal) {
    }

    override fun share(song: MusicLocal) {
        mContext.startActivity(
            Intent.createChooser(
                MusicUtil.createShareSongMultiFileIntent(listOf(song), mContext),
                null
            )
        )
    }

    private fun deleteSongFromExternalStorage(songUri: Uri, mediaStoreId: Long) {
        try {
            mContext.contentResolver.delete(songUri, null, null)
        } catch (e: Exception) {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val uris = arrayListOf<Uri>()
            val newUri = ContentUris.withAppendedId(uri, mediaStoreId)
            uris.add(newUri)
            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(
                        mContext.contentResolver,
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