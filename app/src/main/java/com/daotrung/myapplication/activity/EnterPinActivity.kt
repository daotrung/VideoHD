package com.daotrung.myapplication.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.databinding.ActivityEnterPinBinding
import com.daotrung.myapplication.fragment.VideoFragmentVideo
import com.daotrung.myapplication.util.KEY_PASS_CODE
import com.daotrung.myapplication.util.PASS_CODE
import com.daotrung.myapplication.util.PREFS_NAME_PIN
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class EnterPinActivity : BaseActivity<ActivityEnterPinBinding>(), View.OnClickListener {

    var passCode: String = ""
    var num1: String = ""
    var num2: String = ""
    var num3: String = ""
    var num4: String = ""
    var checkDotPin = false

    lateinit var numberList: ArrayList<String>

    override fun binding(): ActivityEnterPinBinding {
        return ActivityEnterPinBinding.inflate(layoutInflater)
    }

    override fun initView() {
        checkDone()
        numberList = ArrayList()
        setOnClick()
    }

    private fun setOnClick() {
        binding.btnEnterNumberOne.setOnClickListener(this)
        binding.btnEnterNumberTwo.setOnClickListener(this)
        binding.btnEnterSetNumberThree.setOnClickListener(this)
        binding.btnEnterSetNumberFour.setOnClickListener(this)
        binding.btnEnterSetNumberFive.setOnClickListener(this)
        binding.btnEnterSetNumberSix.setOnClickListener(this)
        binding.btnEnterSetNumberSeven.setOnClickListener(this)
        binding.btnEnterSetNumberEight.setOnClickListener(this)
        binding.btnEnterSetNumberNine.setOnClickListener(this)
        binding.btnEnterNumberZero.setOnClickListener(this)
        binding.btnEnterBack.setOnClickListener(this)
        binding.closeEnterPin.setOnClickListener {
            finish()
            startActivity(Intent(this,MainActivity::class.java))
        }

        binding.txtForgetPassCode.setOnClickListener {
            startActivity(Intent(this,SecurityQuestionActivity::class.java))
        }

        binding.btnOkEnter.setOnClickListener {
            val sharedPref = this.getSharedPreferences(PREFS_NAME_PIN, Context.MODE_PRIVATE)
            val passCheck = sharedPref.getString(KEY_PASS_CODE, null)
            if (passCheck == passCode) {
                startActivity(Intent(this,PrivateVideoActivity::class.java))
            } else {
                val dialogIncorrect = MaterialAlertDialogBuilder(this)
                    .setTitle("Incorrect Passcode")
                    .setMessage(
                        "The passcode you entered is incorrect.\n" +
                                "Please try again."
                    )
                    .setNegativeButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialogIncorrect.show()
            }
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnEnterNumberOne.id -> {
                numberList.add("1")
                passNumber(numberList)
            }
            binding.btnEnterNumberTwo.id -> {
                numberList.add("2")
                passNumber(numberList)
            }
            binding.btnEnterSetNumberThree.id -> {
                numberList.add("3")
                passNumber(numberList)
            }
            binding.btnEnterSetNumberFour.id -> {
                numberList.add("4")
                passNumber(numberList)
            }
            binding.btnEnterSetNumberFive.id -> {
                numberList.add("5")
                passNumber(numberList)
            }
            binding.btnEnterSetNumberSix.id -> {
                numberList.add("6")
                passNumber(numberList)
            }
            binding.btnEnterSetNumberSeven.id -> {
                numberList.add("7")
                passNumber(numberList)
            }
            binding.btnEnterSetNumberEight.id -> {
                numberList.add("8")
                passNumber(numberList)

            }
            binding.btnEnterSetNumberNine.id -> {
                numberList.add("9")
                passNumber(numberList)

            }
            binding.btnEnterNumberZero.id -> {
                numberList.add("0")
                passNumber(numberList)

            }
            binding.btnEnterBack.id -> {
                numberList.clear()
                checkDone()
                passNumber((numberList))

            }
        }
    }

    private fun checkDone() {
        if (checkDotPin) {
            binding.btnOkEnter.setImageResource(R.drawable.ic_check_pin)
            binding.btnOkEnter.isEnabled = true
            checkDotPin = false
        } else {
            binding.btnOkEnter.setImageResource(R.drawable.ic_un_check_pin)
            binding.btnOkEnter.isEnabled = false
            checkDotPin = true
        }
    }

    private fun passNumber(numberList: ArrayList<String>) {
        if (numberList.size == 0) {
            binding.pinDot1Enter.setBackgroundResource(R.drawable.bg_view_gray_oval)
            binding.pinDot2Enter.setBackgroundResource(R.drawable.bg_view_gray_oval)
            binding.pinDot3Enter.setBackgroundResource(R.drawable.bg_view_gray_oval)
            binding.pinDot4Enter.setBackgroundResource(R.drawable.bg_view_gray_oval)
            checkDotPin = false
            checkDone()
        } else {
            when (numberList.size) {
                1 -> {
                    num1 = numberList[0]
                    binding.pinDot1Enter.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2Enter.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    binding.pinDot3Enter.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    binding.pinDot4Enter.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    checkDotPin = false
                    checkDone()
                }
                2 -> {
                    num2 = numberList[1]
                    binding.pinDot1Enter.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2Enter.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot3Enter.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    binding.pinDot4Enter.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    checkDotPin = false
                    checkDone()
                }
                3 -> {
                    num3 = numberList[2]
                    binding.pinDot3Enter.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot1Enter.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2Enter.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot4Enter.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    checkDotPin = false
                    checkDone()
                }
                4 -> {
                    num4 = numberList[3]
                    binding.pinDot1Enter.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2Enter.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot3Enter.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot4Enter.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    passCode = num1 + num2 + num3 + num4
                    checkDotPin = true
                    checkDone()
                }
            }
        }
    }


}