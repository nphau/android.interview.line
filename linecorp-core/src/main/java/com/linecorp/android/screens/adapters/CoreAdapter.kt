package com.linecorp.android.screens.adapters

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linecorp.android.executors.AppExecutors

abstract class CoreAdapter<T>(
    appExecutors: AppExecutors,
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, RecyclerView.ViewHolder>(
    AsyncDifferConfig.Builder<T>(diffCallback)
        .setBackgroundThreadExecutor(appExecutors.diskIO())
        .build()
) {

    open fun get(position: Int): T? {
        if (position < 0 || position > itemCount)
            return null
        return super.getItem(position)
    }

}
