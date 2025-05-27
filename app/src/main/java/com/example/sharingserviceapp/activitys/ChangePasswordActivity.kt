package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.ChangePasswordRequest
import com.example.sharingserviceapp.models.ChangePasswordResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var oldPasswordLayout: TextInputLayout
    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var oldPasswordField: TextInputEditText
    private lateinit var newPasswordField: TextInputEditText
    private lateinit var confirmPasswordField: TextInputEditText
    private lateinit var confirmButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        oldPasswordLayout = findViewById(R.id.layout_old_password)
        newPasswordLayout = findViewById(R.id.layout_new_password)
        confirmPasswordLayout = findViewById(R.id.layout_confirm_password)
        oldPasswordField = findViewById(R.id.old_password)
        newPasswordField = findViewById(R.id.et_new_password)
        confirmPasswordField = findViewById(R.id.confirm_password)
        confirmButton = findViewById(R.id.btn_confirm_psw)
        backButton = findViewById(R.id.btn_back)

        setupListeners()
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
        confirmButton.setOnClickListener {
            clearErrors()

            val oldPassword = oldPasswordField.text.toString().trim()
            val newPassword = newPasswordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            var hasError = false

            if (oldPassword.isEmpty()) {
                oldPasswordLayout.error = getString(R.string.change_password_error_message_old_password_empty)
                hasError = true
            }
            if (newPassword.length < 6) {
                newPasswordLayout.error = getString(R.string.change_password_error_message_new_password_short)
                hasError = true
            }
            if (confirmPassword != newPassword) {
                confirmPasswordLayout.error = getString(R.string.change_password_error_message_confirm_password_match)
                hasError = true
            }

            if (hasError) return@setOnClickListener

            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val token = sharedPreferences.getString("auth_token", null)

            if (token.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
             else {
            sendChangePasswordRequest(token, oldPassword, newPassword)
            }
        }
    }

    private fun clearErrors() {
        oldPasswordLayout.error = null
        newPasswordLayout.error = null
        confirmPasswordLayout.error = null
    }

    private fun sendChangePasswordRequest(token: String, oldPassword: String, newPassword: String) {
        val request = ChangePasswordRequest(
            currentPassword = oldPassword,
            newPassword = newPassword
        )
        val call = ApiServiceInstance.Auth.apiServices.changePassword("Bearer $token", request)
        call.enqueue(object : Callback<ChangePasswordResponse> {
            override fun onResponse(call: Call<ChangePasswordResponse>, response: Response<ChangePasswordResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@ChangePasswordActivity, getString(R.string.change_password_update_successful), Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@ChangePasswordActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    oldPasswordLayout.error = response.body()?.message ?: getString(R.string.change_password_error_message_old_password_match)
                }
            }
            override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                oldPasswordLayout.error = "Error: ${t.localizedMessage}"
            }
        })
    }

}
