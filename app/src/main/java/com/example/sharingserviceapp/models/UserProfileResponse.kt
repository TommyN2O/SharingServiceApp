package com.example.sharingserviceapp.models

data class UserProfileResponse(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val date_of_birth: String,
    val is_tasker: Boolean
)
