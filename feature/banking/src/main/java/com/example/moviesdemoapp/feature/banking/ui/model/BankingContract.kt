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
) : UiState

sealed interface BankingPageIntent : UiIntent {
    data object LoadPersonalDetailMainPage : BankingPageIntent
    data class LoadOtherMainPage(val pageDetail:String) : BankingPageIntent
    data class OnAction(val actionId: String, val params: Map<String, String>) : BankingPageIntent
}

sealed interface BankingPageEffect : UiEffect {
    data class Navigate(val route: String) : BankingPageEffect
}
