package com.example.sharingserviceapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AvailabilitySlot(
    val date: String,
    val time: String
) : Parcelable