package com.linecorp.android.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.linecorp.android.domain.internal.ProcessStatus
import com.linecorp.android.libs.livedata.SingleLiveEvent
import io.reactivex.CompletableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class CoreViewModel : ViewModel(), HasDisposableManager {

    // Composite Disposable
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    // region PROCESS-STATUS
    private val _processStatus: MutableLiveData<ProcessStatus<Boolean>> = MutableLiveData(
        ProcessStatus.IDLE()
    )

    protected fun dispatchProcessStatus(newStatus: ProcessStatus<Boolean>) {
        _processStatus.postValue(newStatus)
    }

    protected fun dispatchProcessLoading() {
        _processStatus.postValue(ProcessStatus.LOADING())
    }

    protected fun dispatchProcessError() {
        _processStatus.postValue(ProcessStatus.ERROR())
    }

    protected fun dispatchProcessIdle() {
        _processStatus.postValue(ProcessStatus.IDLE())
    }

    protected fun dispatchProcessSuccess() {
        _processStatus.postValue(ProcessStatus.SUCCESS())
    }

    fun <T> applySingleLoading(): SingleTransformer<T, T> {
        return SingleTransformer {
            it.doOnSubscribe { showLoading(true) }
                .doFinally { showLoading(false) }
        }
    }

    fun applyCompletableLoading(): CompletableTransformer {
        return CompletableTransformer {
            it.doOnSubscribe { showLoading(true) }
                .doFinally { showLoading(false) }
        }
    }

    fun <T> applyObservableLoading(): ObservableTransformer<T, T> {
        return ObservableTransformer {
            it.doOnSubscribe { showLoading(true) }
                .doFinally { showLoading(false) }
        }
    }

    fun isProcessError(): LiveData<Boolean> {
        return Transformations.map(_processStatus) {
            _processStatus.value is ProcessStatus.ERROR
        }
    }

    fun isProcessLoading(): LiveData<Boolean> {
        return Transformations.map(_processStatus) {
            _processStatus.value is ProcessStatus.LOADING
        }
    }

    fun isProcessIdle(): LiveData<Boolean> {
        return Transformations.map(_processStatus) {
            _processStatus.value !is ProcessStatus.LOADING
        }
    }
    // endregion

    // region UI-STATUS
    private val _viewState: SingleLiveEvent<UIViewState> = SingleLiveEvent()
    val viewState: LiveData<UIViewState> get() = _viewState

    protected fun showLoading(newIsLoading: Boolean) {
        _viewState.postValue(UIViewState.loading(newIsLoading))
    }

    protected fun showVibrator(isVibrator: Boolean) {
        _viewState.postValue(UIViewState.vibrator(isVibrator))
    }

    open fun showError(message: String?) {
        _viewState.postValue(UIViewState.error(message))
    }

    // endregion

    override fun addToDisposable(disposable: Disposable) {
        this.compositeDisposable.add(disposable)
    }

    override fun getCompositeDisposable(): CompositeDisposable {
        if (compositeDisposable.isDisposed)
            compositeDisposable = CompositeDisposable()
        return compositeDisposable
    }

    override fun dispose() {
        getCompositeDisposable().clear()
    }

    override fun onCleared() {
        dispose()
        super.onCleared()
    }
}
