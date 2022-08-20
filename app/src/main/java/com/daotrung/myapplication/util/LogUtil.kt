package com.daotrung.myapplication.util

import android.util.Log
import com.daotrung.myapplication.BuildConfig

class LogUtil {
    val CLASS = 4


    fun i(message: Any) {
        if (BuildConfig.DEBUG) {
            Log.i(getClassNamevsMethodvsLineNumber(), message.toString())
        }
    }

    fun d(message: Any) {
        if (BuildConfig.DEBUG) {
            Log.d(getClassNamevsMethodvsLineNumber(), message.toString())
        }
    }

    fun e(message: Any) {
        if (BuildConfig.DEBUG) {
            Log.e(getClassNamevsMethodvsLineNumber(), message.toString())
        }
    }

    fun v(message: Any) {
        if (BuildConfig.DEBUG) {
            Log.v(getClassNamevsMethodvsLineNumber(), message.toString())
        }
    }

    fun w(message: Any) {
        if (BuildConfig.DEBUG) {
            Log.w(getClassNamevsMethodvsLineNumber(), message.toString())
        }
    }

    fun getClassNamevsMethodvsLineNumber(): String {
        val fullClassName = Thread.currentThread().stackTrace[CLASS].className
        val className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
        val methodName = Thread.currentThread().stackTrace[CLASS].methodName
        val lineNumber = Thread.currentThread().stackTrace[CLASS].lineNumber
        return "$className.$methodName():$lineNumber----> "
    }
}

val LogInstance = LogUtil()