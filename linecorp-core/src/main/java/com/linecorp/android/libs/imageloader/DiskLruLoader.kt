package com.linecorp.android.libs.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Environment
import android.widget.ImageView
import com.linecorp.android.extensions.applyFlowableIoScheduler
import com.linecorp.android.extensions.hash
import com.linecorp.android.libs.cache.DiskLruCache
import com.linecorp.android.libs.cache.editor
import com.linecorp.android.libs.cache.openAs
import com.linecorp.android.libs.cache.snapshot
import com.linecorp.android.libs.download.RxDownload
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

class DiskLruLoader(context: Context) : ImageCacheBehavior {

    companion object {
        internal const val DISK_CACHE_INDEX = 0
        internal val TAG = DiskLruLoader::class.java.name
        internal val DISK_CACHE_SIZE = CacheStrategy.DISK.cacheSize()
        internal val COMPRESS_FORMAT = CacheStrategy.DISK.compressFormat()
        internal val COMPRESS_QUANTITY = CacheStrategy.DISK.compressQuantity()
        internal const val DISK_CACHE_SUB_DIRECTORY = "thumbnails"
        fun log(error: Throwable) = Timber.tag(TAG).e(error)
    }

    private var resource = context.resources

    private var mDiskCacheStarting = true
    private var mDownloadDispose: Disposable? = null
    private val mDiskCacheLock = ReentrantLock()
    private var mDiskLruCache: DiskLruCache? = null
    private var cacheDir: File = getDiskCacheDir(context, DISK_CACHE_SUB_DIRECTORY)
    private val diskCacheLockCondition: Condition = mDiskCacheLock.newCondition()
    private val imageViews = Collections.synchronizedMap(WeakHashMap<ImageView, String>())
    private var mCallBack: ((Long) -> Unit)? = null

    init {
        // Initialize disk cache on background thread
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

    override fun key(imageUrl: String) = imageUrl.hash()

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
                        bitmap.bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUANTITY, outputStream)
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
            mDownloadDispose?.dispose()
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
            mDownloadDispose?.dispose()
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
            mDownloadDispose?.dispose()
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
        imageViews[imageView] = imageUrl
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
            val interval = Observable.interval(100, TimeUnit.MILLISECONDS, Schedulers.io())
            val download = RxDownload.download(
                imageUrl, editor.newOutputStream(DISK_CACHE_INDEX)
            )
//                mDownloadDispose =
//                    Observable.zip(download, interval, { bytes: Long, _: Long -> bytes })
            download.compose(applyFlowableIoScheduler())
                .doOnNext { this.mCallBack?.invoke(it) }
                .doOnComplete {
                    editor.commit()
                    display(imageUrl, imageView, getFromCache(imageUrl))
                }
                .doOnError { editor.abort() }
                .subscribe({}, { Timber.e(it) })
        }
    }

    /**
     * Called when the processing is complete and the final drawable should be
     * set on the ImageView.
     */
    override fun display(imageUrl: String, imageView: ImageView, bitmap: Bitmap?) {
        val drawable = BitmapDrawable(resource, bitmap)
        addToCache(imageUrl, drawable)
        imageView.setImageDrawable(drawable)
        // Transition drawable with a transparent drawable and the final drawable
        val transitionDrawable =
            TransitionDrawable(arrayOf(ColorDrawable(Color.TRANSPARENT), drawable))
        // Set background to loading bitmap
        imageView.setImageDrawable(drawable)
        imageView.setImageDrawable(transitionDrawable)
        transitionDrawable.startTransition(200)
        this.mCallBack?.invoke(Long.MAX_VALUE)
    }

    override fun onProgress(callBack: (Long) -> Unit) {
        this.mCallBack = callBack
    }

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

    // Check if media is mounted or storage is built-in, if so, try and cacheStrategy external cache dir
    // otherwise cacheStrategy internal cache dir
    private fun isMediaMounted() =
        Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                || !Environment.isExternalStorageRemovable()
}