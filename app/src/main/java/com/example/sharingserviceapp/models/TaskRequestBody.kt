package com.example.sharingserviceapp.models

data class TaskRequestBody(
    val description: String,
    val city: City,
    val categories: List<Category>,
    val duration: Int,
    val availability: List<AvailabilitySlot>,
    val sender_id: Int,
    val tasker_id: Int
)

