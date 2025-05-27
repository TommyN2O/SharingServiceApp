package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.LoginRequest
import com.example.sharingserviceapp.models.AuthResponse
import com.example.sharingserviceapp.models.TokenRequest
import com.example.sharingserviceapp.models.TokenResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var backButton: ImageView
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        emailInput = findViewById(R.id.email)
        passwordInput = findViewById(R.id.password)
        loginButton = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.pb_loading)
        emailLayout = findViewById(R.id.email_layout)
        passwordLayout = findViewById(R.id.password_layout)
        backButton = findViewById(R.id.backButton)
        setupListeners()
        resetListener()
    }

    private fun loginUser() {
        progressBar.visibility = View.VISIBLE

        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        var isValid = true

        emailLayout.error = null
        passwordLayout.error = null

        if (email.isEmpty()) {
            emailLayout.error = getString(R.string.login_error_empty_email)
            isValid = false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.login_error_format_email)
            isValid = false
        }
        if (password.isEmpty()) {
            passwordLayout.error = getString(R.string.login_error_empty_password)
            isValid = false
        }
        if (!isValid) {
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

                        registerFCMToken(authResponse.token)
                    }
                } else {
                    progressBar.visibility = View.GONE
                    passwordLayout.error = getString(R.string.login_error_wrong_email)
                    emailLayout.error = getString(R.string.login_error_wrong_password)
                    Toast.makeText(this@LoginActivity, getString(R.string.login_error_toast_check_fields), Toast.LENGTH_SHORT).show()

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
                    return@addOnCompleteListener
                }
                val fcmToken = task.result
                Log.d(TAG, "FCM Token: $fcmToken")

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

    private fun setupListeners() {
        backButton.setOnClickListener{
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        loginButton.setOnClickListener {
            loginUser()
        }
    }

    private fun resetListener(){
        val textView = findViewById<TextView>(R.id.question)
        textView.movementMethod = LinkMovementMethod.getInstance()

        val spannableString = SpannableString(getString(R.string.login_psw_forget))

        val respasswClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, ResetPasswordActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        val respasswStartIndex = spannableString.indexOf("Atkurti slaptažodį")
        val respasswEndIndex = respasswStartIndex + "Atkurti slaptažodį".length

        spannableString.setSpan(respasswClickableSpan, respasswStartIndex, respasswEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
    }
}


