package com.example.sharingserviceapp.models

data class OpenTask(
    val id: Int,
    val description: String,
    val budget: Double?,
    val duration: String,
    val city: City,
    val category: Category,
    val creator: User,
    val availability: List<AvailabilitySlot>,
    val gallery: List<String>,
    val status: String,
    val created_at: String
)
