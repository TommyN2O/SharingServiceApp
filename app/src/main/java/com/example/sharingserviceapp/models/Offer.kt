package com.example.sharingserviceapp.models

import java.io.Serializable
import java.time.Duration

data class Offer(
    val id: Int,
    val taskId: Int,
    val tasker: User,
    val description: String,
    val price: Double,
    val duration: Int,
    val availability: AvailabilitySlot,
    val status: String
) : Serializable
