package com.daotrung.myapplication.activity

import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.widget.LinearLayout
import android.widget.SeekBar
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.App.Companion.mmkv
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.databinding.ActivityEqualizerMusicBinding
import com.daotrung.myapplication.service.MusicService
import com.daotrung.myapplication.util.*
import com.daotrung.myapplication.util.view.CircularSeekBar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.models.RadioItem
import kotlinx.android.synthetic.main.item_seekbar.view.*
import kotlin.math.roundToInt

class EqualizerMusicActivity : BaseActivity<ActivityEqualizerMusicBinding>() {
    override fun binding(): ActivityEqualizerMusicBinding {
        return ActivityEqualizerMusicBinding.inflate(layoutInflater)
    }

    private var eqBands = HashMap<Short, Int>()
    private var listSeekBars = ArrayList<SeekBar>()
    override fun initView() {
        window.statusBarColor = Color.rgb(24, 20, 40)
        initMediaPlayer()
        binding.imvBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initMediaPlayer() {
        val mPlayer = MusicService.mPlayer ?: MediaPlayer()
        val equalizer = MusicService.mEqualizer ?: Equalizer(0, mPlayer.audioSessionId)
        try {
            if (!equalizer.enabled) {
                equalizer.enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val bassBoost = MusicService.mBassBoost ?: BassBoost(0, mPlayer.audioSessionId)
        try {
            bassBoost.enabled = mmkv.getBoolean(CHECK_BASS, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val virtualizer = MusicService.mVisualizer ?: Virtualizer(0, mPlayer.audioSessionId)
        try {
            virtualizer.enabled = mmkv.getBoolean(CHECK_TREBLE, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setupEqBands(equalizer)
        setupPresets(equalizer)
        setupBass(bassBoost)
        setupTreble(virtualizer)
        mmkv.getBoolean(CHECK_EQUALIZER, true).apply {
            binding.btnSeekbar.isChecked = this
            listSeekBars.forEach {
                it.isEnabled = this
            }
        }
        mmkv.getBoolean(CHECK_BASS, true).apply {
            binding.btnBass.isChecked = this
            binding.seekbarBass.isTouchEnabled = this
            if (this) {
                binding.seekbarBass.circleProgressColor = Color.parseColor("#FFDD5789")
            } else binding.seekbarBass.circleProgressColor = Color.parseColor("#353945")
        }
        mmkv.getBoolean(CHECK_TREBLE, true).apply {
            binding.btnTreble.isChecked = this
            binding.seekbarTreble.isTouchEnabled = this
            if (this) {
                binding.seekbarTreble.circleProgressColor = Color.parseColor("#FFDD5789")
            } else binding.seekbarTreble.circleProgressColor = Color.parseColor("#353945")
        }

        binding.btnSeekbar.setOnCheckedChangeListener { _, b ->
            mmkv.putBoolean(CHECK_EQUALIZER, b)
            listSeekBars.forEach {
                it.isEnabled = b
            }
        }
        binding.btnBass.setOnCheckedChangeListener { _, b ->
            binding.seekbarBass.isTouchEnabled = b
            mmkv.putBoolean(CHECK_BASS, b)
            if (b) {
                binding.seekbarBass.circleProgressColor = Color.parseColor("#FFDD5789")
            } else binding.seekbarBass.circleProgressColor = Color.parseColor("#353945")
        }
        binding.btnTreble.setOnCheckedChangeListener { _, b ->
            binding.seekbarTreble.isTouchEnabled = b
            mmkv.putBoolean(CHECK_TREBLE, b)
            if (b) {
                binding.seekbarTreble.circleProgressColor = Color.parseColor("#FFDD5789")
            } else binding.seekbarTreble.circleProgressColor = Color.parseColor("#353945")
        }
    }


    private fun setupEqBands(equalizer: Equalizer) {
        val minEq = equalizer.bandLevelRange[0]
        val maxEq = equalizer.bandLevelRange[1]
        listSeekBars.clear()
        binding.linearSeekbar.removeAllViews()
        val bandType = object : TypeToken<HashMap<Short, Int>>() {}.type
        eqBands = Gson().fromJson<HashMap<Short, Int>>(config.equalizerBands, bandType) ?: HashMap()
        for (band in 0 until equalizer.numberOfBands) {
            val freq = equalizer.getCenterFreq(band.toShort())
            val tvHz = MusicUtil.milliHzToString(freq)
            layoutInflater.inflate(R.layout.item_seekbar, binding.linearSeekbar, false).apply {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                params.weight = 1.0f
                this.constraint.layoutParams = params
                binding.linearSeekbar.addView(this)
                listSeekBars.add(this.seekBar)
                this.tvHz.text = tvHz
                this.seekBar.max = maxEq - minEq
                this.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        if (p2) {
                            val newValue = (p1 / 100.0).roundToInt() * 100
                            this@apply.seekBar.progress = newValue
                            val newProgress = newValue + minEq
                            try {
                                if ((MusicService.mEqualizer
                                        ?: equalizer).getBandLevel(band.toShort()) != newProgress.toShort()
                                ) {
                                    (MusicService.mEqualizer ?: equalizer).setBandLevel(
                                        band.toShort(),
                                        newProgress.toShort()
                                    )
                                    eqBands[band.toShort()] = newProgress
                                }
                            } catch (e: Exception) {

                            }
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                        seekBarStarted(equalizer)
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                        eqBands[band.toShort()] = this@apply.seekBar.progress
                        config.equalizerBands = Gson().toJson(eqBands)
                    }

                })
            }
        }
    }

    private fun seekBarStarted(equalizer: Equalizer) {
        binding.tvCategory.text = getString(R.string.custom)
        config.equalizerPreset = EQUALIZER_PRESET_CUSTOM
        for (band in 0 until equalizer.numberOfBands) {
            eqBands[band.toShort()] = listSeekBars[band].progress
        }
    }

    private fun setupPresets(equalizer: Equalizer) {
        try {
            presetChanged(config.equalizerPreset, equalizer)
        } catch (e: Exception) {
            showErrorToast(e)
            config.equalizerPreset = EQUALIZER_PRESET_CUSTOM
        }
        binding.linearCategory.setOnClickListener {
            val items = arrayListOf<RadioItem>()
            (0 until equalizer.numberOfPresets).mapTo(items) {
                RadioItem(it, equalizer.getPresetName(it.toShort()))
            }

            items.add(RadioItem(EQUALIZER_PRESET_CUSTOM, getString(R.string.custom)))
            RadioGroupDialog(this, items, config.equalizerPreset) { presetId ->
                try {
                    config.equalizerPreset = presetId as Int
                    presetChanged(presetId, equalizer)
                } catch (e: Exception) {
                    showErrorToast(e)
                    config.equalizerPreset = EQUALIZER_PRESET_CUSTOM
                }
            }
        }
    }

    private fun presetChanged(presetId: Int, equalizer: Equalizer) {
        if (presetId == EQUALIZER_PRESET_CUSTOM) {
            binding.tvCategory.text = getString(R.string.custom)

            for (band in 0 until equalizer.numberOfBands) {
                val minValue = equalizer.bandLevelRange[0]
                val progress = if (eqBands.containsKey(band.toShort())) {
                    eqBands[band.toShort()]
                } else {
                    val maxValue = equalizer.bandLevelRange[1]
                    (maxValue - minValue) / 2
                }

                listSeekBars[band].progress = progress!!.toInt()
                val newValue = progress + minValue
                (MusicService.mEqualizer ?: equalizer).setBandLevel(
                    band.toShort(),
                    newValue.toShort()
                )
            }
        } else {
            val presetName =
                (MusicService.mEqualizer ?: equalizer).getPresetName(presetId.toShort())
            if (presetName.isEmpty()) {
                config.equalizerPreset = EQUALIZER_PRESET_CUSTOM
                binding.tvCategory.text = getString(R.string.custom)
            } else {
                binding.tvCategory.text = presetName
            }

            (MusicService.mEqualizer ?: equalizer).usePreset(presetId.toShort())

            val lowestBandLevel = (MusicService.mEqualizer ?: equalizer).bandLevelRange?.get(0)
            for (band in 0 until (MusicService.mEqualizer ?: equalizer).numberOfBands) {
                val level = (MusicService.mEqualizer ?: equalizer).getBandLevel(band.toShort())
                    .minus(lowestBandLevel!!)
                listSeekBars[band].progress = level
            }
        }
    }

    private fun setupBass(bass: BassBoost) {
        var x = 0
        if (bass != null) {
            try {
                x = bass.roundedStrength.toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (x == 0) {
                binding.seekbarBass.progress = 1
            } else binding.seekbarBass.progress = x
        } else {
            x = config.bassStrength
            if (x == 0) {
                binding.seekbarBass.progress = 1
            } else binding.seekbarBass.progress = x
        }
        (MusicService.mBassBoost ?: bass).setStrength(x.toShort())
        binding.seekbarBass.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                try {
                    (MusicService.mBassBoost ?: bass).setStrength(progress.toShort())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
                config.bassStrength = seekBar?.progress!!
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
            }

        })
    }

    private fun setupTreble(virtualizer: Virtualizer) {
        var x = 0
        if (virtualizer != null) {
            try {
                x = virtualizer.roundedStrength.toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (x == 0) {
                binding.seekbarTreble.progress = 1
            } else binding.seekbarTreble.progress = x
        } else {
            x = config.virtualizerStrength
            if (x == 0) {
                binding.seekbarTreble.progress = 1
            } else binding.seekbarTreble.progress = x
        }
        (MusicService.mVisualizer ?: virtualizer).setStrength(x.toShort())
        binding.seekbarTreble.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                try {
                    (MusicService.mVisualizer ?: virtualizer).setStrength(progress.toShort())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
                config.virtualizerStrength = seekBar?.progress!!
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
            }

        })
    }

}