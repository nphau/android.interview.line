// Use this annotation so you can call it from Java code like FormatUtils.
@file:JvmName("FormatUtils")

package com.linecorp.android.extensions

import android.text.Spanned
import androidx.core.text.HtmlCompat
import java.math.BigInteger
import java.security.MessageDigest

fun String?.fileExtension(): String {
    return this?.substringAfterLast('.', "") ?: ""
}

fun String?.formatHtml(): Spanned {
    if (!isNullOrEmpty()) {
        if (contains("\n"))
            return HtmlCompat
                .fromHtml(replace("\n", "<br/>"), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
    return HtmlCompat.fromHtml(this ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun String.hash(algorithm: String = "SHA-256"): String {
    val messageDigest = MessageDigest.getInstance(algorithm)
    messageDigest.update(this.toByteArray())
    return String.format("%064x", BigInteger(1, messageDigest.digest()))
}
