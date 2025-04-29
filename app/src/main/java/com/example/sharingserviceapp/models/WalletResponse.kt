package com.example.sharingserviceapp.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WalletResponse(
    @SerializedName("wallet_amount") val walletAmount: Int,
    @SerializedName("transactions") val payments: List<WalletPaymentsResponse>
) : Serializable