package com.example.moviesdemoapp.core.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel enforcing strict MVI unidirectional data flow.
 *
 * @param S UI state — must implement [UiState]
 * @param I UI intent — must implement [UiIntent]
 * @param E UI effect for one-shot events — must implement [UiEffect]
 */
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect> : ViewModel() {

    /** Provide the initial state for this ViewModel. */
    abstract fun initialState(): S

    private val _state = MutableStateFlow(initialState())

    /** Observable UI state. Collect with [collectAsStateWithLifecycle] in composables. */
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)

    /** One-shot UI effects (navigation, toasts). Collect with [LaunchedEffect] in composables. */
    val effect: Flow<E> = _effect.receiveAsFlow()

    /** Dispatch an [intent] to be reduced on the coroutine scope. */
    fun handleIntent(intent: I) {
        viewModelScope.launch { reduce(intent) }
    }

    /** Reduce the [intent] and produce state or effect changes. Must be suspend-safe. */
    protected abstract suspend fun reduce(intent: I)

    /** Atomically apply [block] to the current state. */
    protected fun setState(block: S.() -> S) {
        _state.update { it.block() }
    }

    /** Enqueue a one-shot [effect] for the UI to consume exactly once. */
    protected fun setEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
