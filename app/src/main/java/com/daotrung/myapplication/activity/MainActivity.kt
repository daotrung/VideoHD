package com.daotrung.myapplication.activity

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.databinding.ActivityMainBinding
import com.daotrung.myapplication.fragment.MusicFragment
import com.daotrung.myapplication.fragment.AllPlaylistFragment
import com.daotrung.myapplication.fragment.SettingFragment
import com.daotrung.myapplication.fragment.VideoFragment
import com.daotrung.myapplication.util.KEY_SIZE_FILTER
import com.daotrung.myapplication.util.PREF_SIZE_FILTER
import com.daotrung.myapplication.util.UseSharedPreferences
import com.daotrung.myapplication.util.updateAllDatabases
import com.daotrung.myapplication.util.view.ProgressHUD
import com.simplemobiletools.commons.extensions.toast

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var mProgressHUD: ProgressHUD? = null
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentMain.id, fragment)
            commitAllowingStateLoss()
        }
    }

    override fun binding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }


    override fun initView() {
        binding.navView.itemIconTintList = null
        mProgressHUD = ProgressHUD.show(this)
        if (hasPermissions()) {
            updateAllDatabases {
                replaceFragment(VideoFragment.newInstance())
                binding.navView.setOnNavigationItemSelectedListener {
                    when (it.itemId) {
                        R.id.navigation_video -> replaceFragment(VideoFragment.newInstance())
                        R.id.navigation_music -> replaceFragment(MusicFragment.newInstance())
                        R.id.navigation_playlist -> replaceFragment(AllPlaylistFragment.newInstance())
                        R.id.navigation_setting -> replaceFragment(SettingFragment.newInstance())
                    }
                    true
                }
                Handler(Looper.getMainLooper()).post {
                    mProgressHUD?.dismiss()
                }
            }
        } else {
            toast(R.string.no_storage_permissions)
            finish()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mProgressHUD != null && mProgressHUD!!.isShowing) {
            mProgressHUD?.dismiss()
        }
    }


}