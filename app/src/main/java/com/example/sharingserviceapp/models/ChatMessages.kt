package com.example.sharingserviceapp.models


data class ChatMessages(
    val chatId: Int,
    val senderId: Int,
    val receiverId: Int,
    val message: String,
    val createdAt: String,
    val sender: User,
    val receiver: User
)
