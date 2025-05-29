package com.example.sharingserviceapp.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WalletResponse(
    @SerializedName("wallet_amount") val walletAmount: Double,
    val wallet_bank_iban: String,
    @SerializedName("transactions") val payments: List<WalletPaymentsResponse>
) : Serializable