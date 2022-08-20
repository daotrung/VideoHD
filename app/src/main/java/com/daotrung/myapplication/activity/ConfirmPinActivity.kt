package com.daotrung.myapplication.activity

import android.content.Intent
import android.view.View
import android.widget.Toast
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.databinding.ActivityConfirmPinBinding
import com.daotrung.myapplication.util.*
import com.daotrung.myapplication.util.UseSharedPreferences

class ConfirmPinActivity : BaseActivity<ActivityConfirmPinBinding>(), View.OnClickListener {

    var passCode: String = ""
    var num1: String = ""
    var num2: String = ""
    var num3: String = ""
    var num4: String = ""
    var checkDotPin = false

    lateinit var numberList: ArrayList<String>

    override fun binding(): ActivityConfirmPinBinding {
        return ActivityConfirmPinBinding.inflate(layoutInflater)
    }

    override fun initView() {
        checkDone()
        numberList = ArrayList()
        setOnClick()
    }

    private fun setOnClick() {
        binding.btnConfNumberOne.setOnClickListener(this)
        binding.btnConfNumberTwo.setOnClickListener(this)
        binding.btnConfSetNumberThree.setOnClickListener(this)
        binding.btnConfSetNumberFour.setOnClickListener(this)
        binding.btnConfSetNumberFive.setOnClickListener(this)
        binding.btnConfSetNumberSix.setOnClickListener(this)
        binding.btnConfSetNumberSeven.setOnClickListener(this)
        binding.btnConfSetNumberEight.setOnClickListener(this)
        binding.btnConfSetNumberNine.setOnClickListener(this)
        binding.btnConfNumberZero.setOnClickListener(this)
        binding.btnConfBack.setOnClickListener(this)
        binding.closeConfirmPin.setOnClickListener {
            finish()
        }
        binding.btnOkConf.setOnClickListener {
            var passCodeCheck = intent.getStringExtra(PASS_CODE)

            if (passCode == passCodeCheck.toString()) {
                Toast.makeText(this, "Set Pass Successful !!", Toast.LENGTH_SHORT).show()

                val questionCheck = UseSharedPreferences.getStringPreferences(PREFS_QUESTION_PIN,this,
                    SET_QUESTION_PIN)
                // put String
                UseSharedPreferences.putStringPreferences(PREFS_NAME_PIN,this,KEY_PASS_CODE,passCode)
                if(questionCheck==null){
                    startActivity(Intent(this, SecurityQuestionActivity::class.java))

                }else{
                    startActivity(Intent(this,EnterPinActivity::class.java))
                }

            } else {
                Toast.makeText(this, "Wrong Pass Match !! Please Check Again", Toast.LENGTH_SHORT)
                    .show()
                numberList.clear()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnConfNumberOne.id -> {
                numberList.add("1")
                passNumber(numberList)
            }
            binding.btnConfNumberTwo.id -> {
                numberList.add("2")
                passNumber(numberList)
            }
            binding.btnConfSetNumberThree.id -> {
                numberList.add("3")
                passNumber(numberList)
            }
            binding.btnConfSetNumberFour.id -> {
                numberList.add("4")
                passNumber(numberList)
            }
            binding.btnConfSetNumberFive.id -> {
                numberList.add("5")
                passNumber(numberList)
            }
            binding.btnConfSetNumberSix.id -> {
                numberList.add("6")
                passNumber(numberList)
            }
            binding.btnConfSetNumberSeven.id -> {
                numberList.add("7")
                passNumber(numberList)
            }
            binding.btnConfSetNumberEight.id -> {
                numberList.add("8")
                passNumber(numberList)
            }
            binding.btnConfSetNumberNine.id -> {
                numberList.add("9")
                passNumber(numberList)
            }
            binding.btnConfNumberZero.id -> {
                numberList.add("0")
                passNumber(numberList)
            }
            binding.btnConfBack.id -> {
                numberList.clear()
                checkDone()
                passNumber((numberList))
            }
        }
    }

    private fun checkDone() {
        if (checkDotPin) {
            binding.btnOkConf.setImageResource(R.drawable.ic_check_pin)
            binding.btnOkConf.isEnabled = true
            checkDotPin = false
        } else {
            binding.btnOkConf.setImageResource(R.drawable.ic_un_check_pin)
            binding.btnOkConf.isEnabled = false
            checkDotPin = true
        }
    }

    private fun passNumber(numberList: ArrayList<String>) {
        if (numberList.size == 0) {
            binding.pinDot1Conf.setBackgroundResource(R.drawable.bg_view_gray_oval)
            binding.pinDot2Conf.setBackgroundResource(R.drawable.bg_view_gray_oval)
            binding.pinDot3Conf.setBackgroundResource(R.drawable.bg_view_gray_oval)
            binding.pinDot4Conf.setBackgroundResource(R.drawable.bg_view_gray_oval)
            checkDotPin = false
            checkDone()
        } else {
            when (numberList.size) {
                1 -> {
                    num1 = numberList[0]
                    binding.pinDot1Conf.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2Conf.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    binding.pinDot3Conf.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    binding.pinDot4Conf.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    checkDotPin = false
                    checkDone()
                }
                2 -> {
                    num2 = numberList[1]
                    binding.pinDot1Conf.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2Conf.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot3Conf.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    binding.pinDot4Conf.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    checkDotPin = false
                    checkDone()
                }
                3 -> {
                    num3 = numberList[2]
                    binding.pinDot1Conf.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2Conf.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot3Conf.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot4Conf.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    checkDotPin = false
                    checkDone()
                }
                4 -> {
                    num4 = numberList[3]
                    binding.pinDot1Conf.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2Conf.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot3Conf.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot4Conf.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    passCode = num1 + num2 + num3 + num4
                    checkDotPin = true
                    checkDone()
                }
            }
        }
    }


}