package com.linecorp.android.domain.internal

import com.linecorp.android.data.paging.Status


sealed class ProcessStatus<out T>(val status: Status, val data: T? = null, val message: String? = null) {

    class LOADING<T> : ProcessStatus<T>(status = Status.LOADING)
    class ERROR<T>(message: String? = null) : ProcessStatus<T>(status = Status.ERROR, data = null, message = message)
    class SUCCESS<T>(data: T? = null) : ProcessStatus<T>(status = Status.SUCCESS, data = data, message = null)
    class IDLE<T> : ProcessStatus<T>(status = Status.EMPTY)

}