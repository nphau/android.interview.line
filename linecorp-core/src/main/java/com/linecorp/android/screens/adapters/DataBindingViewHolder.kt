package com.linecorp.android.screens.adapters

import android.content.Context
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

/**
 * A generic ViewHolder that works with a [ViewDataBinding].
 * @param <T> The type of the ViewDataBinding.
</T> */
open class DataBindingViewHolder<T>(
    protected val binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root), LifecycleOwner {

    private val lifecycleRegistry =
        LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    @CallSuper
    open fun bind(item: T?) {
        binding.executePendingBindings()
    }

    open fun context(): Context = itemView.context

    fun onAttached() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun onDetached() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

}
