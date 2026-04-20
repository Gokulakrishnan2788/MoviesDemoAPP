package com.example.moviesdemoapp.feature.banking.domain.repository

import com.example.moviesdemoapp.core.domain.Result
import com.example.moviesdemoapp.feature.banking.domain.model.BankingHome

interface BankingRepository {
    suspend fun getBankingHome(): Result<BankingHome>
}
