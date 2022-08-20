package com.daotrung.myapplication.util

import android.app.ProgressDialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.util.DisplayMetrics
import android.widget.Toast
import com.daotrung.myapplication.abs.App
import com.google.android.material.button.MaterialButton
import java.util.*

object AppUtils {
    fun milliHzToString(milliHz: Int): String {
        if (milliHz < 1000) return ""
        return if (milliHz < 1000000) "" + milliHz / 1000 + "Hz" else "" + milliHz / 1000000 + "kHz"
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun screenWidth(): Int {
        val displayMetrics: DisplayMetrics = App.context.resources.displayMetrics
        return displayMetrics.widthPixels
    }

    fun screenHeight(): Int {
        val displayMetrics: DisplayMetrics = App.context.resources.displayMetrics
        return displayMetrics.heightPixels
    }

    fun Int.getFormattedDuration(forceShowHours: Boolean = false): String {
        val sb = StringBuilder(8)
        val hours = this / 3600
        val minutes = this % 3600 / 60
        val seconds = this % 60

        if (this >= 3600) {
            sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
        } else if (forceShowHours) {
            sb.append("0:")
        }

        sb.append(String.format(Locale.getDefault(), "%02d", minutes))
        sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
        return sb.toString()
    }

    fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)

    private var mProgressDialog: ProgressDialog? = null
    fun Context.showLoading(message: String) {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog?.run {
            setMessage(message)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(false)
            show()
        }
    }

    fun hideLoading() {
        mProgressDialog?.dismiss()
    }
}


inline fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    for ((index, item) in this.withIndex()) {
        if (predicate(item))
            return index
    }
    return null
}