package com.linecorp.android

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.facebook.stetho.Stetho
import com.linecorp.android.libs.logger.DebugLoggingTree
import com.linecorp.android.libs.logger.ReleaseLoggingTree
import com.uber.rxdogtag.RxDogTag
import dagger.android.DaggerApplication
import timber.log.Timber

abstract class LINEApp : DaggerApplication(), LifecycleObserver {

    companion object {

        @JvmField
        var isInBackground = false

        @JvmStatic
        lateinit var application: Application

        @JvmStatic
        fun getContext(): Context = application.applicationContext

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        application = this

        RxDogTag.install()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // Logger
        if (isDebugMode()) {
            Timber.plant(DebugLoggingTree())
            Stetho.initializeWithDefaults(this)
            // FirebaseFirestore.setLoggingEnabled(true)
        } else {
            Timber.plant(releaseLoggingTree())
            // FirebaseFirestore.setLoggingEnabled(false)
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected open fun onEnterForeground() {
        isInBackground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected open fun onEnterBackground() {
        isInBackground = true
    }

    abstract fun releaseLoggingTree(): ReleaseLoggingTree

    open fun isDebugMode(): Boolean {
        return false
    }

}
