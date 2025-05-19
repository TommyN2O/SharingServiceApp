package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.CreateSupportTicketRequest
import com.example.sharingserviceapp.models.CreateTicketResponse
import com.example.sharingserviceapp.models.Review
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewsActivity : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var reviewEditText: TextInputEditText
    private lateinit var submitButton: Button
    private var taskId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        ratingBar = findViewById(R.id.rating_bar)
        reviewEditText = findViewById(R.id.et_review)
        submitButton = findViewById(R.id.btn_submit_review)
        taskId = intent.getIntExtra("TASK_ID", -1)

        if (taskId == -1) {
            Toast.makeText(this, "Invalid Task ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        submitButton.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val content = reviewEditText.text?.toString()?.trim()

            if (content.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter your review", Toast.LENGTH_SHORT).show()
            } else {
                sendReview(rating, content, taskId)
            }
        }
    }

    private fun sendReview(rating: Int, content: String, taskId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val api = ApiServiceInstance.Auth.apiServices
        val body = Review(task_request_id = taskId, rating = rating, review = content)

        api.sendReview("Bearer $token", body).enqueue(object : Callback<Review> {
            override fun onResponse(call: Call<Review>, response: Response<Review>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@ReviewsActivity, "Review submitted!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@ReviewsActivity, HistoryActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@ReviewsActivity, "Failed to send review: ${response.errorBody()?.string() ?: response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Review>, t: Throwable) {
                Toast.makeText(this@ReviewsActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
