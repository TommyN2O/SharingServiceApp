package com.example.sharingserviceapp.models

data class CustumerHelper(
    val name: String,
    val profileImage: Int,
    val city: String,
    val description: String,
    val budget: Int,          // Estimated budget for task
    val dueDate: String,      // Due date for completion
    val category: Int,        // Task category
    val galleryImages: List<Int>, // Images related to the task

)



