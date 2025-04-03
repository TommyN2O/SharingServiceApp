package com.example.sharingserviceapp.models

import java.io.Serializable

data class Message(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val message: String,
    val createdAt: String
) : Serializable

