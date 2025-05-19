package com.example.sharingserviceapp.models

import java.io.Serializable
import com.google.gson.annotations.SerializedName

data class WalletPaymentsResponse(
    @SerializedName("payment_id") val paymentId: Int,
    @SerializedName("amount") val amount: Double,
    @SerializedName("payment_date") val paymentDate: String,
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("transaction_type") val type: String,
    @SerializedName("task") val task: Task,
    @SerializedName("other_party") val otherParty: Sender
) : Serializable