package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.AuthResponse
import com.example.sharingserviceapp.models.LoginRequest
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val REQUEST_CODE_NOTIFICATION = 1001

class MainActivity : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var btnCreateAcc: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATION
                )
            }
        }
        val email = getSharedPrefString("email") ?: ""
        val password = getSharedPrefString("password") ?: ""

        if (email.isNotEmpty() && password.isNotEmpty()) {
            // Try auto login
            attemptAutoLogin(email, password)
        } else {
            // No auto login credentials, show buttons
            showButtons()
        }
    }

    private fun attemptAutoLogin(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)
        val call = ApiServiceInstance.Auth.apiServices.loginUser(loginRequest)

        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    saveTokenAndUserId(response.body()!!.token, response.body()!!.user.id, email, password)
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    finish()
                } else {
                    // Failed, show login/register buttons
                    showButtons()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                // Network error or API failed
                showButtons()
            }
        })
    }

    private fun showButtons() {
        setContentView(R.layout.activity_main)

        btnLogin = findViewById(R.id.btn_login)
        btnCreateAcc = findViewById(R.id.btn_con_email)

        btnLogin.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        btnCreateAcc.setOnClickListener {
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
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

    private fun getSharedPrefString(name: String): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getString(name, "")
    }
}
