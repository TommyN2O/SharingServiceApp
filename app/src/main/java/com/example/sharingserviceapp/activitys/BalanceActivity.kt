package com.example.sharingserviceapp.activitys

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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BalanceActivity : AppCompatActivity() {

    private lateinit var recyclerEarnings: RecyclerView
    private lateinit var textBalanceAmount: TextView
    private lateinit var buttonBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)

        recyclerEarnings = findViewById(R.id.recycler_earnings)
        textBalanceAmount = findViewById(R.id.text_balance_amount)
        buttonBack = findViewById(R.id.button_back)

        recyclerEarnings.layoutManager = LinearLayoutManager(this)

        setupBackButton()
        fetchBalanceData()
    }
    private fun setupBackButton() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun fetchBalanceData() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getBalanceData("Bearer $token")

        call.enqueue(object : Callback<BalanceResponse> {
                override fun onResponse(call: Call<BalanceResponse>, response: Response<BalanceResponse>) {
                    if (response.isSuccessful) {
                        val balanceData = response.body()
                        balanceData?.let {
                            textBalanceAmount.text = "${it.balance} EUR"
                            recyclerEarnings.adapter = BalanceAdapter(it.transactions)
                        }
                    } else {
                        Toast.makeText(this@BalanceActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                    Toast.makeText(this@BalanceActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
