package com.example.sharingserviceapp.models

data class Transaction(
    val id: Int,
    val sender: User,
    val category: String,
    val date: String,
    val status: String,
    val amount: Double
)
