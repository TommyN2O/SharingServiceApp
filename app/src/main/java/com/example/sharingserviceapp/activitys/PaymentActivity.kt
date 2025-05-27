package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Balance
import com.example.sharingserviceapp.models.Payment
import com.example.sharingserviceapp.models.PaymentResponse
import com.example.sharingserviceapp.models.TaskResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.jvm.java

class PaymentActivity : AppCompatActivity() {

    private lateinit var confirmButton: Button
    private lateinit var paymentMethodGroup: RadioGroup
    private lateinit var walletRadio: RadioButton
    private lateinit var cardRadio: RadioButton
    private lateinit var balanceTextView: TextView
    private var taskId: Int = -1
    private var total: Double = -1.0
    private var balance: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment)
        confirmButton = findViewById(R.id.confirmButton)
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup)
        walletRadio = findViewById(R.id.walletRadio)
        cardRadio = findViewById(R.id.cardRadio)
        balanceTextView=findViewById(R.id.balanceTextView)

        val intent = intent
        val action = intent.action
        val data = intent.data

        taskId = intent.getIntExtra("TASK_ID", -1)

        if (Intent.ACTION_VIEW == action && data != null) {
            if (data.scheme == "sharingapp" && data.host == "payment-cancel") {
                taskId = data.getQueryParameter("task_id")?.toIntOrNull()!!
                Toast.makeText(this, getString(R.string.payment_canceled), Toast.LENGTH_LONG).show()
            }
        }

        if (taskId == -1) {
            Toast.makeText(this, getString(R.string.error_invalid_taskId), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        usersBalance()
        setUpBackButton()
        setUpConfirmButton()
        loadPaymentDetailed(taskId)
    }

    private fun setUpBackButton() {
        findViewById<ImageView>(R.id.backArrowButton).setOnClickListener {
            val intent = Intent(this, TaskDetailActivity::class.java).apply {
                putExtra("TASK_ID", taskId)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setUpConfirmButton() {
        paymentMethodGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.walletRadio -> {
                    val balanceAmount = balance ?: 0.0
                    balanceTextView.visibility = View.VISIBLE
                        val fullText = "Skaitmeninės piniginės likutis: ${String.format("%.2f", balanceAmount)}€"
                        val start = fullText.indexOf(":") + 2
                        val end = fullText.length

                        val spannable = SpannableString(fullText)
                        spannable.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(this,android.R.color.holo_green_dark)),
                            start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        balanceTextView.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
                        balanceTextView.text = spannable
                }
                else -> {
                    balanceTextView.visibility = View.GONE
                }
            }
        }
        confirmButton.setOnClickListener {
            val selectedId = paymentMethodGroup.checkedRadioButtonId
            val (displayText, backendValue) = when (selectedId) {
                R.id.walletRadio -> "Skaitmeninė piniginė" to "Wallet"
                R.id.cardRadio -> "Kortelė" to "Card"
                else -> null to null
            }
            if (backendValue == null) {
                Toast.makeText(this, getString(R.string.payment_select_payment_method), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (backendValue == "Wallet") {
                val balanceAmount = balance ?: 0.0
                if (balanceAmount < total) {
                    Toast.makeText(this, getString(R.string.payment_select_payment_wallet_not_enough), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            Toast.makeText(this, "Mokėjimas patvirtintas naudojant: $displayText", Toast.LENGTH_SHORT).show()
            goToPayment(taskId,total, backendValue)
        }
    }

    private fun goToPayment(task_id: Int,amount: Double, type: String){
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val api = ApiServiceInstance.Auth.apiServices
        val body= Payment(amount,task_id,type)
        val call = api.payment("Bearer $token", body)

        call.enqueue(object : Callback<PaymentResponse> {
            override fun onResponse(call: Call<PaymentResponse>, response: Response<PaymentResponse>) {
                if (response.isSuccessful ) {
                    if (type == "Card") {
                        val paymentUrl = response.body()!!.url
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(paymentUrl))
                        startActivity(intent)
                    }
                    else {
                        Toast.makeText(this@PaymentActivity, getString(R.string.payment_select_payment_wallet_completed), Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@PaymentActivity, PlannedTasksActivity::class.java)
                        startActivity(intent)
                    }
                    finish()
                } else {
                    if (type == "Card") {
                        Toast.makeText(this@PaymentActivity, getString(R.string.payment_select_failed_load_by_card), Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this@PaymentActivity, getString(R.string.payment_select_failed_load_by_wallet), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                Toast.makeText(this@PaymentActivity, "Error: ${t.message}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun usersBalance() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val api = ApiServiceInstance.Auth.apiServices
        val call = api.usersBalance("Bearer $token")
        call.enqueue(object : Callback<Balance> {
            override fun onResponse(call: Call<Balance>, response: Response<Balance>) {
                if (response.isSuccessful && response.body() != null) {
                    balance = response.body()!!.balance
                } else {
                    Toast.makeText(this@PaymentActivity, getString(R.string.payment_select_failed_load_balance), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Balance>, t: Throwable) {
                Toast.makeText(this@PaymentActivity, "Error: ${t.message}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun loadPaymentDetailed(taskId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getMyTasksById(
            "Bearer $token",
            taskId
        )
        call.enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showRequestDetailed(response.body()!!)
                } else {
                    Toast.makeText(this@PaymentActivity, getString(R.string.payment_select_failed_load_payment_info), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Toast.makeText(this@PaymentActivity, "Error: ${t.message}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun showRequestDetailed(task: TaskResponse) {
        val taskCategory: TextView = findViewById(R.id.taskTitle)
        val taskDateTime: TextView = findViewById(R.id.taskDateTime)
        val taskDuration: TextView = findViewById(R.id.taskDuration)
        val taskPrice: TextView = findViewById(R.id.hourlyRate)
        val taskerProfileImage: ImageView = findViewById(R.id.taskerProfileImage)
        val taskerName: TextView = findViewById(R.id.taskerName)
        val supportFee: TextView = findViewById(R.id.supportFee)
        val totalRate: TextView = findViewById(R.id.totalRate)
        val paymentNotice: TextView = findViewById(R.id.paymentNotice)

        taskCategory.text = task.categories.joinToString { it.name }
        val slot = task.availability.firstOrNull()
        slot?.let {
            try {
                val lithuanianLocale = Locale("lt")

                val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", lithuanianLocale)
                val inputTimeFormat = SimpleDateFormat("HH:mm:ss", lithuanianLocale)
                val dayFormat = SimpleDateFormat("EEEE", lithuanianLocale)
                val monthFormat = SimpleDateFormat("MMMM", lithuanianLocale)
                val dayNumYearFormat = SimpleDateFormat("d, yyyy", lithuanianLocale)
                val outputTimeFormat = SimpleDateFormat("HH:mm", lithuanianLocale)
                val date = inputDateFormat.parse(it.date)
                val time = inputTimeFormat.parse(it.time)
                val day = dayFormat.format(date).replaceFirstChar { it.titlecase(lithuanianLocale) }
                val month = monthFormat.format(date).replaceFirstChar { it.titlecase(lithuanianLocale) }
                val dayNumYear = dayNumYearFormat.format(date)
                val formattedDate = "$day - $month $dayNumYear"
                val formattedTime = outputTimeFormat.format(time)

                taskDateTime.text = "$formattedDate $formattedTime"
            } catch (e: Exception) {
                taskDateTime.text = "Data ir laikas: neteisingas formatas"
            }
        }

        taskerName.text = "${task.tasker?.name?.replaceFirstChar { it.uppercase() }} ${
            task.tasker?.surname?.firstOrNull()?.uppercaseChar() ?: ""
        }."

        val taskerImageUrl = task.tasker?.profile_photo?.let {
            URL(URL(ApiServiceInstance.BASE_URL), it)
        } ?: R.drawable.placeholder_image_user

        Glide.with(this)
            .load(taskerImageUrl)
            .placeholder(R.drawable.placeholder_image_user)
            .error(R.drawable.error)
            .circleCrop()
            .into(taskerProfileImage)

        taskDuration.text = "${task.duration} val."
        val durationHours = task.duration.replace(" val.", "").trim().toDoubleOrNull() ?: 0.0

        val hourlyRate = task.tasker?.hourly_rate ?: 0.0
        taskPrice.text = "${hourlyRate}€/val."

        val supportFeeValue = 2.50
        supportFee.text = "$supportFeeValue€/val."

        total = (hourlyRate * durationHours) + (supportFeeValue * durationHours)
        totalRate.text = "Galutinė kaina: ${"%.2f".format(total)}€"
        paymentNotice.text = "Jūsų mokėjimo būde gali būti matomas laikinas rezervavimas sumai ${"%.2f".format(total)}€..."
    }
}
