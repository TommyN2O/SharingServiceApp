package com.example.sharingserviceapp.models

data class UserChat(
    val id: Int,
    val name: String,
    val surname: String,
    val profile_photo: String?,
    val isRequester: Boolean
)
