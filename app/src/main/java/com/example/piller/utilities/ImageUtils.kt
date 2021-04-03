package com.example.piller.utilities

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object ImageUtils {
    fun saveFile(context: Context, b: Bitmap, fileName: String): File {
        val filePath: String =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath.toString() +
                    "/Piller"
        val dir = File(filePath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "$fileName.png")
        val fOut = FileOutputStream(file)

        b.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        fOut.flush()
        fOut.close()
        return file
    }

    fun loadImageFile(context: Context, fileName: String): File? {
        var b: File? = null
        val filePath: String =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath.toString() +
                    "/Piller"
        try {
            val dir = File(filePath)
            b = File(dir, "$fileName.png")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return b
    }

    fun deleteFile(fileName: String, context: Context) {
        val filePath: String =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath.toString() +
                    "/Piller"
        try {
            val dir = File(filePath)
            val file = File(dir, "$fileName.png")
            file.delete()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}