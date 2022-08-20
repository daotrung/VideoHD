package com.daotrung.myapplication.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.daotrung.myapplication.R
import com.daotrung.myapplication.activity.EditSongActivity
import com.daotrung.myapplication.activity.EqualizerMusicActivity
import com.daotrung.myapplication.activity.SearchVideoActivity
import com.daotrung.myapplication.activity.SongPlayingActivity
import com.daotrung.myapplication.adapter.ViewPagerAdapter
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentMusicBinding
import com.daotrung.myapplication.model.Events
import com.daotrung.myapplication.model.MusicLocal
import com.daotrung.myapplication.service.MusicService
import com.daotrung.myapplication.util.PLAYPAUSE
import com.daotrung.myapplication.util.sendIntent
import com.daotrung.myapplication.util.view.updatePlayPauseIcon
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.simplemobiletools.commons.extensions.*
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import kotlinx.android.synthetic.main.view_current_song.*
import me.yokeyword.fragmentation.SupportActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MusicFragment : BaseFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): MusicFragment {
            val args = Bundle()
            val fragment = MusicFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: FragmentMusicBinding
    private lateinit var balloon: Balloon
    private var bus: EventBus? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bus = EventBus.getDefault()
        bus!!.register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = Color.rgb(24, 20, 40)
        initView()
        setOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        updateColors()
        updateCurrentSong(MusicService.mCurrentSong)
        updateSongState(MusicService.getIsPlaying())
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun trackChangedEvent(event: Events.SongChanged) {
        updateCurrentSong(event.song)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun trackStateChanged(event: Events.SongStateChanged) {
        updateSongState(event.isPlaying)
    }

    private fun updateColors() {
//        binding.include.background = ColorDrawable(mContext.getProperBackgroundColor())
        binding.currentSongLabel.setTextColor(mContext.getProperTextColor())
        binding.currentSongPlayPause.setOnClickListener {
            mContext.sendIntent(PLAYPAUSE)
        }
    }

    private fun updateCurrentSong(song: MusicLocal?) {
        if (song == null) {
            binding.include.visibility = View.GONE
            binding.include.fadeOut()
            return
        } else {
            binding.include.fadeIn()
        }

        val artist =
            if (song.artist.trim().isNotEmpty() && song.artist != MediaStore.UNKNOWN_STRING) {
                " • ${song.artist}"
            } else {
                ""
            }

        binding.currentSongLabel.text = "${song.title}$artist"
        val cornerRadius = resources.getDimension(R.dimen.rounded_corner_radius_small).toInt()
        val currentTrackPlaceholder = resources.getColoredDrawableWithColor(
            R.drawable.ic_headset,
            mContext.getProperTextColor()
        )
        val options = RequestOptions()
            .error(currentTrackPlaceholder)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))

        Glide.with(this)
            .load(song.coverArt)
            .apply(options)
            .into(binding.currentSongImage)
    }

    private fun updateSongState(isPlaying: Boolean) {
        binding.currentSongPlayPause.updatePlayPauseIcon(isPlaying, mContext.getProperTextColor())
    }

    override fun onDestroy() {
        super.onDestroy()
        bus?.unregister(this)
    }

    private fun setOnClickListener() {
        binding.icSearchMainMusic.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    requireContext(), SearchVideoActivity::class.java
                ).apply {
                    putExtra("TYPE", 1)
                }
            )
        }

        binding.currentSongLabel.setOnClickListener {
            requireActivity().startActivity(
                Intent(requireContext(), SongPlayingActivity::class.java)
            )
        }
    }

    private fun initView() {
        setAdapterTabLayout()
        popUpCreate()
    }

    private var tabTitle = arrayOf("Songs", "Album", "Artist", "Folder")
    private fun setAdapterTabLayout() {
        binding.tabLayout.removeAllTabs()
        tabTitle.forEach {
            val tab = binding.tabLayout.newTab()
            tab.text = it
            binding.tabLayout.addTab(tab)
        }
        val adapter = ViewPagerAdapter(_mActivity)

        val musicSongFragment = MusicFragmentSong()
        adapter.addFragment(musicSongFragment, "Songs")

        val musicFragmentAlbum = MusicFragmentAlbum()
        adapter.addFragment(musicFragmentAlbum, "Album")

        val musicFragmentArtist = MusicFragmentArtist()
        adapter.addFragment(musicFragmentArtist, "Artist")

        val musicFragmentFolder = MusicFragmentFolder()
        adapter.addFragment(musicFragmentFolder, "Folder")

        binding.viewPager.apply {
            offscreenPageLimit = adapter.itemCount
            isUserInputEnabled = false
            this.adapter = adapter
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()
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

        binding.icMoreMusic.setOnClickListener {
            balloon.showAlignBottom(binding.icMoreMusic)
        }
        btnEdit.setOnClickListener {
            startActivity(Intent(mContext, EditSongActivity::class.java))
            balloon.dismiss()
        }
        btnEqualizer.setOnClickListener {
            startActivity(Intent(mContext, EqualizerMusicActivity::class.java))
            balloon.dismiss()
        }
    }

}