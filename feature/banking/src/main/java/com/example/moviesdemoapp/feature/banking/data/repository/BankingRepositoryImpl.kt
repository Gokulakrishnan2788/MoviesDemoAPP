package com.example.moviesdemoapp.feature.banking.data.repository

import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.core.network.BankingApiService
import com.example.moviesdemoapp.feature.banking.domain.model.BankingHome
import com.example.moviesdemoapp.feature.banking.domain.model.Transaction
import com.example.moviesdemoapp.feature.banking.domain.repository.BankingRepository
import javax.inject.Inject

class BankingRepositoryImpl @Inject constructor(
    private val api: BankingApiService,
) : BankingRepository {

    override suspend fun getBankingHome(): Result<BankingHome> = runCatching {
        val dto = api.getBankingHome()
        BankingHome(
            balance = dto.balance,
            currency = dto.currency,
            transactions = dto.transactions.map { t ->
                Transaction(id = t.id, label = t.label, amount = t.amount, date = t.date)
            },
        )
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(message = it.message ?: "Network error") },
    )
}
