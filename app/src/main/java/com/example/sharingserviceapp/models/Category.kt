package com.example.sharingserviceapp.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Category(
    val id: Int,
    val name: String,
    @SerializedName("image_url") val image: String,
    val description: String,
    @SerializedName("created_at") val createdAt: String
)