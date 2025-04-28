package com.example.sharingserviceapp.models

data class TaskerTaskResponseBody(
    val id: Int,
    val profile_photo: String?,
    val name: String?,
    val surname: String?,
    val hourly_rate: Double?
)
