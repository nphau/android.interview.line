package com.linecorp.android.screens.fragments

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.linecorp.android.extensions.hideKeyboardIfNeed
import com.linecorp.android.screens.common.UIBehaviour
import com.linecorp.android.utils.CommonUtils
import com.linecorp.android.vm.UIViewState
import dagger.android.support.DaggerFragment
import org.jetbrains.anko.support.v4.longToast

open class CoreFragment : DaggerFragment(), UIBehaviour {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerBackPress()
        onSyncViews()
        onSyncEvents()
        onSyncData()
    }

    /**
     * Note that you could enable/disable the callback here as
     * well by setting callback.isEnabled = true/false
     */
    private fun registerBackPress() {
        /** true means that the callback is enabled */
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(
                enableOnBackPressedCallback()
            ) {
                override fun handleOnBackPressed() {
                    doOnBackPressed()
                }
            })
    }

    protected open fun doOnBackPressed() {

    }

    protected open fun enableOnBackPressedCallback() = false

    override fun onSyncViews() {

    }

    override fun onSyncEvents() {

    }

    override fun onSyncData() {

    }

    override fun onPause() {
        super.onPause()
        hideKeyboardIfNeed()
    }

    override fun render(viewState: UIViewState) {
        // Loading
        showLoading(viewState.isLoading)

        // Error message
        if (viewState.isError != null)
            showError(viewState.isError)

        // Vibrator
        if (viewState.isVibrator)
            makeVibrator()
    }

    override fun showError(message: String?) {
        longToast(message ?: "")
    }

    override fun makeVibrator() {
        CommonUtils.makeVibrator(requireContext())
    }

    protected fun showLoading() {
        hideKeyboardIfNeed()
        showLoading(true)
    }

    protected fun dismissLoading() {
        showLoading(false)
    }

    override fun showLoading(isShow: Boolean) {
        showLoading(isShow, 0)
    }

    override fun showLoading(isShow: Boolean, timeout: Long) {

    }

    override fun doNotCare() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            dismissLoading()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
