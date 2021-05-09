package com.example.piller.utilities

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import com.example.piller.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object ImageUtils {
    private const val imageExtension = ".png"
    private const val imageQuality = 100

    private fun getExternalFilesDir(context: Context): String {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath.toString() +
                "/" + context.getString(R.string.app_name)
    }

    fun saveFile(context: Context, b: Bitmap, fileName: String): File {
        val filePath: String = getExternalFilesDir(context)
        val dir = File(filePath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "$fileName$imageExtension")
        val fOut = FileOutputStream(file)

        b.compress(Bitmap.CompressFormat.PNG, imageQuality, fOut)
        fOut.flush()
        fOut.close()
        return file
    }

    fun loadImageFile(context: Context, fileName: String): File? {
        var b: File? = null
        val filePath: String = getExternalFilesDir(context)
        try {
            val dir = File(filePath)
            b = File(dir, "$fileName$imageExtension")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return b
    }

    fun deleteFile(fileName: String, context: Context) {
        val filePath: String = getExternalFilesDir(context)
        try {
            val dir = File(filePath)
            val file = File(dir, "$fileName$imageExtension")
            file.delete()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}