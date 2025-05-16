package com.example.sharingserviceapp.models

data class OpenTaskBody(
    val description: String,
    val location_id: Int,
    val budget: Double,
    val category_id: Int,
    val duration: Int,
    val availability: List<AvailabilitySlot>

)
