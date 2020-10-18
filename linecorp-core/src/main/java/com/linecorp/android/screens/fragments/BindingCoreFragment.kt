package com.linecorp.android.screens.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.linecorp.android.binding.autoCleared
import com.linecorp.android.executors.AppExecutors
import javax.inject.Inject

open class BindingCoreFragment<B : ViewDataBinding>(@LayoutRes val layoutId: Int) : CoreFragment() {

    @Inject
    lateinit var appExecutors: AppExecutors

    protected var binding by autoCleared<B>()

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater,
                layoutId,
                container,
                false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}