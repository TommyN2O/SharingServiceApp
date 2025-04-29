package com.example.sharingserviceapp.activitys

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.BalanceAdapter
import com.example.sharingserviceapp.models.BalanceResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharingserviceapp.models.WalletResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BalanceActivity : AppCompatActivity() {

    private lateinit var recyclerEarnings: RecyclerView
    private lateinit var textBalanceAmount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)

        recyclerEarnings = findViewById(R.id.recycler_earnings)
        textBalanceAmount = findViewById(R.id.text_balance_amount)

        recyclerEarnings.layoutManager = LinearLayoutManager(this)

        setupBackButton()
        fetchWalletPayments()
    }
    private fun setupBackButton() {
        findViewById<ImageView>(R.id.button_back).setOnClickListener {
            finish()
        }
    }
    private fun fetchWalletPayments() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getWalletPayments("Bearer $token")

        call.enqueue(object : Callback<WalletResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<WalletResponse>, response: Response<WalletResponse>) {
                    if (response.isSuccessful) {
                        val balanceData = response.body()
                        balanceData?.let {
                            textBalanceAmount.text = "${it.walletAmount / 100.00} EUR"
                            recyclerEarnings.adapter = BalanceAdapter(it.payments)
                        }
                    } else {
                        Toast.makeText(this@BalanceActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<WalletResponse>, t: Throwable) {
                    Toast.makeText(this@BalanceActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
