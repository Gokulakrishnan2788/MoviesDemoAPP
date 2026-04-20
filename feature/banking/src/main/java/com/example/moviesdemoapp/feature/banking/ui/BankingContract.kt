package com.example.moviesdemoapp.feature.banking.ui

import com.example.moviesdemoapp.core.domain.UiEffect
import com.example.moviesdemoapp.core.domain.UiIntent
import com.example.moviesdemoapp.core.domain.UiState
import com.example.moviesdemoapp.feature.banking.domain.model.BankingHome

data class BankingState(
    val home: BankingHome? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : UiState

sealed interface BankingIntent : UiIntent {
    data object LoadHome : BankingIntent
}

sealed interface BankingEffect : UiEffect
