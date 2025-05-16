package com.example.sharingserviceapp.models

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
