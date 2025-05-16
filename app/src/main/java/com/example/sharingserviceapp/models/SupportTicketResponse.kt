package com.example.sharingserviceapp.models

data class SupportTicketResponse(
    val id: Int,
    val sender_id: Int,
    val sender_name: String,
    val sender_surname: String,
    val sender_email: String,
    val type: String,
    val content: String,
    val created_at: String,
    val status: String
)
