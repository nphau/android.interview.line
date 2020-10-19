package com.linecorp.android.libs.imageloader

import android.content.Context
import android.widget.ImageView
import com.linecorp.android.utils.SingletonArgument
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageLoader private constructor(val context: Context) {

    private var loader: ImageCaching? = null
    private var executorService: ExecutorService? = null

    companion object : SingletonArgument<ImageLoader, Context>(::ImageLoader) {
        internal var screenWidth = 0
        internal var screenHeight = 0
    }

    init {
        val metrics = context.resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        // Initialize disk cache
        cacheStrategy(CacheStrategy.DISK)
    }

    private fun threadPoolTags(): Pair<Int, String> {
        return when (loader) {
            is DiskLruLoader -> {
                Pair(CacheStrategy.DISK.threadPool(), DiskLruLoader::class.java.name)
            }
            else -> {
                Pair(CacheStrategy.MEMORY.threadPool(), MemLruLoader::class.java.name)
            }
        }
    }

    fun cacheStrategy(source: CacheStrategy): ImageLoader {
        loader = when (source) {
            CacheStrategy.DISK -> DiskLruLoader(context)
            else -> MemLruLoader(context)
        }
        // Define executor service
        val (threadPool, tag) = threadPoolTags()
        executorService = Executors.newFixedThreadPool(threadPool, PriorityThreadFactory(tag))
        return this
    }

    fun onProgress(callBack: (Long) -> Unit): ImageLoader {
        loader?.onProgress(callBack)
        return this
    }

    fun load(imageView: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty())
            return
        loader?.load(imageView, imageUrl)
    }

    fun flush() {
        executorService?.execute { loader?.flush() }
    }

    fun close() {
        executorService?.execute { loader?.close() }
    }
}