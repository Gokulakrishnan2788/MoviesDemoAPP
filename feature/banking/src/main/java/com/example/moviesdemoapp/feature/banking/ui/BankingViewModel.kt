package com.example.moviesdemoapp.feature.banking.ui

import com.example.moviesdemoapp.core.data.ScreenRepository
import com.example.moviesdemoapp.core.data.remote.DataSourceExecutor
import com.example.moviesdemoapp.core.domain.BaseViewModel
import com.example.moviesdemoapp.engine.navigation.Routes
import com.example.moviesdemoapp.feature.banking.data.BankingFormStateRepository
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageEffect
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageIntent
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class BankingViewModel @Inject constructor(
    private val screenRepository: ScreenRepository,
    private val executeDataSource: DataSourceExecutor,
    private val formStateRepository: BankingFormStateRepository,
) : BaseViewModel<BankingPageState, BankingPageIntent, BankingPageEffect>() {

    override fun initialState() = BankingPageState()

    init {
        handleIntent(BankingPageIntent.LoadPersonalDetailMainPage)
    }

    override suspend fun reduce(intent: BankingPageIntent) {
        when (intent) {
            is BankingPageIntent.LoadPersonalDetailMainPage -> loadScreen()
            is BankingPageIntent.LoadOtherMainPage -> loadScreen(intent.pageDetail)
            is BankingPageIntent.OnAction -> handleAction(intent.actionId, intent.params)
            is BankingPageIntent.MarkFormCompleted -> handleFormCompleted(intent.formId, intent.formData)
            is BankingPageIntent.CheckAndNavigateToNextForm -> checkAndNavigateToNextForm()
            is BankingPageIntent.ResumeFromSavedState -> resumeFromSavedState()
        }
    }

    private suspend fun loadScreen(screenName: String) {
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

        setState {
            copy(
                screenModel = screenModel,
                isLoading = true,
                error = null,
                currentFormId = Routes.BANKING
            )
        }

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
            "search" -> { /* Phase 3 */ }
            "reorder" -> {
                val binding = params["binding"] ?: return
                val from = params["from"]?.toIntOrNull() ?: return
                val to = params["to"]?.toIntOrNull() ?: return
                reorderList(binding, from, to)
            }
        }
    }

    /**
     * Mark a form as completed and save state
     */
    private fun handleFormCompleted(formId: String, formData: String? = null) {
        formStateRepository.markFormCompleted(formId, formData)

        val completionStatus = formStateRepository.getFormCompletionStatus()
        setState {
            copy(
                isForm1Completed = completionStatus.isForm1Completed,
                isForm2Completed = completionStatus.isForm2Completed,
                isForm3Completed = completionStatus.isForm3Completed,
                isForm4Completed = completionStatus.isForm4Completed,
                currentFormId = formId
            )
        }
    }

    /**
     * Check if forms 1 & 2 are complete, then auto-navigate to form 3
     */
    private suspend fun checkAndNavigateToNextForm() {
        val completionStatus = formStateRepository.getFormCompletionStatus()

        // If forms 1 and 2 are complete, auto-navigate to form 3
        if (completionStatus.canProceedToForm3()) {
            setState { copy(currentFormId = Routes.BANKING_FINENCIAL_DETAIL) }
            setEffect(BankingPageEffect.AutoNavigate(Routes.BANKING_FINENCIAL_DETAIL))
            loadScreen("financial_information") // Load form 3
        }
    }

    /**
     * Resume from where the user left off (Choosing a random incomplete form)
     */
    private suspend fun resumeFromSavedState() {
        val completionStatus = formStateRepository.getFormCompletionStatus()

        setState {
            copy(
                isForm1Completed = completionStatus.isForm1Completed,
                isForm2Completed = completionStatus.isForm2Completed,
                isForm3Completed = completionStatus.isForm3Completed,
                isForm4Completed = completionStatus.isForm4Completed
            )
        }

        // Navigate to a random incomplete form
        val formToResume = formStateRepository.getRandomIncompleteForm()

        when (formToResume) {
            Routes.BANKING -> loadScreen("personal_details")
            Routes.BANKING_ADDRESS -> loadScreen("address_details")
            Routes.BANKING_FINENCIAL_DETAIL -> {
                setState { copy(currentFormId = Routes.BANKING_FINENCIAL_DETAIL) }
                loadScreen("financial_information")
            }
            Routes.BANKING_REVIEW_SUBMIT -> {
                setState { copy(currentFormId = Routes.BANKING_REVIEW_SUBMIT) }
                loadScreen("review_submit")
            }
        }

        setEffect(BankingPageEffect.AutoNavigate(formToResume))
    }

    // ─── Order ───────────────────────────────────────────────────────────────

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
