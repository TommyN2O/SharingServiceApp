package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.TaskAdapter
import com.example.sharingserviceapp.models.Task1
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class TasksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCustomer: Button
    private lateinit var btnTasker: Button
    private var isTasker = false // Default is Customer view

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        recyclerView = findViewById(R.id.recycler_tasks)
        btnCustomer = findViewById(R.id.btn_customer)
        btnTasker = findViewById(R.id.btn_tasker)

        setupButtonListeners()
        loadTasks()

        setupBottomNavigation()
    }

    private fun setupButtonListeners() {
        btnCustomer.setOnClickListener {
            if (!isTasker) return@setOnClickListener // Avoid unnecessary reloading
            isTasker = false
            updateButtonStyles()
            loadTasks()
        }

        btnTasker.setOnClickListener {
            if (isTasker) return@setOnClickListener // Avoid unnecessary reloading
            isTasker = true
            updateButtonStyles()
            loadTasks()
        }
    }

    private fun updateButtonStyles() {
        // Customer Button
        btnCustomer.setBackgroundColor(
            ContextCompat.getColor(this, if (isTasker) R.color.white else R.color.my_light_primary)
        )
        btnCustomer.setTextColor(
            ContextCompat.getColor(this, if (isTasker) R.color.black else R.color.white)
        )

        // Tasker Button
        btnTasker.setBackgroundColor(
            ContextCompat.getColor(this, if (isTasker) R.color.my_light_primary else R.color.white)
        )
        btnTasker.setTextColor(
            ContextCompat.getColor(this, if (isTasker) R.color.white else R.color.black)
        )
    }


    private fun loadTasks() {
        val groupedTasks = mutableListOf<Pair<String, List<Task1>>>()
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (isTasker) {
            groupedTasks.addAll(
                listOf(
                    Pair(todayDate, listOf(Task1("Help with laundry", "Laundry", "3:00 PM", "Tasker 2", todayDate))),
                    Pair("2025-03-30", listOf(Task1("Help with cleaning", "Cleaning", "12:00 PM", "Tasker 1", "2025-03-30")))
                )
            )
        } else {
            groupedTasks.addAll(
                listOf(
                    Pair(todayDate, listOf(Task1("Looking for help with cleaning", "Cleaning", "12:00 PM", "", todayDate))),
                    Pair("2025-03-30", listOf(Task1("Looking for help with laundry", "Laundry", "1:00 PM", "", "2025-03-30")))
                )
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TaskAdapter(groupedTasks, isTasker)
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menu_tasks
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Handle the home item click
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.menu_tasks -> {
                    // Handle the home item click
                    startActivity(Intent(this, TasksActivity::class.java))
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.menu_messages -> {
                    // Handle the messages item click
                    startActivity(Intent(this, MessagesActivity::class.java))
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.menu_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }

                else -> false
            }
        }
    }
}
