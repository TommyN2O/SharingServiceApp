// ChatMessage.kt
package com.example.sharingserviceapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMessage(
    val text: String,
    val isSentByUser: Boolean, // True for sent, false for received
    val timestamp: String,
    val senderProfileImageResId: Int? = null // This should be passed as an ID of a drawable resource
) : Parcelable
