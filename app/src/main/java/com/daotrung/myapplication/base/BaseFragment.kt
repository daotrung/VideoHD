package com.daotrung.myapplication.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.daotrung.myapplication.activity.MainActivity
import com.daotrung.myapplication.util.LogInstance
import me.yokeyword.fragmentation.SupportFragment

open class BaseFragment : SupportFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogInstance.e(this@BaseFragment::class.java.simpleName)
    }

    protected lateinit var mActivity: Activity
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        mActivity = activity
    }

    protected lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    fun onBack() {
        startActivity(Intent(mActivity, MainActivity::class.java))
        mActivity.finish()
    }

}