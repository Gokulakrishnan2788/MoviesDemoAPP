package com.example.moviesdemoapp.feature.banking.ui

import com.example.moviesdemoapp.core.domain.BaseViewModel
import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.banking.domain.usecase.GetBankingHomeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BankingViewModel @Inject constructor(
    private val getBankingHome: GetBankingHomeUseCase,
) : BaseViewModel<BankingState, BankingIntent, BankingEffect>() {

    override fun initialState() = BankingState()

    init {
        handleIntent(BankingIntent.LoadHome)
    }

    override suspend fun reduce(intent: BankingIntent) {
        when (intent) {
            is BankingIntent.LoadHome -> loadHome()
        }
    }

    private suspend fun loadHome() {
        setState { copy(isLoading = true, error = null) }
        when (val result = getBankingHome()) {
            is Result.Success -> setState { copy(isLoading = false, home = result.data) }
            is Result.Error   -> setState { copy(isLoading = false, error = result.message) }
            is Result.Loading -> Unit
        }
    }
}
