package com.linecorp.android.libs.logger

import android.util.Log
import com.linecorp.android.extensions.isNetworkException
import timber.log.Timber

abstract class ReleaseLoggingTree : Timber.Tree() {

    companion object {

        @JvmStatic
        val CRASHLYTICS_KEY_PRIORITY = "priority"

        @JvmStatic
        val CRASHLYTICS_KEY_TAG = "tag"

        @JvmStatic
        val CRASHLYTICS_KEY_MESSAGE = "message"
    }

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {

        applyKeys(priority, tag ?: System.currentTimeMillis().toString(), message)

        if (throwable.isNetworkException()) {
            return
        }
        if (priority >= Log.ERROR)
            logException(throwable ?: Exception(message))
        else
            logOther(priority, tag, message)
    }

    abstract fun applyKeys(priority: Int, tag: String?, message: String)

    abstract fun logOther(priority: Int, tag: String?, message: String)

    abstract fun logException(throwable: Throwable?)
}