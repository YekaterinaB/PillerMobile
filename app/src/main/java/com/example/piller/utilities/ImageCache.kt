package com.example.piller.utilities

import androidx.collection.LruCache
import android.graphics.Bitmap

class ImageCache private constructor() {

    private object HOLDER {
        val INSTANCE = ImageCache()
    }

    companion object {
        val instance: ImageCache by lazy { HOLDER.INSTANCE }
    }

    private val lru: LruCache<Any, Any> = LruCache(1024)

    fun saveBitmapToCache(key: String, bitmap: Bitmap) {
        try {
            instance.lru.put(key, bitmap)
        } catch (e: Exception) {
        }

    }

    fun retrieveBitmapFromCache(key: String): Bitmap? {
        try {
            return instance.lru.get(key) as Bitmap?
        } catch (e: Exception) {
        }

        return null
    }

    fun removeImageFromCache(rxcui: String) {
        try {
            instance.lru.remove(rxcui)
        } catch (e: java.lang.Exception) {

        }
    }

    fun removeAllImagesFromCache() {
        try {
            instance.lru.evictAll()
        } catch (e: java.lang.Exception) {

        }
    }

}