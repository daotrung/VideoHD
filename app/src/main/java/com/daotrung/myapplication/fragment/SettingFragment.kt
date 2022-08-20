package com.daotrung.myapplication.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.daotrung.myapplication.R
import com.daotrung.myapplication.activity.MainActivity
import com.daotrung.myapplication.base.BaseFragment
import com.daotrung.myapplication.databinding.FragmentSettingBinding
import com.daotrung.myapplication.util.*
import com.daotrung.myapplication.viewmodel.VideoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingFragment : BaseFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): SettingFragment {
            val args = Bundle()
            val fragment = SettingFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: FragmentSettingBinding
    var checkShowLastPlayerVideo: Boolean = false
    var checkShowLastResumeStatusVideo: Boolean = false
    var checkRememberBrightness: Boolean = false
    var checkTimeVideo: String? = ""
    var checkFilterSizeVideo: String? = ""
    private lateinit var videoViewModel: VideoViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = Color.rgb(24, 20, 40)
        videoViewModel = VideoViewModel()
        initView()
        setOnClickListener()
    }

    private fun initView() {

        // check showlastplayvideo
        if (UseSharedPreferences.getBooleanPreferences(
                PREFS_SHOW_LAST_PLAY_VIDEO, requireContext(),
                KEY_SHOW_LAST_PVIDEO
            )
        ) {
            binding.imgShowlastPlayerVideo.setImageResource(R.drawable.ic_swbar_check_setting)
        } else {
            binding.imgShowlastPlayerVideo.setImageResource(R.drawable.ic_swbar_un_check_setting)
        }

        //check showLastStatusVideo
        if (UseSharedPreferences.getBooleanPreferences(
                PREFS_SHOW_RESUME_STATUS, requireContext(),
                KEY_SHOW_RESUME_STATUS
            )
        ) {
            binding.imgShowResumeStatusVideo.setImageResource(R.drawable.ic_swbar_check_setting)
        } else {
            binding.imgShowResumeStatusVideo.setImageResource(R.drawable.ic_swbar_un_check_setting)
        }

        //check lastBrightNessVideo
        if (UseSharedPreferences.getBooleanPreferences(
                PREF_BRIGHTNESS, requireContext(),
                KEY_BRIGHTNESS
            )
        ) {
            binding.imgRememberBrightNess.setImageResource(R.drawable.ic_swbar_check_setting)
        } else {
            binding.imgRememberBrightNess.setImageResource(R.drawable.ic_swbar_un_check_setting)
        }

        // check video size filter

    }

    private fun setOnClickListener() {

        // show last played Video
        binding.swbShowLastPlayedVideo.setOnClickListener {
            if (!checkShowLastPlayerVideo) {
                binding.imgShowlastPlayerVideo.setImageResource(R.drawable.ic_swbar_un_check_setting)

                UseSharedPreferences.putBooleanPreferences(
                    PREFS_SHOW_LAST_PLAY_VIDEO,
                    requireContext(),
                    KEY_SHOW_LAST_PVIDEO,
                    false
                )
                checkShowLastPlayerVideo = true

            } else {
                binding.imgShowlastPlayerVideo.setImageResource(R.drawable.ic_swbar_check_setting)
                UseSharedPreferences.putBooleanPreferences(
                    PREFS_SHOW_LAST_PLAY_VIDEO, requireContext(),
                    KEY_SHOW_LAST_PVIDEO, true
                )

                checkShowLastPlayerVideo = false
            }

        }

        // show resumeStatus
        binding.swbShowResumeStatus.setOnClickListener {
            if (!checkShowLastResumeStatusVideo) {
                binding.imgShowResumeStatusVideo.setImageResource(R.drawable.ic_swbar_un_check_setting)

                UseSharedPreferences.putBooleanPreferences(
                    PREFS_SHOW_RESUME_STATUS,
                    requireContext(),
                    KEY_SHOW_RESUME_STATUS,
                    false
                )
                checkShowLastResumeStatusVideo = true

            } else {
                binding.imgShowResumeStatusVideo.setImageResource(R.drawable.ic_swbar_check_setting)
                UseSharedPreferences.putBooleanPreferences(
                    PREFS_SHOW_RESUME_STATUS, requireContext(),
                    KEY_SHOW_RESUME_STATUS, true
                )

                checkShowLastResumeStatusVideo = false
            }
        }

        // setNextPrevTime
        binding.scollerListTimeTap.setOnClickListener {
            showAlertDialog()
        }
        // setBrightness
        // setBrightNessRemember
        binding.swbRememberBrightness.setOnClickListener {
            if (!checkRememberBrightness) {
                binding.imgRememberBrightNess.setImageResource(R.drawable.ic_swbar_un_check_setting)
                UseSharedPreferences.putBooleanPreferences(
                    PREF_BRIGHTNESS,
                    requireContext(),
                    KEY_BRIGHTNESS,
                    false
                )
                checkRememberBrightness = true

            } else {
                binding.imgRememberBrightNess.setImageResource(R.drawable.ic_swbar_check_setting)
                UseSharedPreferences.putBooleanPreferences(
                    PREF_BRIGHTNESS, requireContext(),
                    KEY_BRIGHTNESS, true
                )

                checkRememberBrightness = false
            }
        }

        // check video size Filter
        binding.scorllListVideoSizeFilter.setOnClickListener {
            showAlertDialogVideoSizeFilter()
        }

    }

    private fun showAlertDialogVideoSizeFilter() {
        var checkItem: BooleanArray
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        val items = arrayOf("none", "5M", "10M", "20M", "30M")

        checkFilterSizeVideo = UseSharedPreferences.getStringPreferences(
            PREF_SIZE_FILTER, requireContext(),
            KEY_SIZE_FILTER
        )
        when (checkFilterSizeVideo) {
            "none" -> {
                checkItem = booleanArrayOf(true, false, false, false, false)
            }
            "5M" -> {
                checkItem = booleanArrayOf(false, true, false, false, false)
            }
            "10M" -> {
                checkItem = booleanArrayOf(false, false, true, false, false)
            }
            "20M" -> {
                checkItem = booleanArrayOf(false, false, false, true, false)
            }
            "30M" -> {
                checkItem = booleanArrayOf(false, false, false, false, true)
            }
            else -> {
                checkItem = booleanArrayOf(true, false, false, false, false)
            }
        }

        materialAlertDialogBuilder.setMultiChoiceItems(
            items,
            checkItem
        ) { dialog, which, isCheked ->
            checkItem[which] = isCheked

            when (items[which]) {

                "none" -> {
                    UseSharedPreferences.putStringPreferences(
                        PREF_SIZE_FILTER, requireContext(),
                        KEY_SIZE_FILTER, "none"
                    )
                    checkItem = booleanArrayOf(true, false, false, false, false)
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "All video is display", Toast.LENGTH_SHORT)
                        .show()
                    videoViewModel.forceReload(requireContext())
                }
                "5M" -> {
                    UseSharedPreferences.putStringPreferences(
                        PREF_SIZE_FILTER, requireContext(),
                        KEY_SIZE_FILTER, "5M"
                    )
                    checkItem = booleanArrayOf(false, true, false, false, false)
                    dialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "All video with size 5M will filter",
                        Toast.LENGTH_SHORT
                    ).show()
                    videoViewModel.forceReload(requireContext())
                }
                "10M" -> {
                    UseSharedPreferences.putStringPreferences(
                        PREF_SIZE_FILTER, requireContext(),
                        KEY_SIZE_FILTER, "10M"
                    )
                    checkItem = booleanArrayOf(false, false, true, false, false)
                    dialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "All video with size 10M will filter",
                        Toast.LENGTH_SHORT
                    ).show()
                    videoViewModel.forceReload(requireContext())
                }
                "20M" -> {
                    UseSharedPreferences.putStringPreferences(
                        PREF_SIZE_FILTER, requireContext(),
                        KEY_SIZE_FILTER, "20M"
                    )
                    checkItem = booleanArrayOf(false, false, false, true, false)
                    dialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "All video with size 20M will filter",
                        Toast.LENGTH_SHORT
                    ).show()
                    videoViewModel.forceReload(requireContext())
                }
                "30M" -> {
                    UseSharedPreferences.putStringPreferences(
                        PREF_SIZE_FILTER, requireContext(),
                        KEY_SIZE_FILTER, "30M"
                    )
                    checkItem = booleanArrayOf(false, false, false, false, true)
                    dialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "All video with size 30M will filter",
                        Toast.LENGTH_SHORT
                    ).show()
                    videoViewModel.forceReload(requireContext())
                }
                else -> {
                    UseSharedPreferences.putStringPreferences(
                        PREFS_TIME_VIDEO, requireContext(),
                        KEY_TIME_VIDEO, "none"
                    )
                    checkItem = booleanArrayOf(true, false, false, false, false)
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "All video is display", Toast.LENGTH_SHORT)
                        .show()
                    videoViewModel.forceReload(requireContext())
                }

            }
        }
            .create()
        materialAlertDialogBuilder.show()

    }


    private fun showAlertDialog() {
        var checkItems = booleanArrayOf(false, false, false)
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        var items = arrayOf("5s", "10s", "30s")

        checkTimeVideo = UseSharedPreferences.getStringPreferences(
            PREFS_TIME_VIDEO, requireContext(),
            KEY_TIME_VIDEO
        )
        when (checkTimeVideo) {
            "" -> {
                checkItems = booleanArrayOf(false, false, false)
            }
            "5s" -> {
                checkItems = booleanArrayOf(true, false, false)
            }
            "10s" -> {
                checkItems = booleanArrayOf(false, true, false)
            }
            "30s" -> {
                checkItems = booleanArrayOf(false, false, true)
            }
        }

        materialAlertDialogBuilder.setMultiChoiceItems(
            items,
            checkItems
        ) { dialog, which, isCheked ->
            checkItems[which] = isCheked

            when (items[which]) {

                "5s" -> {
                    UseSharedPreferences.putStringPreferences(
                        PREFS_TIME_VIDEO, requireContext(),
                        KEY_TIME_VIDEO, "5s"
                    )
                    checkItems = booleanArrayOf(true, false, false)
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Set time to 5s", Toast.LENGTH_SHORT).show()
                }
                "10s" -> {
                    UseSharedPreferences.putStringPreferences(
                        PREFS_TIME_VIDEO, requireContext(),
                        KEY_TIME_VIDEO, "10s"
                    )
                    checkItems = booleanArrayOf(false, true, false)
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Set time to 10s", Toast.LENGTH_SHORT).show()
                }
                "30s" -> {
                    UseSharedPreferences.putStringPreferences(
                        PREFS_TIME_VIDEO, requireContext(),
                        KEY_TIME_VIDEO, "30s"
                    )
                    checkItems = booleanArrayOf(false, false, true)
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Set time to 30s", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    UseSharedPreferences.putStringPreferences(
                        PREFS_TIME_VIDEO, requireContext(),
                        KEY_TIME_VIDEO, "10s"
                    )
                    checkItems = booleanArrayOf(false, true, false)
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Set time to 10s", Toast.LENGTH_SHORT).show()
                }
            }
        }.create()
        materialAlertDialogBuilder.show()

    }


}