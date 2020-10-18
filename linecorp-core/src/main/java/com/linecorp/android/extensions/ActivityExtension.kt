package com.linecorp.android.extensions

import android.app.Activity
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.linecorp.android.binding.ActivityBindingProperty
import org.jetbrains.anko.inputMethodManager

/**
 * Extension method to provide hide keyboard for [Activity].
 */
fun Activity.hideKeyboardIfNeed() {
    if (currentFocus != null)
        inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
}


// Use for activity layout binding
fun <T : ViewDataBinding> activityBinding(@LayoutRes resId: Int) = ActivityBindingProperty<T>(resId)