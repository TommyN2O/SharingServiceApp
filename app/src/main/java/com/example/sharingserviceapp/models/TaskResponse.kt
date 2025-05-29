package com.example.sharingserviceapp.models

data class TaskResponse(
    val id: Int,
    val description: String,
    val city: City,
    val categories: List<Category>,
    val duration: String,
    val availability: List<AvailabilitySlot>,
    val sender: User,
    val tasker: TaskerTaskResponseBody?,
    val hourly_rate: Double,
    val gallery: List<String>,
    val status: String,
    val created_at: String,
    val budget: Double?,
    val is_open_task: Boolean?
)

