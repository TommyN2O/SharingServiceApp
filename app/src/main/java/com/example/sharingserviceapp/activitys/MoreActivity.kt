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
        setContentView(R.layout.activity_more)

        setupBottomNavigation()

        setupListeners()
    }
    private fun setupListeners() {

        val iconSaved = findViewById<LinearLayout>(R.id.icon_saved)
        iconSaved?.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
            finish()
        }

        val iconMyCreatedTasker = findViewById<LinearLayout>(R.id.icon_my_created_tasker)
        iconMyCreatedTasker?.setOnClickListener {
            checkIfTaskerProfileExists()
        }

        val iconHistory = findViewById<LinearLayout>(R.id.icon_history)
        iconHistory?.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            finish()
        }

        val iconSupport = findViewById<LinearLayout>(R.id.icon_support)
        iconSupport?.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
            finish()
        }

        val iconSettings = findViewById<ImageView>(R.id.icon_setting)
        iconSettings?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
        val iconCreatTask = findViewById<LinearLayout>(R.id.icon_creat_task)
        iconCreatTask?.setOnClickListener {
            startActivity(Intent(this, CreatTaskActivity::class.java))
            finish()
        }
        val iconRequestsOffers = findViewById<LinearLayout>(R.id.icon_requests)
        iconRequestsOffers?.setOnClickListener {
            startActivity(Intent(this, RequestsOffersActivity::class.java))
            finish()
        }
        val iconBalance = findViewById<LinearLayout>(R.id.icon_balance)
        iconBalance?.setOnClickListener {
            startActivity(Intent(this, BalanceActivity::class.java))
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
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }

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
