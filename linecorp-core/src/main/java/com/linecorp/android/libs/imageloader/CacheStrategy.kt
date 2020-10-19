package com.linecorp.android.libs.imageloader

import android.graphics.Bitmap.CompressFormat
import kotlin.math.pow

enum class CacheStrategy {

    DISK {
        override fun cacheSize() = (20 * 2.0.pow(20.0)).toLong()
        override fun bufferSize() = 8 * 1024
        override fun threadPool() = 5
        override fun compressFormat(): CompressFormat = CompressFormat.JPEG
        override fun compressQuantity() = 100
    },
    MEMORY {
        override fun cacheSize() = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 8L
        override fun bufferSize() = 8 * 1024
        override fun threadPool() = 5
        override fun compressFormat() = CompressFormat.JPEG
        override fun compressQuantity() = 100
    };

    abstract fun cacheSize(): Long
    abstract fun bufferSize(): Int
    abstract fun threadPool(): Int
    abstract fun compressQuantity(): Int
    abstract fun compressFormat(): CompressFormat
}