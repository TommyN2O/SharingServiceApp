package com.example.sharingserviceapp.models

data class OpenedTasksHelper(
    val id: Int,
    val description: String,
    val city: City,
    val budget: Double,
    val category: Category,
    val creator:User,
    val duration: Int,
    val availability: List<AvailabilitySlot>,
    val gallery_images: List<String>

)



