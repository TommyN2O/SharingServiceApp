package com.example.sharingserviceapp.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.sharingserviceapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupListeners()
        setupBottomNavigation()
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

        val darkModeOption = findViewById<LinearLayout>(R.id.dark_mode_option)
        darkModeOption?.setOnClickListener {
//            startActivity(Intent(this, DarkModeSettingsActivity::class.java))
//            finish()
        }

        val passwordChangeOption = findViewById<LinearLayout>(R.id.password_change_option)
        passwordChangeOption?.setOnClickListener {
//            startActivity(Intent(this, ChangePasswordActivity::class.java))
//            finish()
        }

        val notificationsOption = findViewById<LinearLayout>(R.id.notifications_option)
        notificationsOption?.setOnClickListener {
//            startActivity(Intent(this, NotificationsSettingsActivity::class.java))
//            finish()
        }
    }



    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menu_more
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_tasks -> {
                    startActivity(Intent(this, PlannedTasksActivity::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_messages -> {
                    startActivity(Intent(this, MessagesActivity::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }
}