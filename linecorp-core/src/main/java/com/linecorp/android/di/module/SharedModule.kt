package com.linecorp.android.di.module

import androidx.lifecycle.ViewModelProvider
import com.linecorp.android.di.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module(includes = [SchedulerModule::class])
interface SharedModule {
    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
