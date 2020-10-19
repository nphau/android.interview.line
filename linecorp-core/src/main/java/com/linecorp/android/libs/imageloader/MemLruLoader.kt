package com.linecorp.android.libs.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.LruCache
import android.widget.ImageView
import com.linecorp.android.extensions.applySingleIoScheduler
import io.reactivex.Single
import timber.log.Timber
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MemLruLoader(context: Context) : ImageCaching(context) {

    companion object {
        internal val MEM_CACHE_SIZE = CacheStrategy.MEMORY.cacheSize()
    }

    private var mReusableBitmaps: Set<WeakReference<Bitmap>> =
        Collections.synchronizedSet(HashSet())
    private var mMemoryCache: LruCache<String, BitmapDrawable>? = null

    init {
        initCache()
    }

    override fun initCache() {

        mMemoryCache = object : LruCache<String, BitmapDrawable>(MEM_CACHE_SIZE.toInt()) {
            /* Notify the removed entry that is no longer being cached */
            override fun entryRemoved(
                evicted: Boolean, key: String,
                oldValue: BitmapDrawable, newValue: BitmapDrawable
            ) {
                if (oldValue is RecyclingBitmapDrawable) {
                    // The removed entry is a recycling drawable, so notify it
                    // that it has been removed from the memory cache
                    oldValue.setIsCached(false)
                } else {
                    // The removed entry is a standard BitmapDrawable.
                    // Add the bitmap to a SoftReference set for possible use with inBitmap
                    // later.
                    mReusableBitmaps.plus(WeakReference(oldValue.bitmap))
                }
            }

            /**
             * Measure item size in kilobytes rather than units which is more practical
             * for a bitmap cache
             */
            override fun sizeOf(key: String, value: BitmapDrawable): Int {
                val bitmapSize: Int = BitmapUtils.getBitmapSize(value) / 1024
                return if (bitmapSize == 0) 1 else bitmapSize
            }
        }
    }

    override fun load(imageView: ImageView, imageUrl: String) {
        imageView.setImageResource(0)
        val bitmap = getFromCache(imageUrl)
        // If the bitmap was processed and the image cache is available, then add the processed
        // bitmap to the cache for future use. Note we don't check if the task was cancelled
        // here, if it was, and the thread is still running, we may as well add the processed
        // bitmap to our cache as it might be used again in the future
        if (bitmap != null) {
            display(imageUrl, imageView, bitmap)
            return
        }
        setDownLoadDispose(
            download(imageUrl).compose(applySingleIoScheduler())
                .subscribe({
                    addToCache(imageUrl, BitmapDrawable(mResource, it))
                    display(imageUrl, imageView, getFromCache(imageUrl))
                }, { Timber.e(it) })
        )
    }

    private fun download(imageUrl: String?): Single<Bitmap> {
        if (imageUrl.isNullOrEmpty())
            return Single.error(NullPointerException())
        return Single.create { emitter ->
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                if (bitmap != null) {
                    if (!emitter.isDisposed)
                        emitter.onSuccess(bitmap)
                }
            } catch (e: Exception) {
                if (!emitter.isDisposed)
                    emitter.onError(e)
            }
        }
    }

    override fun delayEmitter() = 100L

    override fun bufferSize(): Int = CacheStrategy.MEMORY.bufferSize()

    override fun compressFormat() = CacheStrategy.DISK.compressFormat()

    override fun compressQuantity(): Int = CacheStrategy.DISK.compressQuantity()

    @Synchronized
    override fun addToCache(imageUrl: String, bitmap: BitmapDrawable) {
        // Add to memory cache
        if (mMemoryCache != null) {
            if (bitmap is RecyclingBitmapDrawable) {
                // The removed entry is a recycling drawable, so notify it
                // that it has been added into the memory cache
                bitmap.setIsCached(true)
            }
            mMemoryCache!!.put(key(imageUrl), bitmap)
        }
    }

    @Synchronized
    override fun getFromCache(data: String): Bitmap? {
        return mMemoryCache?.get(key(data))?.bitmap
    }

    override fun clear() {
        mMemoryCache?.evictAll()
        disposeDownload()
    }

    override fun flush() {
        disposeDownload()
    }

    override fun close() {
        disposeDownload()
    }

}