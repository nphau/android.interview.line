package com.linecorp.android.libs.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Environment
import android.widget.ImageView
import com.linecorp.android.extensions.hash
import io.reactivex.disposables.Disposable
import timber.log.Timber

abstract class ImageCaching(context: Context) {

    protected var mResource = context.resources
    protected var mCallBack: ((Long) -> Unit)? = null
    private var mDownloadDispose: Disposable? = null

    abstract fun initCache()

    open fun key(imageUrl: String) = imageUrl.hash()

    fun onProgress(callBack: (Long) -> Unit) {
        this.mCallBack = callBack
    }

    abstract fun load(imageView: ImageView, imageUrl: String)

    abstract fun addToCache(imageUrl: String, bitmap: BitmapDrawable)

    abstract fun getFromCache(data: String): Bitmap?

    /**
     * Called when the processing is complete and the final drawable should be
     * set on the ImageView.
     */
    protected fun display(imageUrl: String, imageView: ImageView, bitmap: Bitmap?) {
        val drawable = BitmapDrawable(mResource, bitmap)
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

    abstract fun clear()

    abstract fun flush()

    abstract fun close()

    abstract fun bufferSize(): Int

    abstract fun delayEmitter(): Long

    abstract fun compressQuantity(): Int

    abstract fun compressFormat(): Bitmap.CompressFormat

    fun disposeDownload() {
        if (this.mDownloadDispose?.isDisposed == false) {
            this.mDownloadDispose?.dispose()
        }
    }

    fun setDownLoadDispose(dispose: Disposable) {
        this.mDownloadDispose = dispose
    }

    // Check if media is mounted or storage is built-in, if so, try and cacheStrategy external cache dir
    // otherwise cacheStrategy internal cache dir
    protected fun isMediaMounted() =
        Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                || !Environment.isExternalStorageRemovable()

    fun log(error: Throwable) = Timber.tag(this::class.java.name).e(error)
}