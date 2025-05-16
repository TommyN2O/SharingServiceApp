package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.CreateSupportTicketRequest
import com.example.sharingserviceapp.models.CreateTicketResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SupportActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var contentEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        spinner = findViewById(R.id.spinner_support_type)
        contentEditText = findViewById(R.id.et_content)
        sendButton = findViewById(R.id.btn_send)
        backButton = findViewById(R.id.btn_back)

        val types = listOf("Select issue type", "Payment", "Froud", "Task", "Account", "Other")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        backButton.setOnClickListener {
            startActivity(Intent(this, MoreActivity::class.java))
            finish()
        }

        sendButton.setOnClickListener {
            val selectedType = spinner.selectedItem.toString()
            val content = contentEditText.text.toString().trim()

            if (selectedType == "Select issue type" || content.isEmpty()) {
                Toast.makeText(this, "Please select a type and fill in the content.", Toast.LENGTH_SHORT).show()
            } else {
                sendSupportTicket(selectedType, content)
            }
        }
    }

    private fun sendSupportTicket(type: String, content: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val api = ApiServiceInstance.Auth.apiServices
        val request = CreateSupportTicketRequest(type = type, content = content)

        api.createSupportTicket("Bearer $token", request).enqueue(object : Callback<CreateTicketResponse> {
            override fun onResponse(call: Call<CreateTicketResponse>, response: Response<CreateTicketResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SupportActivity, "Support ticket sent!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@SupportActivity, SupportTicketConfirmationActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@SupportActivity, "Failed to send ticket: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<CreateTicketResponse>, t: Throwable) {
                Toast.makeText(this@SupportActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
