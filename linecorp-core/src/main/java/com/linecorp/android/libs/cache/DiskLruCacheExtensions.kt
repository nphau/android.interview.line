package com.linecorp.android.libs.cache

import android.graphics.Bitmap
import com.linecorp.android.extensions.hash
import java.io.File
import java.io.OutputStream

/**
 * check if contain or not
 * put Bitmap into DiskLruCache
 * */
fun openAs(cacheDir: File, maxSize: Long): DiskLruCache {
    return DiskLruCache.open(cacheDir, 1, 1, maxSize)
}

/**
 * check if contain or not
 * put Bitmap into DiskLruCache
 * */
fun DiskLruCache.put(key: String, bitmap: Bitmap) {
    val hash = key.hash()
    if (!containsKey(hash)) {
        putAs(hash, bitmap)
    }
}

/* put Bitmap into DiskLruCache */
private fun DiskLruCache?.putAs(key: String, bitmap: Bitmap) {
    try {
        val editor: DiskLruCache.Editor? = this?.edit(key)
        if (editor != null) {
            val outputStream: OutputStream = editor.newOutputStream(0)
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
            editor.commit()
            outputStream.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/* get Snapshot from DiskLruCache by key */
fun DiskLruCache?.snapshot(key: String) = this?.get(key)

/* get Editor from DiskLruCache by key */
fun DiskLruCache?.editor(key: String) = this?.edit(key)

/* check if key already in DiskLruCache */
fun DiskLruCache?.containsKey(key: String): Boolean {
    var contained = false
    var snapshot: DiskLruCache.Snapshot? = null
    try {
        snapshot = snapshot(key)
        contained = snapshot != null
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        snapshot?.close()
    }
    return contained
}
