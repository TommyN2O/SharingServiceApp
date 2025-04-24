package com.example.sharingserviceapp.models

data class MyTask(
    val name: String,
    val surname: String,
    val category: String,
    val availability: String,
    val city: String,
    val status: String,
    val categoryImageRes: String, // e.g., R.drawable.plumbing_icon
    val taskerName: String?, // Nullable if not yet assigned
    val taskerSurname: String?
)
