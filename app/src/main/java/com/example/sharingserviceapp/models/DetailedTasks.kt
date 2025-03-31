package com.example.sharingserviceapp.models

data class DetailedTasks(
    val customerName: String,
    val taskDescription: String,
    val taskLocation: String,
    val taskDate: String,
    val taskTime: String,
    val taskImages: List<Int>,  // List of drawable resource IDs
    val taskerName: String,
    val customerProfileImage: Int,
    val taskerProfileImage: Int
)
