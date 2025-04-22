package com.example.sharingserviceapp.models

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val status: String
)