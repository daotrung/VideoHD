package com.daotrung.myapplication.dialog

import android.app.Activity
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App
import com.daotrung.myapplication.databinding.DialogSelectPlayistBinding
import com.daotrung.myapplication.model.PlayListEntity
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import kotlinx.android.synthetic.main.dialog_select_playist.view.*
import kotlinx.android.synthetic.main.select_playlist_item.view.*

class DialogSelectPlayList(val activity: Activity, val callBack: (playListId: Int) -> Unit) {
    var dialog: AlertDialog? = null
    private lateinit var binding: DialogSelectPlayistBinding

    init {
        binding = DialogSelectPlayistBinding.inflate(activity.layoutInflater)
        ensureBackgroundThread {
            val playList = App.getDB().playListDao().getAll() as ArrayList<PlayListEntity>
            activity.runOnUiThread {
                initDialog(playList, binding)
                if (playList.isEmpty()) {
                    {
                        showNewPlayListDialog()
                    }
                }
            }
        }
        binding.dialogSelectPlaylistNewRadio.setOnClickListener {
            binding.dialogSelectPlaylistNewRadio.isChecked = false
            showNewPlayListDialog()
        }
    }

    private fun initDialog(
        playList: ArrayList<PlayListEntity>,
        binding: DialogSelectPlayistBinding
    ) {
        playList?.forEach {
            activity.layoutInflater.inflate(R.layout.select_playlist_item, null).apply {
                this.select_playlist_item_radio_button.apply {
                    text = it.playListName
                    isChecked = false
                    id = it.playListId
                    setOnClickListener { view ->
                        callBack(it.playListId)
                        dialog?.dismiss()
                    }
                }

                this.dialog_select_playlist_linear.addView(
                    this,
                    RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        dialog = AlertDialog.Builder(activity)
            .create().apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun showNewPlayListDialog() {

    }
}