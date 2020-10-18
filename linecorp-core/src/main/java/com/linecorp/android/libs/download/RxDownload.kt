package com.linecorp.android.libs.download

import com.linecorp.android.libs.imageloader.CacheStrategy
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object RxDownload {

    fun download(imageUrl: String?, outputStream: OutputStream?): Flowable<Long> {

        if (imageUrl.isNullOrEmpty() || outputStream == null)
            return Flowable.error(NullPointerException())

        return Flowable.create({ emitter ->

            var urlConnection: HttpURLConnection? = null
            var bufferedOutputStream: BufferedOutputStream? = null
            var bufferedInputStream: BufferedInputStream? = null
            try {
                val url = URL(imageUrl)
                urlConnection = url.openConnection() as HttpURLConnection
                bufferedInputStream = BufferedInputStream(
                    urlConnection.inputStream,
                    CacheStrategy.DISK.bufferSize()
                )
                bufferedOutputStream = BufferedOutputStream(
                    outputStream,
                    CacheStrategy.DISK.bufferSize()
                )
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
                } catch (e: IOException) {
                    if (!emitter.isCancelled) {
                        emitter.onError(e)
                    }
                }
            }
        }, BackpressureStrategy.DROP)
    }
}