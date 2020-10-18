package com.linecorp.interview.movie.app.domain

import com.linecorp.interview.movie.app.data.model.Movie
import io.reactivex.Single

interface MovieRepository {
    fun getMovie(): Single<Movie>
}