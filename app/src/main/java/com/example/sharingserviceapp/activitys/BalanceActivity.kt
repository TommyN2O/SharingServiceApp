package com.example.sharingserviceapp.activitys

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.BalanceAdapter
import com.example.sharingserviceapp.network.ApiServiceInstance
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharingserviceapp.models.PayoutRequests
import com.example.sharingserviceapp.models.WalletResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BalanceActivity : AppCompatActivity() {

    private lateinit var recyclerEarnings: RecyclerView
    private lateinit var textBalanceAmount: TextView
    private var iban: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)

        recyclerEarnings = findViewById(R.id.recycler_earnings)
        textBalanceAmount = findViewById(R.id.text_balance_amount)
        recyclerEarnings.layoutManager = LinearLayoutManager(this)
        fetchWalletPayments()
        setupListeners()
    }
    private fun setupListeners() {
        findViewById<ImageView>(R.id.button_back).setOnClickListener {
            startActivity(Intent(this, MoreActivity::class.java))
            finish()
        }
        findViewById<ImageView>(R.id.cash_out).setOnClickListener {
            if(iban==null){
                AlertDialog.Builder(this@BalanceActivity)
                    .setTitle(getString(R.string.payments_payout_request_try_title))
                    .setMessage(getString(R.string.payments_payout_request_try_text))
                    .setPositiveButton("Gerai", null)
                    .show()
            }else{
                setupPayout()
            }
        }
    }

    private fun setupPayout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.payments_payout_title))

        val container = FrameLayout(this)
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setBackgroundResource(R.drawable.rounded_edittext)

        val paddingInPx = (12 * resources.displayMetrics.density).toInt()
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        input.layoutParams = layoutParams
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Patvirtinti", null)
        builder.setNegativeButton("Atšaukti") { dialog, _ -> dialog.cancel() }

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val amountText = input.text.toString().trim()
            if (amountText.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.payments_payout_error_empty),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val amount = amountText.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(
                        this,
                        getString(R.string.payments_payout_error_amount),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Išmokėjimo suma: $amount", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    sendPayoutAmount(amount)
                }
            }
        }
    }


    private fun sendPayoutAmount(amount:Double){
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val body = PayoutRequests(amount = amount)
        val api = ApiServiceInstance.Auth.apiServices
        val call = api.postPayoutRequests("Bearer $token",body)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    AlertDialog.Builder(this@BalanceActivity)
                        .setTitle(getString(R.string.payments_payout_request_accepted))
                        .setMessage(getString(R.string.payments_payout_request_accepted_text))
                        .setPositiveButton("Gerai", null)
                        .show()
                } else {
                    Toast.makeText(this@BalanceActivity, getString(R.string.payments_payout_request_sent), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@BalanceActivity, getString(R.string.profile_details_network_error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchWalletPayments() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getWalletPayments("Bearer $token")

        call.enqueue(object : Callback<WalletResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<WalletResponse>, response: Response<WalletResponse>) {
                    if (response.isSuccessful) {
                        val balanceData = response.body()
                        balanceData?.let {
                            textBalanceAmount.text = "${it.walletAmount} EUR"
                            iban = {it.wallet_bank_iban}.toString()
                            recyclerEarnings.adapter = BalanceAdapter(it.payments)
                        }
                    } else {
                        Toast.makeText(this@BalanceActivity, getString(R.string.payments_error_fetch), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<WalletResponse>, t: Throwable) {
                    Toast.makeText(this@BalanceActivity, getString(R.string.profile_details_network_error), Toast.LENGTH_SHORT).show()
                }
            })
    }
}
