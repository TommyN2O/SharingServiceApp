package com.example.sharingserviceapp.models

data class CreateTicketResponse(
    val message: String,
    val ticket: SupportTicketResponse
)
