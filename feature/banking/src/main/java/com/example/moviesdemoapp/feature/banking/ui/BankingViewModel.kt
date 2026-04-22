package com.example.moviesdemoapp.feature.banking.ui

import com.example.moviesdemoapp.core.data.remote.DataSourceExecutor
import com.example.moviesdemoapp.core.domain.BaseViewModel
import com.example.moviesdemoapp.engine.sdui.usecase.LoadSDUIScreenUseCase
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageEffect
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageIntent
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class BankingViewModel @Inject constructor(
    private val loadSDUIScreen: LoadSDUIScreenUseCase,
    private val executeDataSource: DataSourceExecutor,
) : BaseViewModel<BankingPageState, BankingPageIntent, BankingPageEffect>() {

    override fun initialState() = BankingPageState()

    init {
        handleIntent(BankingPageIntent.LoadPersonalDetailMainPage)
    }

    override suspend fun reduce(intent: BankingPageIntent) {
        when (intent) {
            is BankingPageIntent.LoadPersonalDetailMainPage -> loadScreen()
            is BankingPageIntent.LoadOtherMainPage -> loadScreen(intent.pageDetail)
            is BankingPageIntent.OnAction   -> handleAction(intent.actionId, intent.params)
        }
    }

    private suspend fun loadScreen(screenName:String) {
        val screenModel = loadSDUIScreen(screenName)
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
        val screenModel = loadSDUIScreen("personal_details")
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
            "navigate" -> params["route"]?.let { setEffect(BankingPageEffect.Navigate(it)) }
            "navigation" -> params["route"]?.let { setEffect(BankingPageEffect.Navigate(it)) }
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
