package com.example.sharingserviceapp.models

import com.example.sharingserviceapp.network.ApiService
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class TaskerProfileRequest(
    val name: String,
    val surname: String,
    val description: String,
    val hourly_rate: Double,
    val categories: List<Category>,
    val cities: List<City>,
    val availability: List<AvailabilitySlot>,
    val deletedGalleryImages: List<String>? = null
//    val profile_photo: String?, // If photo is optional
//    val gallery: List<MultipartBody.Part>
)
