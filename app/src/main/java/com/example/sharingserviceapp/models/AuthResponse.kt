package com.example.sharingserviceapp.models

import java.io.Serializable

data class AuthResponse(
    val token: String,
    val user: UserProfileResponse
) : Serializable
