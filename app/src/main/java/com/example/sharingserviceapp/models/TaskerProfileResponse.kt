package com.example.sharingserviceapp.models

data class TaskerProfileResponse(
    val id: Int,
    val name: String,
    val surname: String,
    val description: String,
    val hourly_rate: Double,
    val categories: List<Category>,
    val cities: List<City>,
    val availability: List<AvailabilitySlot>,
    val rating: Double,
    val review_count: Int,
    val profile_photo: String?,
    val gallery: List<String>
)