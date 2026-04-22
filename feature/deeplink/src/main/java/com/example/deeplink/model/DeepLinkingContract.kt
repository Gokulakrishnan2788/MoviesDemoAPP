package com.example.deeplink.model

import com.example.moviesdemoapp.core.domain.UiEffect
import com.example.moviesdemoapp.core.domain.UiIntent
import com.example.moviesdemoapp.core.domain.UiState
import com.example.moviesdemoapp.core.network.model.ScreenModel


data class DeepLinkPageState(
    val screenModel: ScreenModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val dataMap: Map<String, String> = emptyMap(),
    val listData: Map<String, List<Map<String, String>>> = emptyMap(),
) : UiState

sealed interface DeepLinkPageIntent : UiIntent {
    data object LoadPersonalDetailMainPage : DeepLinkPageIntent
    data class LoadOtherMainPage(val pageDetail:String) : DeepLinkPageIntent
    data class OnAction(val actionId: String, val params: Map<String, String>) : DeepLinkPageIntent
}

sealed interface DeepLinkPageEffect : UiEffect {
    data class Navigate(val route: String) : DeepLinkPageEffect
}
