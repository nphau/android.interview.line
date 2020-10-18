package com.linecorp.interview.movie.app.data.repository

import com.linecorp.interview.movie.app.data.model.Movie
import com.linecorp.interview.movie.app.domain.MovieRepository
import io.reactivex.Single
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor() : MovieRepository {
    override fun getMovie(): Single<Movie> {
        return Single.create { emitter ->
            if (!emitter.isDisposed)
                emitter.onSuccess(
                    Movie(
                        "Civil War", listOf(
                            "http://movie.phinf.naver.net/20151127_272/1448585271749MCMVs_JPEG/movie_image.jpg",
                            "http://movie.phinf.naver.net/20151127_84/1448585272016tiBsF_JPEG/movie_image.jpg",
                            "http://movie.phinf.naver.net/20151125_36/1448434523214fPmj0_JPEG/movie_image.jpg",

                        )
                    )
                )
        }
    }
}