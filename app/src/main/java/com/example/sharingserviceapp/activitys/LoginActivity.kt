package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.LoginRequest
import com.example.sharingserviceapp.models.AuthResponse
import com.example.sharingserviceapp.models.TokenRequest
import com.example.sharingserviceapp.models.TokenResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "LoginActivity"
    }
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.email)
        passwordInput = findViewById(R.id.password)
        loginButton = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.pb_loading)

        loginButton.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        progressBar.visibility = View.VISIBLE

        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        val loginRequest = LoginRequest(email, password)
        val call = ApiServiceInstance.Auth.apiServices.loginUser(loginRequest)

        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        saveTokenAndUserId(authResponse.token, authResponse.user.id, email, password)

                        // Get and register FCM token after successful login
                        registerFCMToken(authResponse.token)
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@LoginActivity, "Login failed. Check your credentials.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun proceedToHome() {
        progressBar.visibility = View.GONE
        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun registerFCMToken(authToken: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    //proceedToHome()
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val fcmToken = task.result
                Log.d(TAG, "FCM Token: $fcmToken")

                // Send FCM token to server
                val call = ApiServiceInstance.Auth.apiServices.registerDeviceToken(
                    "Bearer $authToken",
                    TokenRequest(fcmToken)
                )

                call.enqueue(object : Callback<TokenResponse> {
                    override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                        if (response.isSuccessful) {
                            Log.d(TAG, "FCM token registered successfully")
                        } else {
                            Log.e(TAG, "Failed to register FCM token: ${response.errorBody()?.string()}")
                        }
                        proceedToHome()
                    }

                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                        Log.e(TAG, "Error registering FCM token", t)
                        proceedToHome()
                    }
                })
            }
    }

    private fun saveTokenAndUserId(token: String, user: Int, email: String, password: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("auth_token", token)
            putInt("user_id", user)
            putString("email", email)
            putString("password", password)
            apply()
        }
    }
}


