package com.linecorp.interview.movie.app.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Movie(val title: String, val images: List<String>) : Parcelable