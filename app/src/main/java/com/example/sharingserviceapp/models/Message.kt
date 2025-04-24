package com.example.sharingserviceapp.models

data class Message(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val message: String,
    val createdAt: String
)

