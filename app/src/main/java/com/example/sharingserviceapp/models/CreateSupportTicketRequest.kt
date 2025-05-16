package com.example.sharingserviceapp.models

data class CreateSupportTicketRequest(
    val type: String,
    val content: String
)