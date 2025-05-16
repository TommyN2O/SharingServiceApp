package com.example.sharingserviceapp.models

import java.time.Duration

data class OpenTaskOffer(
    val description: String,
    val price: Double,
    val availability: AvailabilitySlot,
    val duration: Int
)
