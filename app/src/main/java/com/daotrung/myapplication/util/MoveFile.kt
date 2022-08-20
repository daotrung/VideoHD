package com.daotrung.myapplication.util

import java.io.*
import java.nio.channels.FileChannel

object MoveFile {

    @Throws(IOException::class)
    @JvmOverloads
    fun moveFileWithExceptions(from: File, to: File, overwrite: Boolean = true) {
        if (!from.exists()) {
            throw FileNotFoundException(String.format("'from' file was not found (%s).", from.toString()))
        }

        if (overwrite && to.exists()) {
            if (!to.delete()) {
                throw IOException(String.format("'to' file was cannot be overwritten (%s).", to.toString()))
            }
        }
        if (from.renameTo(to)) {
            return
        }
        if (to.createNewFile()) {
            var source: FileChannel? = null
            var destination: FileChannel? = null
            try {
                source = FileInputStream(from).channel
                destination = FileOutputStream(to).channel
                destination!!.transferFrom(source, 0, source!!.size())
            } catch (ex: IOException) {
                to.delete()
                throw ex
            } finally {
                source?.close()
                destination?.close()
                from.delete()
            }
        }
        else {
            throw IOException(String.format("'to' file was cannot be created (%s).", to.toString()))
        }
    }

    @JvmOverloads
    fun moveFile(from: File, to: File, overwrite: Boolean = true): Boolean {
        try {
            moveFileWithExceptions(from, to, overwrite)
            return true
        } catch (ignore: IOException) { }

        return false
    }
    @JvmOverloads
    fun moveFile(from: String, to: String, overwrite: Boolean = true): Boolean {
        val fromFile = File(from)
        val toFile = File(to)
        return moveFile(fromFile,toFile,overwrite)
    }
}