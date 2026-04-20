package com.example.moviesdemoapp.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BankingHomeDto(
    @SerialName("balance") val balance: String = "",
    @SerialName("currency") val currency: String = "",
    @SerialName("transactions") val transactions: List<TransactionDto> = emptyList(),
)

@Serializable
data class TransactionDto(
    @SerialName("id") val id: String = "",
    @SerialName("label") val label: String = "",
    @SerialName("amount") val amount: String = "",
    @SerialName("date") val date: String = "",
)
