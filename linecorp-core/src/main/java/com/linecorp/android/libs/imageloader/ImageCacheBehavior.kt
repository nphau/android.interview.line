package com.linecorp.android.libs.imageloader

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView

interface ImageCacheBehavior {

    fun initCache()

    fun key(imageUrl: String): String

    fun onProgress(callBack: (Long) -> Unit)

    fun load(imageView: ImageView, imageUrl: String)

    fun addToCache(imageUrl: String, bitmap: BitmapDrawable)

    fun getFromCache(data: String): Bitmap?

    fun display(imageUrl: String, imageView: ImageView, bitmap: Bitmap?)

    fun clear()

    fun flush()

    fun close()
}