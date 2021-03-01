package com.example.piller.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import java.lang.Exception


object ImageUtils {
    fun saveFile(context: Context, b: Bitmap, fileName: String) {
        val fos: FileOutputStream
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            b.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadBitmap(context: Context, fileName: String): Bitmap? {
        var b: Bitmap? = null
        val fis: FileInputStream
        try {
            fis = context.openFileInput(fileName)
            b = BitmapFactory.decodeStream(fis)
            fis.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return b
    }

    fun deleteFile(fileName: String, context: Context) {
        try {
            context.deleteFile(fileName)
        } catch (e: Exception) {

        }
    }
}