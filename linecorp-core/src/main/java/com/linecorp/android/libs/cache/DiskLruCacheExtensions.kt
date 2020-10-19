package com.linecorp.android.libs.cache

import java.io.File

/**
 * check if contain or not
 * put Bitmap into DiskLruCache
 * */
fun openAs(cacheDir: File, maxSize: Long): DiskLruCache {
    return DiskLruCache.open(cacheDir, 1, 1, maxSize)
}

/* get Snapshot from DiskLruCache by key */
fun DiskLruCache?.snapshot(key: String) = this?.get(key)

/* get Editor from DiskLruCache by key */
fun DiskLruCache?.editor(key: String) = this?.edit(key)