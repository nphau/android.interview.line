package com.linecorp.android.libs.logger

import android.os.Build
import android.util.Log
import com.linecorp.android.extensions.isNetworkException
import timber.log.Timber

class DebugLoggingTree : Timber.DebugTree() {

    companion object {
        @JvmStatic
        val BLACK_LIST_OF_DEVICES = arrayListOf("HUAWEI")
    }

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {

        if (throwable.isNetworkException()) {
            return
        }

        // Workaround for devices that doesn't show lower priority logs
        if (BLACK_LIST_OF_DEVICES.any { it == Build.MANUFACTURER }) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO)
                super.log(Log.ERROR, tag, message, throwable)
            else
                super.log(priority, tag, message, throwable)
        } else {
            super.log(priority, tag, message, throwable)
        }
    }

}