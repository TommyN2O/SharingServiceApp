package com.example.sharingserviceapp.models

import java.io.Serializable

data class RegisterRequest(
    val name: String,
    val surname: String,
    val email: String,
    val password: String,
    val dateOfBirth: String
) : Serializable





