package com.linecorp.interview.movie.app.libs

import com.linecorp.android.libs.logger.ReleaseLoggingTree

class CrashlyticsLoggingTree : ReleaseLoggingTree() {

    override fun applyKeys(priority: Int, tag: String?, message: String) {

    }

    override fun logException(throwable: Throwable?) {
    }

    override fun logOther(priority: Int, tag: String?, message: String) {

    }
}
