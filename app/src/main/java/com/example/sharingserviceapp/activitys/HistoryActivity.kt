package com.example.sharingserviceapp.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.MyTasksAdapter
import com.example.sharingserviceapp.adapters.PeopleRequestsAdapter
import com.example.sharingserviceapp.models.TaskResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryActivity : AppCompatActivity() {
    private lateinit var myTasksAdapter: MyTasksAdapter
    private lateinit var peopleRequestsAdapter: PeopleRequestsAdapter
    private var isShowingPeopleRequests = false
    private lateinit var recyclerView: RecyclerView
    private var myTasksList = mutableListOf<TaskResponse>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        peopleRequestsAdapter = PeopleRequestsAdapter(this, emptyList()) { request ->
            navigateToRequestDetails(request)
        }

        val btnMyTasks = findViewById<Button>(R.id.btn_my_tasks)
        val btnPeopleRequests = findViewById<Button>(R.id.btn_people_requests)

        btnMyTasks.setOnClickListener { toggleList(showPeopleRequests = false) }
        btnPeopleRequests.setOnClickListener { toggleList(showPeopleRequests = true) }

        myTasksAdapter = MyTasksAdapter(this, emptyList()) { task ->
            navigateToTaskDetails(task)
        }

        setupBackButton()
        toggleList(false)
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchMyTasksHistory() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val apiService = ApiServiceInstance.Auth.apiServices

        apiService.TaskSentHistory("Bearer $token").enqueue(object : Callback<List<TaskResponse>> {
            override fun onResponse(
                call: Call<List<TaskResponse>>,
                response: Response<List<TaskResponse>>
            ) {
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()
                    myTasksList.clear()
                    myTasksList.addAll(tasks)

                    myTasksAdapter = MyTasksAdapter(this@HistoryActivity, myTasksList) { task ->
                        navigateToTaskDetails(task)
                    }

                    if (!isShowingPeopleRequests) {
                        recyclerView.adapter = myTasksAdapter
                    }
                } else {
                    Toast.makeText(this@HistoryActivity, "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TaskResponse>>, t: Throwable) {
                Toast.makeText(this@HistoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPeopleRequestsHistory() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        ApiServiceInstance.Auth.apiServices.TaskReceivedHistory("Bearer $token")
            .enqueue(object : Callback<List<TaskResponse>> {
                override fun onResponse(call: Call<List<TaskResponse>>, response: Response<List<TaskResponse>>) {
                    if (response.isSuccessful) {
                        val requests = response.body() ?: emptyList()
                        peopleRequestsAdapter = PeopleRequestsAdapter(this@HistoryActivity, requests) { request ->
                            navigateToRequestDetails(request)
                        }

                        if (isShowingPeopleRequests) {
                            recyclerView.adapter = peopleRequestsAdapter
                        }
                    }
                }

                override fun onFailure(call: Call<List<TaskResponse>>, t: Throwable) {
                    Toast.makeText(this@HistoryActivity, "Failed to load requests", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun toggleList(showPeopleRequests: Boolean) {
        isShowingPeopleRequests = showPeopleRequests

        val btnMyTasks = findViewById<Button>(R.id.btn_my_tasks)
        val btnPeopleRequests = findViewById<Button>(R.id.btn_people_requests)

        if (showPeopleRequests) {
            btnMyTasks.setBackgroundColor(getColor(R.color.white))
            btnPeopleRequests.setBackgroundColor(getColor(R.color.my_light_primary))
            btnMyTasks.setTextColor(getColor(R.color.blacktxt))
            btnPeopleRequests.setTextColor(getColor(R.color.white))
            recyclerView.adapter = peopleRequestsAdapter
            fetchPeopleRequestsHistory()
        } else {
            btnMyTasks.setBackgroundColor(getColor(R.color.my_light_primary))
            btnPeopleRequests.setBackgroundColor(getColor(R.color.white))
            btnMyTasks.setTextColor(getColor(R.color.white))
            btnPeopleRequests.setTextColor(getColor(R.color.blacktxt))
            recyclerView.adapter = myTasksAdapter
            fetchMyTasksHistory()
        }

    }

    private fun navigateToTaskDetails(task: TaskResponse) {
        val intent = Intent(this, MyTasksDetailedHistoryActivity::class.java)
        intent.putExtra("TASK_ID", task.id)
        startActivity(intent)
    }

    private fun navigateToRequestDetails(request: TaskResponse) {
        val intent = Intent(this, PeopleRequestDetailedHistoryActivity::class.java)
        intent.putExtra("task_id", request.id)
        startActivity(intent)
    }
}
