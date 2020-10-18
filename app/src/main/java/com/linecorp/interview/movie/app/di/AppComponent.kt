package com.linecorp.interview.movie.app.di

import com.linecorp.android.di.module.SharedModule
import com.linecorp.android.di.scope.ApplicationScope
import com.linecorp.interview.movie.app.MovieApp
import com.linecorp.interview.movie.app.di.modules.MainModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@ApplicationScope
@Component(
    modules = [
        SharedModule::class,
        MainModule::class,
        AndroidSupportInjectionModule::class]
)
interface AppComponent : AndroidInjector<MovieApp> {

    @Component.Factory
    interface Factory : AndroidInjector.Factory<MovieApp>

}