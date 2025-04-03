package com.example.sharingserviceapp.models

import java.io.Serializable

data class LoginRequest(
    val email: String,
    val password: String
) : Serializable