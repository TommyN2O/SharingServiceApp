package com.example.sharingserviceapp.models

data class CreateChat(
    val chatId: Int,
    val user1: UserChat,
    val user2: UserChat
)
