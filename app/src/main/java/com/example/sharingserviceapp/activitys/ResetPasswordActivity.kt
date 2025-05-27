package com.example.sharingserviceapp.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.ImageView
import com.example.sharingserviceapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var sendButton: Button
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        backButton = findViewById(R.id.backButton)
        sendButton = findViewById(R.id.btn_request_email)
        emailInputLayout = findViewById(R.id.textInputLayoutEmail)
        emailEditText = findViewById(R.id.et_email_registerAct)


        backButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        sendButton.setOnClickListener {
            val email = emailEditText.text?.toString()?.trim()

            if (email.isNullOrEmpty()) {
                emailInputLayout.error = getString(R.string.reg_error_empty_email)
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInputLayout.error = getString(R.string.login_error_format_email)
            } else {
                emailInputLayout.error = null

                val intent = Intent(this, ResetPasswordConfirmActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}