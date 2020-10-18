package com.linecorp.interview.movie.app.di.modules

import androidx.lifecycle.ViewModel
import com.linecorp.android.di.ViewModelKey
import com.linecorp.interview.movie.app.data.repository.MovieRepositoryImpl
import com.linecorp.interview.movie.app.domain.MovieRepository
import com.linecorp.interview.movie.app.screens.activities.MainActivity
import com.linecorp.interview.movie.app.screens.fragments.MainFragment
import com.linecorp.interview.movie.app.vm.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
interface MainModule {

    // region [ViewModel]
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(viewModel: MainViewModel): ViewModel
    // endregion

    // region [Activity]
    @ContributesAndroidInjector
    fun contributeMainActivity(): MainActivity
    // endregion

    // region [Fragment]
    @ContributesAndroidInjector
    fun contributeMainFragment(): MainFragment
    // endregion

    // region [Repository]
    @Binds
    @Singleton
    fun bindMovieRepository(movieRepositoryImpl: MovieRepositoryImpl): MovieRepository
    // endregion
}