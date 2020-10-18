package com.linecorp.android.screens.activities

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.linecorp.android.executors.AppExecutors
import com.linecorp.android.extensions.activityBinding
import javax.inject.Inject

open class BindingCoreActivity<B : ViewDataBinding>(@LayoutRes val layoutId: Int) : CoreActivity() {

    protected val binding: B by activityBinding(layoutId)

    @Inject
    lateinit var appExecutors: AppExecutors

    override fun onSyncViews() {
        super.onSyncViews()
        binding.lifecycleOwner = this
    }
}