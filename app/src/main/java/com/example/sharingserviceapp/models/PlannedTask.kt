package com.example.sharingserviceapp.models

import java.io.Serializable

data class PlannedTask(
    val id: Int,
    val task: Task1,
    val customer: UserProfileResponse,
    val tasker: TaskerProfile,
    val acceptedByCustomer: Boolean,
    val acceptedByTasker: Boolean,
    val isCompleted: Boolean
) : Serializable

