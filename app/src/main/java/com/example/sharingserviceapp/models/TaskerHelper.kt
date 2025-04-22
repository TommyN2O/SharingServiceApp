package com.example.sharingserviceapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskerHelper(
    val id: Int,
    val name: String,
    val surname: String,
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val hourly_rate: Double,
    val availability: List<AvailabilitySlot>,
    val cities: List<City>, // List of cities they serve
    val availableTimes: List<String>,  // Available time slots
    val profile_photo: String?, // ✅ URL to the image
    val gallery: List<String> , // Gallery images
    val categories: List<Category>          // Categories they belong to
): Parcelable

