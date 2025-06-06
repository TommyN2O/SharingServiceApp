package com.example.sharingserviceapp.models

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val id: Int,
    val name: String,
    @SerializedName("image_url") val image: String,
    val description: String,
    @SerializedName("created_at") val createdAt: String
) : Parcelable