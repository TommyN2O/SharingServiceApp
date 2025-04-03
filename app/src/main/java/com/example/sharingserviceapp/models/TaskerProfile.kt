package com.example.sharingserviceapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


data class TaskerProfile(
    val profile_photo: String?,
    val description: String,
    val hourly_rate: Double,
    val categories: List<Int>,
    val cities: List<Int>,
    val availability: List<AvailabilitySlot>
)

