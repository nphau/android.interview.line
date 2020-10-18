package com.linecorp.interview.movie.app

import com.linecorp.android.LINEApp
import com.linecorp.android.libs.logger.ReleaseLoggingTree
import com.linecorp.interview.movie.app.di.DaggerAppComponent
import com.linecorp.interview.movie.app.libs.CrashlyticsLoggingTree
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.HasAndroidInjector

class MovieApp : LINEApp(), HasAndroidInjector {

    override fun releaseLoggingTree(): ReleaseLoggingTree {
        return CrashlyticsLoggingTree()
    }

    override fun isDebugMode(): Boolean {
        return true
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.factory().create(this)
    }
}