package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.Toast
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.TaskerProfileResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java

class MoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)  // Ensure this matches your XML layout name


        // Get the toast message from the intent if it exists
        val toastMessage = intent.getStringExtra("toast_message")
        toastMessage?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()

        }
        // Initialize the bottom navigation
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menu_more
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Navigate to HomeActivity
                    startActivity(Intent(this, HomeActivity::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_tasks -> {
                    // Navigate to TasksActivity
                    startActivity(Intent(this, PlanedTasksActivity::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_messages -> {
                    // Navigate to MessagesActivity
                    startActivity(Intent(this, MessagesActivity::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_more -> {
                    // Stay in MoreActivity
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }

//        // Handle clicks on LinearLayouts (not just the icon)
        val iconSaved = findViewById<LinearLayout>(R.id.icon_saved)
        iconSaved?.setOnClickListener {
            // Navigate to SavedActivity (example)
            startActivity(Intent(this, SavedActivity::class.java))
            finish()
        }

        val iconMyCreatedTasker = findViewById<LinearLayout>(R.id.icon_my_created_tasker)
        iconMyCreatedTasker?.setOnClickListener {
            checkIfTaskerProfileExists()
        }

        val iconHistory = findViewById<LinearLayout>(R.id.icon_history)
        iconHistory?.setOnClickListener {
            // Navigate to HistoryActivity (example)
            startActivity(Intent(this, HistoryActivity::class.java))
            finish()
        }

        val iconSupport = findViewById<LinearLayout>(R.id.icon_support)
        iconSupport?.setOnClickListener {
            // Navigate to SupportActivity (example)
            startActivity(Intent(this, SupportActivity::class.java))
            finish()
        }

        val iconSettings = findViewById<ImageView>(R.id.icon_setting)
        iconSettings?.setOnClickListener {
            // Navigate to SettingsActivity (example)
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
        val iconCreatTask = findViewById<LinearLayout>(R.id.icon_creat_task)
        iconCreatTask?.setOnClickListener {
            // Navigate to SupportActivity (example)
            startActivity(Intent(this, CreatTaskActivity::class.java))
            finish()
        }
    }
    // 🔹 Method to check if the tasker profile exists from the backend
    private fun checkIfTaskerProfileExists() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val userId = sharedPreferences.getInt("user_id", 0)

        if (token.isNullOrEmpty() || userId == 0) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.getUserTaskerProfile("Bearer $token")
        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(
                call: Call<TaskerProfileResponse>,
                response: Response<TaskerProfileResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    startActivity(Intent(this@MoreActivity, MyTaskerProfileActivity::class.java))
                } else {
                    startActivity(Intent(this@MoreActivity, CreateMyTaskerProfileActivity::class.java))
                }
            }

            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(this@MoreActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@MoreActivity, CreateMyTaskerProfileActivity::class.java))
            }
        })
    }
}
