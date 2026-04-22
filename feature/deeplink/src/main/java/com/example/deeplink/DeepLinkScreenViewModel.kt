package com.example.deeplink

import com.example.deeplink.model.DeepLinkPageEffect
import com.example.deeplink.model.DeepLinkPageIntent
import com.example.deeplink.model.DeepLinkPageState
import com.example.moviesdemoapp.core.data.ScreenRepository
import com.example.moviesdemoapp.core.data.remote.DataSourceExecutor
import com.example.moviesdemoapp.core.domain.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class DeepLinkScreenViewModel @Inject constructor(
    private val screenRepository: ScreenRepository,
    private val executeDataSource: DataSourceExecutor,
) : BaseViewModel<DeepLinkPageState, DeepLinkPageIntent, DeepLinkPageEffect>() {

    override fun initialState() = DeepLinkPageState()

    override suspend fun reduce(intent: DeepLinkPageIntent) {
        when (intent) {
            is DeepLinkPageIntent.LoadPersonalDetailMainPage -> loadScreen()
            is DeepLinkPageIntent.LoadOtherMainPage -> loadScreen(intent.pageDetail)
            is DeepLinkPageIntent.OnAction   -> handleAction(intent.actionId, intent.params)
        }
    }

    private suspend fun loadScreen(screenName:String) {
        val screenModel = screenRepository.loadScreen(screenName)
            ?: run { setState { copy(error = "Screen config not found") }; return }

        setState { copy(screenModel = screenModel, isLoading = true, error = null) }

        val dataSource = screenModel.dataSource
            ?: run { setState { copy(isLoading = false) }; return }

        runCatching { executeDataSource.execute(dataSource) }
            .onSuccess { items ->
                setState { copy(isLoading = false, listData = mapOf("series" to items)) }
            }
            .onFailure { error ->
                setState { copy(isLoading = false, error = error.message ?: "Failed to load") }
            }
    }

    // ─── Intent handlers ─────────────────────────────────────────────────────

    private suspend fun loadScreen() {
        val screenModel = screenRepository.loadScreen("personal_details")
            ?: run { setState { copy(error = "Screen config not found") }; return }

        setState { copy(screenModel = screenModel, isLoading = true, error = null) }

        val dataSource = screenModel.dataSource
            ?: run { setState { copy(isLoading = false) }; return }

        runCatching { executeDataSource.execute(dataSource) }
            .onSuccess { items ->
                setState { copy(isLoading = false, listData = mapOf("series" to items)) }
            }
            .onFailure { error ->
                setState { copy(isLoading = false, error = error.message ?: "Failed to load") }
            }
    }

    private fun handleAction(actionId: String, params: Map<String, String>) {
        when (actionId) {
            "navigate" -> params["route"]?.let { setEffect(DeepLinkPageEffect.Navigate(it)) }
            "navigation" -> params["route"]?.let { setEffect(DeepLinkPageEffect.Navigate(it)) }
            "search"   -> { /* Phase 3 */ }
            "reorder"  -> {
                val binding = params["binding"] ?: return
                val from    = params["from"]?.toIntOrNull() ?: return
                val to      = params["to"]?.toIntOrNull() ?: return
                reorderList(binding, from, to)
            }
        }
    }

    // ─── Order ───────────────────────────────────────────────────────────────

    // Moves item at [from] to [to] in state.
    // Order lives in-memory only — resets when the process dies.
    private fun reorderList(binding: String, from: Int, to: Int) {
        if (from == to) return
        setState {
            val current = listData[binding]?.toMutableList()
            if (current == null || from !in current.indices || to !in current.indices) {
                this
            } else {
                current.add(to, current.removeAt(from))
                copy(listData = listData + (binding to current.toList()))
            }
        }
    }
}
