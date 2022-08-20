package com.daotrung.myapplication.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.databinding.ActivitySelectedVideoOrMusicBinding
import com.daotrung.myapplication.fragment.AllPlaylistFragment

class SelectedActivityVideoOrMusic : BaseActivity<ActivitySelectedVideoOrMusicBinding>() {

    private fun replaceFragment(fragment: Fragment) {

    }
    override fun binding(): ActivitySelectedVideoOrMusicBinding {
        return ActivitySelectedVideoOrMusicBinding.inflate(layoutInflater)
    }
    override fun initView() {

    }
}