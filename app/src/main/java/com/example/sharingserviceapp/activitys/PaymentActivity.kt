package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.TaskDetailActivity
import com.example.sharingserviceapp.adapters.GalleryAdapter
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

class PaymentActivity : AppCompatActivity() {

    private lateinit var confirmButton: Button
    private lateinit var paymentMethodGroup: RadioGroup
    private lateinit var cashRadio: RadioButton
    private lateinit var cardRadio: RadioButton
    private var taskId: Int = -1
    private var total: Double = -1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment)
        confirmButton = findViewById(R.id.confirmButton)
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup)
        cashRadio = findViewById(R.id.cashRadio)
        cardRadio = findViewById(R.id.cardRadio)

        val intent = intent
        val action = intent.action
        val data = intent.data

        taskId = intent.getIntExtra("TASK_ID", -1)

        if (Intent.ACTION_VIEW == action && data != null) {
            if (data.scheme == "sharingapp" && data.host == "payment-cancel") {
                taskId = data.getQueryParameter("task_id")?.toIntOrNull()!!
                Toast.makeText(this, "Payment Canceled!", Toast.LENGTH_LONG).show()
            }
        }

        if (taskId == -1) {
            Toast.makeText(this, "Invalid Task ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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
        confirmButton.setOnClickListener {
            val selectedPaymentMethod = when (paymentMethodGroup.checkedRadioButtonId) {
                R.id.cashRadio -> "Cash"
                R.id.cardRadio -> "Card"
                else -> null
            }

            if (selectedPaymentMethod == null) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPaymentMethod == "Card") {
                goToPayment(taskId,total)
                Toast.makeText(this, "Payment Confirmed via Card", Toast.LENGTH_SHORT).show()
            } else {


                Toast.makeText(this, "Payment Confirmed via Cash", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun goToPayment(task_id: Int,amount: Double){
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        val api = ApiServiceInstance.Auth.apiServices
        val body= Payment(amount,task_id)
        val call = api.payment("Bearer $token", body)

        call.enqueue(object : Callback<PaymentResponse> {
            override fun onResponse(call: Call<PaymentResponse>, response: Response<PaymentResponse>) {
                if (response.isSuccessful ) {
                    val paymentUrl = response.body()!!.url
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(paymentUrl))
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@PaymentActivity, "Failed to load Payment", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
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
                    Toast.makeText(
                        this@PaymentActivity,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
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

        taskCategory.text = task.categories.joinToString { it.name }

        val slot = task.availability.firstOrNull()

        slot?.let {
            try {
                val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val inputTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val outputDateFormat = SimpleDateFormat("EEEE - MMMM d, yyyy", Locale.getDefault())

                val date = inputDateFormat.parse(it.date)
                val time = inputTimeFormat.parse(it.time)

                val formattedDate = outputDateFormat.format(date)
                val formattedTime = SimpleDateFormat("h:mm", Locale.getDefault()).format(time)

                taskDateTime.text = "$formattedDate $formattedTime"
            } catch (e: Exception) {
                taskDateTime.text = "Date & Time: Invalid format"
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

        taskDuration.text = task.duration
        val durationHours = task.duration.replace("h", "").trim().toDoubleOrNull() ?: 0.0

        val hourlyRate = task.tasker?.hourly_rate ?: 0.0
        taskPrice.text = "$${hourlyRate}/h"

        val supportFeeValue = 14.20 // or get it from task if it's dynamic
        supportFee.text = "$$supportFeeValue/h"

        total = (hourlyRate * durationHours) + (supportFeeValue * durationHours)
        totalRate.text = "Total Rate: ${"%.2f".format(total)}$"
    }

}
