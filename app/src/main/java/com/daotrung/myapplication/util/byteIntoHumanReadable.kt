package com.daotrung.myapplication.util

object byteIntoHumanReadable {
    fun setTo(bytes: Long): String? {
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024
        return if (bytes in 0 until kilobyte) {
            "$bytes B"
        } else if (bytes in kilobyte until megabyte) {
            (bytes / kilobyte).toString() + " KB"
        } else if (bytes in megabyte until gigabyte) {
            (bytes / megabyte).toString()
        } else if (bytes in gigabyte until terabyte) {
            (bytes / gigabyte).toString() + " GB"
        } else if (bytes >= terabyte) {
            (bytes / terabyte).toString() + " TB"
        } else {
            "$bytes Bytes"
        }
    }
}