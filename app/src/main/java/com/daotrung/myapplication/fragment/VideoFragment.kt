package com.daotrung.myapplication.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.daotrung.myapplication.R
import com.daotrung.myapplication.activity.EnterPinActivity
import com.daotrung.myapplication.activity.PrivateVideoActivity
import com.daotrung.myapplication.activity.SearchVideoActivity
import com.daotrung.myapplication.activity.SetPinActivity
import com.daotrung.myapplication.adapter.VideoViewPagerAdapter
import com.daotrung.myapplication.adapter.ViewPagerAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.skydoves.balloon.*
import com.daotrung.myapplication.databinding.FragmentVideoBinding
import com.daotrung.myapplication.util.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class VideoFragment : BaseFragment() {
    private lateinit var binding: FragmentVideoBinding
    private lateinit var balloon: Balloon
    lateinit var fragment: Fragment

    private var tabTitle = arrayOf("Video", "Folder", "Recent")


    companion object {
        @JvmStatic
        fun newInstance(): VideoFragment {
            val args = Bundle()
            val fragment = VideoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = Color.rgb(24, 20, 40)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.icSearchMainVideo.setOnClickListener {
            startActivity(Intent(requireContext(), SearchVideoActivity::class.java))
        }
        binding.icPrivateLock.setOnClickListener {
            val sharedPref =
                requireContext().getSharedPreferences(PREFS_NAME_PIN, Context.MODE_PRIVATE)
            val passCheck = sharedPref.getString(KEY_PASS_CODE, null)
            if (passCheck == null) {
                startActivity(Intent(requireContext(), SetPinActivity::class.java))
            } else {
                startActivity(Intent(requireContext(), EnterPinActivity::class.java))
            }
        }
    }

    private fun initView() {
        setAdapterTabLayout()
        popUpCreate()

    }

    private fun setAdapterTabLayout() {
        val adapter = ViewPagerAdapter(_mActivity)
        val videoFragment = VideoFragmentVideo()
        adapter.addFragment(videoFragment, "Video")
        val folderFragment = VideoFragmentFolder()
        adapter.addFragment(folderFragment, "Folder")
        val recentFragment = VideoFragmentRecent()
        adapter.addFragment(recentFragment, "Recent")

        binding.viewPager2Video.apply {
            offscreenPageLimit = adapter.itemCount
            isUserInputEnabled = false
            this.adapter = adapter
        }
        TabLayoutMediator(binding.tablayout, binding.viewPager2Video) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()

        binding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager2Video.currentItem = tab?.position!!
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun popUpCreate() {

        balloon = Balloon.Builder(mContext)
            .setLayout(R.layout.custom_popup_menu_video_main)
            .setWidthRatio(0.5f)
            .setHeight(BalloonSizeSpec.WRAP)
            .setIsVisibleArrow(false)
            .setPadding(12)
            .setCornerRadius(15f)
            .setBackgroundColorResource(R.color.white)
            .setBalloonAnimation(BalloonAnimation.CIRCULAR)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        val btnEdit: LinearLayout = balloon.getContentView().findViewById(R.id.edit_menu_video)
        val btnEqualizer: LinearLayout =
            balloon.getContentView().findViewById(R.id.equalizer_menu_video)

        binding.icMore.setOnClickListener {
            balloon.showAlignBottom(binding.icMore)
        }
        btnEdit.setOnClickListener {
            Toast.makeText(mContext, "Click Edit", Toast.LENGTH_SHORT).show()
            balloon.dismiss()
        }
        btnEqualizer.setOnClickListener {
            Toast.makeText(mContext, "Click Equalizer", Toast.LENGTH_SHORT).show()
            balloon.dismiss()
        }

    }
}