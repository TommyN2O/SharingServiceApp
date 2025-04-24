package com.example.sharingserviceapp.models

data class TaskResponse(
    val id: Int,
    val description: String,
    val city: City,
    val categories: List<Category>,
    val duration: String,
    val availability: List<AvailabilitySlot>,
    val sender: User,
    val tasker: User?,
    val gallery: List<String>,// nullable in case not chosen
    val status: String,
    val created_at: String
)

