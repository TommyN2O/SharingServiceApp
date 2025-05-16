package com.example.sharingserviceapp.models

data class SetAsOpenTask(
    val budget: Double,
    val availability: List<AvailabilitySlot>
)
