package com.example.sharingserviceapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class City(
    val id: Int,
    val name: String,
    @SerializedName("created_at") val createdAt: String
): Parcelable
