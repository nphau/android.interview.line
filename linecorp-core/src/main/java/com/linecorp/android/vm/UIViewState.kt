package com.linecorp.android.vm

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UIViewState(
    val isLoading: Boolean = false,
    val isVibrator: Boolean = false,
    var isError: String? = null
) : Parcelable {
    companion object {
        fun error(errorMessage: String?) = UIViewState(isError = errorMessage)
        fun vibrator(isVibrator: Boolean) = UIViewState(isVibrator = isVibrator)
        fun loading(newIsLoading: Boolean) = UIViewState(isLoading = newIsLoading)
    }
}
