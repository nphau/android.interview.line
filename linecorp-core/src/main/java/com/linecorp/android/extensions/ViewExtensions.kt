package com.linecorp.android.extensions

import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Set an onclick listener
 */

inline fun <T : View> T.rxOnClick(crossinline block: () -> Unit): Disposable {
    return RxView.clicks(this)
        .throttleFirst(850, TimeUnit.MILLISECONDS)
        .subscribe({ block() }, { })
}

fun View?.visible() {
    if (this != null) {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }
    }
}

fun View?.gone() {
    if (this != null) {
        if (visibility != View.GONE) {
            visibility = View.GONE
        }
    }
}