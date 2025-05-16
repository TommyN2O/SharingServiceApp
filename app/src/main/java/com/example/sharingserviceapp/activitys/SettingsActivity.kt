package com.example.sharingserviceapp.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.sharingserviceapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupListeners()
    }

    private fun setupListeners() {
        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton?.setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
            finish()
        }

        val langOption = findViewById<LinearLayout>(R.id.lang_option)
        langOption?.setOnClickListener {
            startActivity(Intent(this, LanguageSettingsActivity::class.java))
           finish()
        }

        val profileDetailsOption = findViewById<LinearLayout>(R.id.profile_details_option)
        profileDetailsOption?.setOnClickListener {
            startActivity(Intent(this, ProfileDetailsActivity::class.java))
            finish()
        }

        val passwordChangeOption = findViewById<LinearLayout>(R.id.password_change_option)
        passwordChangeOption?.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
            finish()
        }


        val logout = findViewById<Button>(R.id.btn_logout)
        logout?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))

            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                remove("auth_token")
                remove("user_id")
                remove("email")
                remove("password")
                apply()
            }

            finish()
        }
    }

}