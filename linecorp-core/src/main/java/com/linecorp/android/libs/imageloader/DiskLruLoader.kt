package com.linecorp.android.libs.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.linecorp.android.extensions.applyFlowableIoScheduler
import com.linecorp.android.libs.cache.DiskLruCache
import com.linecorp.android.libs.cache.editor
import com.linecorp.android.libs.cache.openAs
import com.linecorp.android.libs.cache.snapshot
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import timber.log.Timber
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

class DiskLruLoader(context: Context) : ImageCaching(context) {

    companion object {
        internal const val DISK_CACHE_INDEX = 0
        internal val DISK_CACHE_SIZE = CacheStrategy.DISK.cacheSize()
        internal const val DISK_CACHE_SUB_DIRECTORY = "thumbnails"
    }

    private var mDiskCacheStarting = true
    private val mDiskCacheLock = ReentrantLock()
    private var mDiskLruCache: DiskLruCache? = null
    private var cacheDir: File = getDiskCacheDir(context, DISK_CACHE_SUB_DIRECTORY)
    private val diskCacheLockCondition: Condition = mDiskCacheLock.newCondition()

    init {
        initCache()
    }

    /* Initializes the disk cache in a background thread. */
    override fun initCache() {
        synchronized(mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache!!.isClosed) {
                // create folder if it is not exists
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
                if (cacheDir.usableSpace > DISK_CACHE_SIZE) {
                    try { // check if has enough available storage
                        mDiskLruCache = openAs(cacheDir, DISK_CACHE_SIZE)
                    } catch (e: Exception) {
                        log(e)
                    }
                }
            }
            mDiskCacheStarting = false // Finished initialization
            mDiskCacheLock.lock()
            diskCacheLockCondition.signalAll() // Wake any waiting threads
        }
    }

    override fun addToCache(imageUrl: String, bitmap: BitmapDrawable) {
        val key = key(imageUrl)
        // Also add to disk cache
        synchronized(mDiskCacheLock) {
            val snapshot = mDiskLruCache?.snapshot(key)
            var outputStream: OutputStream? = null
            try {
                snapshot?.getInputStream(DISK_CACHE_INDEX)?.close()
                    ?: mDiskLruCache?.editor(key)?.let { editor ->
                        outputStream = editor.newOutputStream(DISK_CACHE_INDEX)
                        bitmap.bitmap.compress(compressFormat(), compressQuantity(), outputStream)
                        editor.commit()
                        outputStream?.close()
                    }
            } catch (e: Exception) {
                log(e)
            } finally {
                outputStream?.close()
            }
        }
    }

    /**
     * Get from disk cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    override fun getFromCache(data: String): Bitmap? {
        val key = key(data)

        var bitmap: Bitmap? = null
        synchronized(mDiskCacheLock) {
            // wait until no one want to read
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.lock()
                } catch (e: Exception) {
                    log(e)
                }
            }
            if (mDiskLruCache == null)
                return null
            var inputStream: InputStream? = null
            try {
                val snapshot = mDiskLruCache?.snapshot(key)
                if (snapshot != null) {
                    inputStream = snapshot.getInputStream(DISK_CACHE_INDEX)
                    if (inputStream != null) {
                        val fd = (inputStream as FileInputStream).fd
                        // Decode bitmap, but we don't want to sample so give
                        // MAX_VALUE as the target dimensions
                        bitmap = BitmapUtils.decodeSampledBitmapFromDescriptor(
                            fd,
                            Int.MAX_VALUE,
                            Int.MAX_VALUE
                        )
                    }
                }
            } catch (e: Exception) {
                log(e)
            } finally {
                try {
                    inputStream?.close()
                } catch (e: Exception) {
                    log(e)
                }
            }
            return bitmap
        }
    }

    override fun clear() {
        synchronized(mDiskCacheLock) {
            disposeDownload()
            mDiskCacheStarting = true
            if (mDiskLruCache != null && !mDiskLruCache!!.isClosed) {
                try {
                    mDiskLruCache?.delete()
                } catch (e: Exception) {
                    log(e)
                }
                mDiskLruCache = null
                initCache()
            }
        }
    }

    override fun flush() {
        synchronized(mDiskCacheLock) {
            disposeDownload()
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache?.flush()
                } catch (e: IOException) {
                    log(e)
                }
            }
        }
    }

    override fun close() {
        synchronized(mDiskCacheLock) {
            disposeDownload()
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache!!.isClosed) {
                        mDiskLruCache!!.close()
                        mDiskLruCache = null
                    }
                } catch (e: Exception) {
                    log(e)
                }
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
        val key = key(imageUrl)
        mDiskLruCache?.editor(key)?.let { editor ->
            setDownLoadDispose(download(imageUrl, editor.newOutputStream(DISK_CACHE_INDEX))
                .throttleLast(delayEmitter(), TimeUnit.MILLISECONDS)
                .compose(applyFlowableIoScheduler())
                .doOnNext { this.mCallBack?.invoke(it) }
                .doOnComplete {
                    editor.commit()
                    display(imageUrl, imageView, getFromCache(imageUrl))
                }
                .doOnError { editor.abort() }
                .subscribe({}, { Timber.e(it) })
            )
        }
    }

    private fun download(imageUrl: String?, outputStream: OutputStream?): Flowable<Long> {
        if (imageUrl.isNullOrEmpty() || outputStream == null)
            return Flowable.error(NullPointerException())

        return Flowable.create({ emitter ->

            var urlConnection: HttpURLConnection? = null
            var bufferedOutputStream: BufferedOutputStream? = null
            var bufferedInputStream: BufferedInputStream? = null
            try {
                val url = URL(imageUrl)
                urlConnection = url.openConnection() as HttpURLConnection
                bufferedOutputStream = BufferedOutputStream(outputStream, bufferSize())
                bufferedInputStream = BufferedInputStream(urlConnection.inputStream, bufferSize())
                var total: Long = 0
                var count: Int
                while (bufferedInputStream.read().also { count = it } != -1) {
                    if (emitter.isCancelled) {
                        bufferedInputStream.close()
                        emitter.onError(RuntimeException("Input Stream was closed"))
                    } else {
                        total += count.toLong()
                        if (count > 0) {
                            if (!emitter.isCancelled) {
                                emitter.onNext(total)
                            }
                        }
                        bufferedOutputStream.write(count)
                    }
                }
            } catch (e: Exception) {
                if (!emitter.isCancelled) {
                    emitter.onError(e)
                }
            } finally {
                urlConnection?.disconnect()
                try {
                    bufferedOutputStream?.close()
                    bufferedInputStream?.close()
                    emitter.onComplete()
                } catch (e: Exception) {
                    if (!emitter.isCancelled) {
                        emitter.onError(e)
                    }
                }
            }
        }, BackpressureStrategy.DROP)
    }

    override fun delayEmitter() = 100L

    override fun bufferSize(): Int = CacheStrategy.DISK.bufferSize()

    override fun compressFormat() = CacheStrategy.DISK.compressFormat()

    override fun compressQuantity(): Int = CacheStrategy.DISK.compressQuantity()

    // Creates a unique subdirectory of the designated app cache directory
    private fun getDiskCacheDir(context: Context, uniqueName: String): File {
        // Check if media is mounted or storage is built-in, if so, try and cacheStrategy external cache dir
        // otherwise cacheStrategy internal cache dir
        val cachePath = if (isMediaMounted()) {
            context.externalCacheDir?.path
        } else {
            context.cacheDir.path
        }
        return File("$cachePath/$uniqueName")
    }

}