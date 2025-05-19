package com.example.sharingserviceapp.models

data class ReviewList(
    val id: Int,
    val rating: Int,
    val review: String,
    val created_at: String,
    val reviewer: User
)
