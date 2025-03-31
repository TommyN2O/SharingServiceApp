package com.example.sharingserviceapp.models

import android.os.Parcel
import android.os.Parcelable

data class Helper(
    val name: String,
    val rating: Double,
    val reviews: Int,
    val shortDescription: String,
    val profileImageResId: Int,
    val categories: List<Int>, // List of category image resource IDs (for Taskers)
    val city: String,
    val price: Int, // Price per hour or task budget
    val availableCities: List<String>, // List of cities they serve
    val availableTimes: List<String>, // Time slots
    val isTasker: Boolean, // Flag indicating if the helper is a tasker
    val galleryImages: List<Int> // Added: List of gallery image resource IDs
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.createIntArray()?.toList() ?: emptyList(), // Read categories
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.createStringArrayList() ?: emptyList(), // Read availableCities
        parcel.createStringArrayList() ?: emptyList(), // Read availableTimes
        parcel.readByte() != 0.toByte(), // Read isTasker
        parcel.createIntArray()?.toList() ?: emptyList() // Read galleryImages
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeDouble(rating)
        parcel.writeInt(reviews)
        parcel.writeString(shortDescription)
        parcel.writeInt(profileImageResId)
        parcel.writeIntArray(categories.toIntArray()) // Write categories
        parcel.writeString(city)
        parcel.writeInt(price)
        parcel.writeStringList(availableCities) // Write availableCities
        parcel.writeStringList(availableTimes) // Write availableTimes
        parcel.writeByte(if (isTasker) 1 else 0) // Write isTasker
        parcel.writeIntArray(galleryImages.toIntArray()) // Write galleryImages
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Helper> {
        override fun createFromParcel(parcel: Parcel): Helper {
            return Helper(parcel)
        }

        override fun newArray(size: Int): Array<Helper?> {
            return arrayOfNulls(size)
        }
    }
}
