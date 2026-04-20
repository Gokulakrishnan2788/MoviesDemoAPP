package com.example.moviesdemoapp.feature.banking.domain.usecase

import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.banking.domain.model.BankingHome
import com.example.moviesdemoapp.feature.banking.domain.repository.BankingRepository

class GetBankingHomeUseCase(private val repository: BankingRepository) {
    suspend operator fun invoke(): Result<BankingHome> = repository.getBankingHome()
}
