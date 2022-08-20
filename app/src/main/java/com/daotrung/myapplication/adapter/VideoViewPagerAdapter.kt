package com.daotrung.myapplication.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.daotrung.myapplication.fragment.VideoFragmentFolder
import com.daotrung.myapplication.fragment.VideoFragmentRecent
import com.daotrung.myapplication.fragment.VideoFragmentVideo

class VideoViewPagerAdapter(fragmentActivity: FragmentActivity, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentActivity.supportFragmentManager, fragmentActivity.lifecycle) {
    val videoFragment = VideoFragmentVideo()
    val folderFragment = VideoFragmentFolder()
    val recentFragment = VideoFragmentRecent()
    val listFragment = arrayListOf<Fragment>(videoFragment, folderFragment, recentFragment)
    var positionL = 0
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        positionL = position
        return when (position) {
            0 -> videoFragment
            1 -> folderFragment
            2 -> recentFragment
            else -> videoFragment
        }
    }

    fun getFragment(position: Int): Fragment {
        return listFragment[position]
    }

    fun reloadFragment() {
        when (positionL) {
            0 -> {
                Log.d("TAG", "reloadFragment2: ")
            }
        }
    }
}