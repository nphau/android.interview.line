package com.linecorp.android.di.module

import android.content.ClipboardManager
import android.content.Context
import com.linecorp.android.LINEApp
import com.linecorp.android.di.qualifier.SchedulerComputationThread
import com.linecorp.android.di.qualifier.SchedulerIoThread
import com.linecorp.android.di.qualifier.SchedulerMainThread
import com.linecorp.android.di.qualifier.SchedulerNewThread
import dagger.Module
import io.reactivex.schedulers.Schedulers
import io.reactivex.Scheduler
import dagger.Provides
import javax.inject.Singleton
import io.reactivex.android.schedulers.AndroidSchedulers

@Module
object SchedulerModule {

    @Provides
    fun provideClipboardManager(context: Context): ClipboardManager =
        context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    @Singleton
    @Provides
    fun provideContext(): Context = LINEApp.getContext()

    @Provides
    @Singleton
    @SchedulerComputationThread
    fun provideComputationScheduler(): Scheduler {
        return Schedulers.computation()
    }

    @Provides
    @SchedulerIoThread
    @Singleton
    fun provideIoScheduler(): Scheduler {
        return Schedulers.io()
    }

    @Provides
    @Singleton
    @SchedulerMainThread
    fun provideMainThreadScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    @Provides
    @Singleton
    @SchedulerComputationThread
    fun provideTrampolineThreadScheduler(): Scheduler {
        return Schedulers.trampoline()
    }

    @Provides
    @Singleton
    @SchedulerNewThread
    fun provideNewThreadScheduler(): Scheduler {
        return Schedulers.newThread()
    }
}