package com.daotrung.myapplication.activity

import android.content.Intent
import android.widget.Toast
import com.daotrung.myapplication.abs.BaseActivity
import com.daotrung.myapplication.databinding.ActivitySecurityQuestionBinding
import com.daotrung.myapplication.util.PREFS_QUESTION_PIN
import com.daotrung.myapplication.util.SET_QUESTION_PIN
import com.daotrung.myapplication.util.UseSharedPreferences

class SecurityQuestionActivity : BaseActivity<ActivitySecurityQuestionBinding>() {


    override fun binding(): ActivitySecurityQuestionBinding {
        return ActivitySecurityQuestionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setOnClickListener()
    }

    private fun setOnClickListener() {

        binding.imgBackVerifySercurityQuestion.setOnClickListener {
            finish()
            startActivity(Intent(this,MainActivity::class.java))
        }

        val questionCheck = UseSharedPreferences.getStringPreferences(PREFS_QUESTION_PIN,this, SET_QUESTION_PIN)

        if(questionCheck==null){
            binding.btnDone.setOnClickListener {
                if(binding.edtAnswer.text.toString().trim()!="") {

                    UseSharedPreferences.putStringPreferences(PREFS_QUESTION_PIN,this,
                        SET_QUESTION_PIN,binding.edtAnswer.text.toString())

                    Toast.makeText(this, "The answer has been saved !!",Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,EnterPinActivity::class.java))
                }else{
                    Toast.makeText(this,"Please enter question !!",Toast.LENGTH_SHORT).show()
                }
            }
        }else {

            binding.btnDone.setOnClickListener {
                if(questionCheck == binding.edtAnswer.text.toString()){
                     startActivity(Intent(this,SetPinActivity::class.java))
                }else{
                    Toast.makeText(this,"Wrong question , please check again !!!",Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

}