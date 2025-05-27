package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.CreateSupportTicketRequest
import com.example.sharingserviceapp.models.CreateTicketResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SupportActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var contentEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: ImageView
    private lateinit var errorType: TextView
    private lateinit var contentLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)
        spinner = findViewById(R.id.spinner_support_type)
        contentEditText = findViewById(R.id.et_content)
        sendButton = findViewById(R.id.btn_send)
        backButton = findViewById(R.id.btn_back)
        contentLayout = findViewById(R.id.layout_content)
        errorType = findViewById(R.id.error_type)
        setupListeners()
    }

    private fun sendSupportTicket(type: String, content: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val api = ApiServiceInstance.Auth.apiServices
        val request = CreateSupportTicketRequest(type = type, content = content)

        api.createSupportTicket("Bearer $token", request).enqueue(object : Callback<CreateTicketResponse> {
            override fun onResponse(call: Call<CreateTicketResponse>, response: Response<CreateTicketResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SupportActivity, getString(R.string.support_message_successful) , Toast.LENGTH_LONG).show()
                    val intent = Intent(this@SupportActivity, SupportTicketConfirmationActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@SupportActivity, "Failed: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<CreateTicketResponse>, t: Throwable) {
                Toast.makeText(this@SupportActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupListeners() {

        val types = listOf(
            getString(R.string.type_select_issue),
            getString(R.string.type_payment),
            getString(R.string.type_fraud),
            getString(R.string.type_task),
            getString(R.string.type_account),
            getString(R.string.type_other)
        )
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedType = spinner.selectedItem.toString()
                if (selectedType == getString(R.string.type_select_issue)) {
                    errorType.visibility = View.GONE
                    spinner.background = ContextCompat.getDrawable(this@SupportActivity, R.drawable.rounded_corner)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, MoreActivity::class.java))
            finish()
        }

        sendButton.setOnClickListener {
            val selectedType = spinner.selectedItem.toString()
            val content = contentEditText.text.toString().trim()

            var valid = true

            if (selectedType == getString(R.string.type_select_issue)) {
                errorType.visibility = View.VISIBLE
                spinner.background = ContextCompat.getDrawable(this, R.drawable.spinner_border_error)
                valid = false
            } else {
                errorType.visibility = View.GONE
                spinner.background = ContextCompat.getDrawable(this, R.drawable.rounded_corner)
            }

            if (content.isEmpty()) {
                contentLayout.error = getString(R.string.error_support_description)
                valid = false
            } else {
                contentLayout.error = null
            }

            if (valid) {
                sendSupportTicket(selectedType, content)
            }
        }

        contentEditText.doAfterTextChanged {
            contentLayout.error = null
        }
    }
}
