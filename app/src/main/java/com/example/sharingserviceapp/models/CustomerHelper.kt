package com.example.sharingserviceapp.models

import android.os.Parcel
import android.os.Parcelable

data class CustomerHelper(
    val name: String,
    val rating: Double,
    val reviewCount: Int,
    val shortDescription: String,
    val profileImage: Int,
    val price: Int,
    val availableCities: List<String>, // List of cities they serve
    val availableTimes: List<String>,  // Available time slots
    val galleryImages: List<Int>,      // Gallery images
    val categories: List<Int>          // Categories they belong to
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.createStringArrayList()!!,
        parcel.createStringArrayList()!!,
        parcel.createIntArray()?.toList() ?: emptyList(),
        parcel.createIntArray()?.toList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeDouble(rating)
        parcel.writeInt(reviewCount)
        parcel.writeString(shortDescription)
        parcel.writeInt(profileImage)
        parcel.writeInt(price)
        parcel.writeStringList(availableCities)
        parcel.writeStringList(availableTimes)
        parcel.writeIntArray(galleryImages.toIntArray())
        parcel.writeIntArray(categories.toIntArray())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CustomerHelper> {
        override fun createFromParcel(parcel: Parcel): CustomerHelper = CustomerHelper(parcel)
        override fun newArray(size: Int): Array<CustomerHelper?> = arrayOfNulls(size)
    }
}
