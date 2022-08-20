package com.daotrung.myapplication.util

import android.content.Context

object UseSharedPreferences {

    // String
    fun putStringPreferences(namePref : String , context: Context,keyPass:String , value : String){
        val sharedPrefQuestion = context.getSharedPreferences(namePref, Context.MODE_PRIVATE)
        with(sharedPrefQuestion.edit()) {
            putString(keyPass, value)
            apply()
        }
    }
    fun getStringPreferences(namePref: String, context: Context, keyPass: String): String? {
        val sharedPrefQuestion = context.getSharedPreferences(namePref, Context.MODE_PRIVATE)
        return sharedPrefQuestion.getString(keyPass, null)
    }

    // Boolean
    fun putBooleanPreferences(namePref : String , context: Context,keyPass:String , value : Boolean){
        val sharedPrefQuestion = context.getSharedPreferences(namePref, Context.MODE_PRIVATE)
        with(sharedPrefQuestion.edit()) {
            putBoolean(keyPass, value)
            apply()
        }
    }
    fun getBooleanPreferences(namePref: String, context: Context, keyPass: String): Boolean {
        val sharedPrefQuestion = context.getSharedPreferences(namePref, Context.MODE_PRIVATE)
        return sharedPrefQuestion.getBoolean(keyPass,false)
    }
    // Int
    fun putIntPreferences(namePref : String , context: Context,keyPass:String , value : Int){
        val sharedPrefQuestion = context.getSharedPreferences(namePref, Context.MODE_PRIVATE)
        with(sharedPrefQuestion.edit()) {
            putInt(keyPass, value)
            apply()
        }
    }
    fun getIntPreferences(namePref: String, context: Context, keyPass: String):Int {
        val sharedPrefQuestion = context.getSharedPreferences(namePref, Context.MODE_PRIVATE)
        return sharedPrefQuestion.getInt(keyPass,0)
    }
}