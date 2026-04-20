package com.example.moviesdemoapp.feature.banking.domain.model

data class Transaction(
    val id: String,
    val label: String,
    val amount: String,
    val date: String,
)

data class BankingHome(
    val balance: String,
    val currency: String,
    val transactions: List<Transaction>,
)
