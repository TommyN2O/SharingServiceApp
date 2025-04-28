package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.MyPlannedTasksAdapter
import com.example.sharingserviceapp.adapters.PeoplePlanedTasksAdapter

import com.example.sharingserviceapp.models.PlannedTaskListItem
import com.example.sharingserviceapp.models.TaskResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PlannedTasksActivity : AppCompatActivity() {

    private lateinit var myPlannedTasksAdapter: MyPlannedTasksAdapter
    private lateinit var peoplePlanedTasksAdapter: PeoplePlanedTasksAdapter
    private var isShowingPeopleRequests = false
    private lateinit var recyclerView: RecyclerView
    private var myTasksList = mutableListOf<TaskResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        recyclerView = findViewById(R.id.recycler_tasks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        myPlannedTasksAdapter = MyPlannedTasksAdapter(this, emptyList()) { task -> navigateToMyTaskDetailed(task) }
        peoplePlanedTasksAdapter = PeoplePlanedTasksAdapter(this, emptyList()) { request -> navigateToPeopleRequestsDetailed(request) }

        val btnMyTasks = findViewById<Button>(R.id.btn_my_planed_tasks)
        val btnPeopleRequests = findViewById<Button>(R.id.btn_people_planed_tasks)

        btnMyTasks.setOnClickListener { toggleList(showPeopleRequests = false) }
        btnPeopleRequests.setOnClickListener { toggleList(showPeopleRequests = true) }

        setupBottomNavigation()

        toggleList(showPeopleRequests = false)
    }


    private fun toggleList(showPeopleRequests: Boolean) {
        isShowingPeopleRequests = showPeopleRequests

        val btnMyTasks = findViewById<Button>(R.id.btn_my_planed_tasks)
        val btnPeopleRequests = findViewById<Button>(R.id.btn_people_planed_tasks)

        if (showPeopleRequests) {
            btnMyTasks.setBackgroundColor(getColor(R.color.white))
            btnPeopleRequests.setBackgroundColor(getColor(R.color.my_light_primary))
            btnMyTasks.setTextColor(getColor(R.color.blacktxt))
            btnPeopleRequests.setTextColor(getColor(R.color.white))
            fetchPeoplePlanedTasks()
        } else {
            btnMyTasks.setBackgroundColor(getColor(R.color.my_light_primary))
            btnPeopleRequests.setBackgroundColor(getColor(R.color.white))
            btnMyTasks.setTextColor(getColor(R.color.white))
            btnPeopleRequests.setTextColor(getColor(R.color.blacktxt))
            fetchMyPlanedTasks()
        }
    }

    private fun fetchMyPlanedTasks() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val apiService = ApiServiceInstance.Auth.apiServices

        apiService.plannedTaskSent("Bearer $token").enqueue(object : Callback<List<TaskResponse>> {
            override fun onResponse(call: Call<List<TaskResponse>>, response: Response<List<TaskResponse>>) {
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()
                    myTasksList.clear()
                    myTasksList.addAll(tasks)

                    val groupedList = mutableListOf<PlannedTaskListItem>()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())

                    val grouped = tasks.groupBy { task ->
                        task.availability.firstOrNull()?.date ?: "Unknown Date"
                    }

                    for ((date, tasksOnDate) in grouped) {
                        val parsedDate = try { dateFormat.parse(date) } catch (e: Exception) { null }
                        val displayDate = parsedDate?.let { displayFormat.format(it) } ?: date
                        groupedList.add(PlannedTaskListItem.DateHeader(displayDate))

                        for (task in tasksOnDate) {
                            groupedList.add(PlannedTaskListItem.TaskItem(task))
                        }
                    }

                    if (!isShowingPeopleRequests) {
                        myPlannedTasksAdapter = MyPlannedTasksAdapter(this@PlannedTasksActivity, groupedList) { task ->
                            navigateToMyTaskDetailed(task)
                        }
                        recyclerView.adapter = myPlannedTasksAdapter
                    }

                } else {
                    Toast.makeText(this@PlannedTasksActivity, "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TaskResponse>>, t: Throwable) {
                Toast.makeText(this@PlannedTasksActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun fetchPeoplePlanedTasks() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiServiceInstance.Auth.apiServices.plannedTaskReceived("Bearer $token")
            .enqueue(object : Callback<List<TaskResponse>> {
                override fun onResponse(call: Call<List<TaskResponse>>, response: Response<List<TaskResponse>>) {
                    if (response.isSuccessful) {
                        val requests = response.body() ?: emptyList()

                        val groupedList2 = mutableListOf<PlannedTaskListItem>()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val displayFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())

                        val grouped = requests.groupBy { request ->
                            request.availability.firstOrNull()?.date ?: "Unknown Date"
                        }

                        for ((date, tasksOnDate) in grouped) {
                            val parsedDate = try { dateFormat.parse(date) } catch (e: Exception) { null }
                            val displayDate = parsedDate?.let { displayFormat.format(it) } ?: date
                            groupedList2.add(PlannedTaskListItem.DateHeader(displayDate))

                            for (task in tasksOnDate) {
                                groupedList2.add(PlannedTaskListItem.TaskItem(task))
                            }
                        }

                        peoplePlanedTasksAdapter = PeoplePlanedTasksAdapter(this@PlannedTasksActivity, groupedList2) { request ->
                            navigateToPeopleRequestsDetailed(request)
                        }

                        if (isShowingPeopleRequests) {
                            recyclerView.adapter = peoplePlanedTasksAdapter
                        }
                    } else {
                        Toast.makeText(this@PlannedTasksActivity, "Failed to load requests", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<TaskResponse>>, t: Throwable) {
                    Toast.makeText(this@PlannedTasksActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun navigateToMyTaskDetailed(task: TaskResponse) {
        val intent = Intent(this, MyPlannedTaskDetailedActivity::class.java)
        intent.putExtra("TASK_ID", task.id)
        startActivity(intent)
    }

    private fun navigateToPeopleRequestsDetailed(request: TaskResponse) {
        val intent = Intent(this, PeoplePlannedTasksDetailedActivity::class.java)
        intent.putExtra("task_id", request.id)
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menu_tasks
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_tasks -> {
                    startActivity(Intent(this, PlannedTasksActivity::class.java))
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_messages -> {
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

