package com.example.sharingserviceapp.models

data class Message(
    val chatId: Int,
    val lastMessageId: Int,
    val lastMessage: String,
    val lastMessageTime: String,
    val otherUser: User
)

