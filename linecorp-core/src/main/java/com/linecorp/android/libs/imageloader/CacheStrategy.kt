package com.linecorp.android.libs.imageloader

import android.graphics.Bitmap.CompressFormat

enum class CacheStrategy {

    DISK {
        override fun cacheSize() = 1024 * 1024 * 10L // 10MB
        override fun bufferSize() = 8 * 1024
        override fun threadPool() = 5
        override fun compressFormat(): CompressFormat = CompressFormat.JPEG
        override fun compressQuantity() = 100
    },
    MEMORY {
        override fun cacheSize() = 1024 * 1024 * 5L // 10MB
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