package com.linecorp.android.screens.activities

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import com.linecorp.android.R
import com.linecorp.android.extensions.formatHtml
import com.linecorp.android.extensions.hideKeyboardIfNeed
import com.linecorp.android.screens.common.UIBehaviour
import com.linecorp.android.utils.CommonUtils
import com.linecorp.android.vm.UIViewState
import dagger.android.support.DaggerAppCompatActivity
import org.jetbrains.anko.longToast

open class CoreActivity : DaggerAppCompatActivity(), UIBehaviour {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onSyncViews()
        onSyncEvents()
        onSyncData()
    }

    override fun onSyncViews() {

    }

    override fun onSyncEvents() {

    }

    override fun onSyncData() {

    }

    override fun render(viewState: UIViewState) {
        // Loading
        showLoading(viewState.isLoading)

        // Error message
        if (viewState.isError != null)
            showError(viewState.isError)

        // vibrator
        if (viewState.isVibrator)
            makeVibrator()
    }

    override fun showError(message: String?) {
        longToast(message.formatHtml())
    }

    override fun showLoading(isShow: Boolean) {
        showLoading(isShow, 0)
    }

    override fun showLoading(isShow: Boolean, timeout: Long) {

    }

    override fun makeVibrator() {
        CommonUtils.makeVibrator(this)
    }

    override fun startActivity(intent: Intent?) {
        try {
            super.startActivity(intent)
            overridePendingTransitionEnter()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        try {
            super.startActivityForResult(intent, requestCode)
            overridePendingTransitionEnter()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun doNotCare() {
        // Empty method
    }

    override fun onPause() {
        super.onPause()
        hideKeyboardIfNeed()
    }

    override fun finish() {
        super.finish()
        overridePendingTransitionExit()
    }

    private fun overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.anim_slide_from_right, R.anim.anim_slide_to_left)
    }

    private fun overridePendingTransitionExit() {
        overridePendingTransition(R.anim.anim_slide_from_left, R.anim.anim_slide_to_right)
    }

    open fun showLoading() {
        hideKeyboardIfNeed()
        showLoading(true)
    }

    open fun dismissLoading() {
        showLoading(false)
    }

    open fun allowUserDismissKeyboardWhenClickOutSide() = false

    override fun onDestroy() {
        super.onDestroy()
        try {
            dismissLoading()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dispatchTouchEvent(motionEvent: MotionEvent?): Boolean {
        if (allowUserDismissKeyboardWhenClickOutSide())
            hideKeyboardIfNeed()
        return super.dispatchTouchEvent(motionEvent)
    }

}
