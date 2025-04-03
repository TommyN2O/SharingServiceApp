package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import com.example.sharingserviceapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.jvm.java

class MoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)  // Ensure this matches your XML layout name

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
                    startActivity(Intent(this, TasksActivity::class.java))
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
            // Navigate to MyCreatedTaskerActivity (example)
            startActivity(Intent(this, MyTaskerProfileActivity::class.java))
            finish()
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
}
