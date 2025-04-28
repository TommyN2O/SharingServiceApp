package com.example.sharingserviceapp.models

data class SendMessage(
    val chatId: Int,
    val receiverId: Int,
    val message: String
)

