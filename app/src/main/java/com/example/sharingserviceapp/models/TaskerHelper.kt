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
    val cities: List<City>,
    val availableTimes: List<String>,
    val profile_photo: String?,
    val gallery: List<String> ,
    val categories: List<Category>
): Parcelable

