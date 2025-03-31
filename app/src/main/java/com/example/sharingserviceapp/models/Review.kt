package com.example.sharingserviceapp.models

data class Review(
    val reviewerName: String,
    val reviewerImage: Int, // This should be a drawable resource ID or URL
    val rating: Double,
    val date: String,
    val reviewText: String
)
