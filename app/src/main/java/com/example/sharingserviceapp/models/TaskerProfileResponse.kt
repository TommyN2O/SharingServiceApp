package com.example.sharingserviceapp.models

import okhttp3.MultipartBody

data class TaskerProfileResponse(
    val user_id: Int,
    val name: String,
    val surname: String,
    val description: String,
    val hourly_rate: Double,
    val categories: List<Category>,
    val cities: List<City>,
    val availability: List<AvailabilitySlot>,
    val rating: Double,
    val reviewCount: Int,
    val profile_photo: String?,
    val gallery: List<String>
)