package com.linecorp.interview.movie.app.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.linecorp.android.extensions.addTo
import com.linecorp.android.vm.CoreViewModel
import com.linecorp.interview.movie.app.data.model.Movie
import com.linecorp.interview.movie.app.domain.MovieRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : CoreViewModel() {

    private val _movie: MutableLiveData<Movie> = MutableLiveData()
    val movie: LiveData<Movie> = _movie

    fun getMove() {
        movieRepository.getMovie()
            .compose(applySingleLoading())
            .subscribe(
                { _movie.postValue(it) },
                { showError(it.message) })
            .addTo(getCompositeDisposable())
    }

}