package com.daotrung.myapplication.activity

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import com.daotrung.myapplication.R
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.databinding.ActivitySetPinBinding
import com.daotrung.myapplication.util.LogInstance
import com.daotrung.myapplication.util.PASS_CODE
import com.daotrung.myapplication.util.POSITION_FOLDER_VIDEO

class SetPinActivity : BaseActivity<ActivitySetPinBinding>() , View.OnClickListener{

    var passCode :String = ""
    var num1 :String = ""
    var num2 :String = ""
    var num3 :String = ""
    var num4 :String = ""
    var checkDotPin = false

    lateinit var numberList:ArrayList<String>
    override fun binding(): ActivitySetPinBinding {
        return ActivitySetPinBinding.inflate(layoutInflater)
    }

    override fun initView() {
        checkDone()
        numberList = ArrayList()
        setOnClick()
    }

    private fun setOnClick() {
        binding.btnSetNumberOne.setOnClickListener(this)
        binding.btnSetNumberTwo.setOnClickListener(this)
        binding.btnSetNumberThree.setOnClickListener(this)
        binding.btnSetNumberFour.setOnClickListener(this)
        binding.btnSetNumberFive.setOnClickListener(this)
        binding.btnSetNumberSix.setOnClickListener(this)
        binding.btnSetNumberSeven.setOnClickListener(this)
        binding.btnSetNumberEight.setOnClickListener(this)
        binding.btnSetNumberNine.setOnClickListener(this)
        binding.btnSetNumberZero.setOnClickListener(this)
        binding.btnSetBack.setOnClickListener(this)
        binding.closeSetPin.setOnClickListener {
            onBackPressed()
        }
        binding.btnOk.setOnClickListener {
            val intent = Intent(this,ConfirmPinActivity::class.java)
              intent.putExtra(PASS_CODE,passCode)
              Log.e("pass",passCode)
              startActivity(intent)
        }

    }

    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnSetNumberOne.id -> {
                    numberList.add("1")
                    passNumber(numberList)
            }
            binding.btnSetNumberTwo.id -> {
                    numberList.add("2")
                    passNumber(numberList)
            }
            binding.btnSetNumberThree.id -> {
                    numberList.add("3")
                    passNumber(numberList)
            }
            binding.btnSetNumberFour.id -> {
                    numberList.add("4")
                    passNumber(numberList)
            }
            binding.btnSetNumberFive.id -> {
                    numberList.add("5")
                    passNumber(numberList)
            }
            binding.btnSetNumberSix.id -> {
                    numberList.add("6")
                    passNumber(numberList)
            }
            binding.btnSetNumberSeven.id -> {
                    numberList.add("7")
                    passNumber(numberList)
            }
            binding.btnSetNumberEight.id -> {
                    numberList.add("8")
                    passNumber(numberList)

            }
            binding.btnSetNumberNine.id -> {
                    numberList.add("9")
                    passNumber(numberList)

            }
            binding.btnSetNumberZero.id -> {
                    numberList.add("0")
                    passNumber(numberList)

            }
            binding.btnSetBack.id -> {
                    numberList.clear()
                    checkDone()
                    passNumber((numberList))

            }
        }
    }

    private fun checkDone(){
        if(checkDotPin) {
            binding.btnOk.setImageResource(R.drawable.ic_check_pin)
            binding.btnOk.isEnabled = true
            checkDotPin = false
        }else{
            binding.btnOk.setImageResource(R.drawable.ic_un_check_pin)
            binding.btnOk.isEnabled = false
            checkDotPin = true
        }
    }

    private fun passNumber(numberList: ArrayList<String>) {
        if(numberList.size == 0){
            binding.pinDot1.setBackgroundResource(R.drawable.bg_view_gray_oval)
            binding.pinDot2.setBackgroundResource(R.drawable.bg_view_gray_oval)
            binding.pinDot3.setBackgroundResource(R.drawable.bg_view_gray_oval)
            binding.pinDot4.setBackgroundResource(R.drawable.bg_view_gray_oval)
            checkDotPin = false
            checkDone()
        }else {
            when (numberList.size) {
                1 -> {
                    num1 = numberList[0]
                    binding.pinDot1.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    binding.pinDot3.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    binding.pinDot4.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    checkDotPin = false
                    checkDone()
                }
                2 -> {
                    num2 = numberList[1]
                    binding.pinDot2.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot1.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot3.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    binding.pinDot4.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    checkDotPin = false
                    checkDone()
                }
                3 -> {
                    num3 = numberList[2]
                    binding.pinDot3.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot1.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot4.setBackgroundResource(R.drawable.bg_view_gray_oval)
                    checkDotPin = false
                    checkDone()
                }
                4 -> {
                    num4 = numberList[3]
                    binding.pinDot1.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot2.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot3.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    binding.pinDot4.setBackgroundResource(R.drawable.bg_view_blue_oval)
                    passCode = num1 + num2 + num3 + num4
                    checkDotPin = true
                    checkDone()
                }
            }
        }
    }
}