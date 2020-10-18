package com.linecorp.android.screens.common

import com.linecorp.android.vm.UIViewState

interface UIBehaviour {

    fun showError(message: String?)

    fun render(viewState: UIViewState)

    fun onSyncViews()

    fun onSyncEvents()

    fun onSyncData()

    fun makeVibrator()

    fun doNotCare()

    fun showLoading(isShow: Boolean)

    fun showLoading(isShow: Boolean, timeout: Long)

}
