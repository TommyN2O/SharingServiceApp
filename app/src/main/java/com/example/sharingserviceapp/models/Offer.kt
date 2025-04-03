package com.example.sharingserviceapp.models

import java.io.Serializable

data class Offer(
    val id: Int,
    val taskId: Int,
    val taskerId: Int,
    val description: String,
    val price: Double,
    val suggestDate: String,
    val suggestTime: String,
    val status: String
) : Serializable
