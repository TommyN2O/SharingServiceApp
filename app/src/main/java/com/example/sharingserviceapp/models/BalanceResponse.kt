package com.example.sharingserviceapp.models

data class BalanceResponse(
    val balance: Double,
    val transactions: List<Transaction>
)
