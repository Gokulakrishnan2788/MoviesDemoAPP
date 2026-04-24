package com.example.moviesdemoapp.feature.banking.ui.model

import com.example.moviesdemoapp.core.domain.UiEffect
import com.example.moviesdemoapp.core.domain.UiIntent
import com.example.moviesdemoapp.core.domain.UiState
import com.example.moviesdemoapp.core.network.model.ScreenModel


data class BankingPageState(
    val screenModel: ScreenModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val dataMap: Map<String, String> = emptyMap(),
    val listData: Map<String, List<Map<String, String>>> = emptyMap(),
    val currentFormNumber: Int = 1,
    val isForm1Completed: Boolean = false,
    val isForm2Completed: Boolean = false,
    val isForm3Completed: Boolean = false,
    val isForm4Completed: Boolean = false,
) : UiState

sealed interface BankingPageIntent : UiIntent {
    data object LoadPersonalDetailMainPage : BankingPageIntent
    data class LoadOtherMainPage(val pageDetail: String) : BankingPageIntent
    data class OnAction(val actionId: String, val params: Map<String, String>) : BankingPageIntent
    data class MarkFormCompleted(val formNumber: Int, val formData: String? = null) : BankingPageIntent
    data object CheckAndNavigateToNextForm : BankingPageIntent
    data object ResumeFromSavedState : BankingPageIntent
}

sealed interface BankingPageEffect : UiEffect {
    data class Navigate(val route: String) : BankingPageEffect
    data class AutoNavigate(val formNumber: Int) : BankingPageEffect
}
