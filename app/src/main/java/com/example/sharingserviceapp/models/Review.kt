package com.example.sharingserviceapp.models

data class Review(
    val reviewerName: String,
    val reviewerImage: Int,
    val rating: Double,
    val date: String,
    val reviewText: String
)
