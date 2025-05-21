package com.example.sharingserviceapp.activitys

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.HelperListActivity
import com.example.sharingserviceapp.activitys.RequestTaskActivity
import com.example.sharingserviceapp.adapters.MyTasksAdapter
import com.example.sharingserviceapp.adapters.PeopleRequestsAdapter
import com.example.sharingserviceapp.models.Category
import com.example.sharingserviceapp.models.City
import com.example.sharingserviceapp.models.TaskResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class RequestsOffersActivity : AppCompatActivity() {

    private lateinit var myTasksAdapter: MyTasksAdapter
    private lateinit var peopleRequestsAdapter: PeopleRequestsAdapter
    private var isShowingPeopleRequests = false
    private lateinit var recyclerView: RecyclerView
    private var cities: List<City> = emptyList()
    private var categories: List<Category> = listOf()
    private var selectedCityIds: MutableList<Int> = mutableListOf()
    private var selectedCategoryIds: MutableList<Int> = mutableListOf()
    private var selectedCityNames: MutableList<String> = mutableListOf()
    private var myTasksList = mutableListOf<TaskResponse>()
    private val selectedItems = mutableListOf<String>()
    private var selectedDate: String? = null
    private var selectedStatus: String? = null
    private var selectedCity: String? = null
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests_offers)

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
        val btnFilter = findViewById<ImageView>(R.id.btn_filter)
        btnFilter.setOnClickListener {

            showTaskerFilterDialog()

        }
        setupBackButton()
        toggleList(false)
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            navigateBackActivity()
        }
    }
    private fun navigateBackActivity() {
        val intent = Intent(this, MoreActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun fetchMyTasks() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val apiService = ApiServiceInstance.Auth.apiServices

        apiService.getMyTasks("Bearer $token").enqueue(object : Callback<List<TaskResponse>> {
            override fun onResponse(
                call: Call<List<TaskResponse>>,
                response: Response<List<TaskResponse>>
            ) {
                if (response.isSuccessful) {
                    val tasks = response.body()?.filter { it.status.lowercase() != "completed" && it.status.lowercase() != "paid" && it.status.lowercase() != "refunded"} ?: emptyList()
                    myTasksList.clear()
                    myTasksList.addAll(tasks)

                    myTasksAdapter = MyTasksAdapter(this@RequestsOffersActivity, myTasksList) { task ->
                        navigateToTaskDetails(task)
                    }

                    if (!isShowingPeopleRequests) {
                        recyclerView.adapter = myTasksAdapter
                    }
                } else {
                    Toast.makeText(this@RequestsOffersActivity, "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TaskResponse>>, t: Throwable) {
                Toast.makeText(this@RequestsOffersActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPeopleRequests() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        ApiServiceInstance.Auth.apiServices.getPeopleRequests("Bearer $token")
            .enqueue(object : Callback<List<TaskResponse>> {
                override fun onResponse(call: Call<List<TaskResponse>>, response: Response<List<TaskResponse>>) {
                    if (response.isSuccessful) {
                        val requests = response.body()?.filter { it.status.lowercase() != "completed" && it.status.lowercase() != "paid" && it.status.lowercase() != "canceled" && it.status.lowercase() != "declined" && it.status.lowercase() != "refunded"} ?: emptyList()
                        peopleRequestsAdapter = PeopleRequestsAdapter(this@RequestsOffersActivity, requests) { request ->
                            navigateToRequestDetails(request)
                        }

                        if (isShowingPeopleRequests) {
                            recyclerView.adapter = peopleRequestsAdapter
                        }
                    }
                }

                override fun onFailure(call: Call<List<TaskResponse>>, t: Throwable) {
                    Toast.makeText(this@RequestsOffersActivity, "Failed to load requests", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun toggleList(showPeopleRequests: Boolean) {
        isShowingPeopleRequests = showPeopleRequests

        val btnMyTasks = findViewById<Button>(R.id.btn_my_tasks)
        val btnPeopleRequests = findViewById<Button>(R.id.btn_people_requests)

        clearFilters()
        if (showPeopleRequests) {
            btnMyTasks.setBackgroundColor(getColor(R.color.white))
            btnPeopleRequests.setBackgroundColor(getColor(R.color.my_light_primary))
            btnMyTasks.setTextColor(getColor(R.color.blacktxt))
            btnPeopleRequests.setTextColor(getColor(R.color.white))
            recyclerView.adapter = peopleRequestsAdapter
            fetchPeopleRequests()
        } else {
            btnMyTasks.setBackgroundColor(getColor(R.color.my_light_primary))
            btnPeopleRequests.setBackgroundColor(getColor(R.color.white))
            btnMyTasks.setTextColor(getColor(R.color.white))
            btnPeopleRequests.setTextColor(getColor(R.color.blacktxt))
            recyclerView.adapter = myTasksAdapter
            fetchMyTasks()
        }

    }
    private fun clearFilters() {
        selectedCityIds.clear()
        selectedCategoryIds.clear()
        selectedCityNames.clear()
        selectedItems.clear()
        selectedDate = null
        selectedStatus = null
        selectedCity = null
        selectedCategory = null
    }

    private fun showTaskerFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.tasks_filter_bottom_sheet, null)
        dialog.setContentView(view)

        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        BottomSheetBehavior.from(bottomSheet!!).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        val categoryTextView = view.findViewById<TextView>(R.id.text_category)
        val statusSpinner = view.findViewById<Spinner>(R.id.spinner_status)
        val cityTextView = view.findViewById<TextView>(R.id.text_city)
        val dateTextView = view.findViewById<TextView>(R.id.text_date_picker)
        val btnApplyFilters = view.findViewById<Button>(R.id.btn_apply_filters)
        val backButton = view.findViewById<ImageView>(R.id.btn_back_filter)

        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("","Pending", "Waiting for Payment", "Open", "Canceled", "Declined"))
        statusSpinner.adapter = statusAdapter

        fetchCities()
        fetchCategories()

        categoryTextView.setOnClickListener {
            showCategorySelectionDialog(categoryTextView)
        }

        cityTextView.setOnClickListener {
            showCitySelectionDialog(cityTextView)
        }

        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                dateTextView.text = formattedDate
                selectedDate = formattedDate
            }, year, month, day)

            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        backButton.setOnClickListener {
            dialog.dismiss()
        }

        btnApplyFilters.setOnClickListener {
            selectedCity = selectedCityIds.joinToString(",")
            selectedCategory = selectedCategoryIds.joinToString(",")
            selectedStatus = statusSpinner.selectedItem?.toString()
            fetchMyTasks()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCategorySelectionDialog(categoryTextView: TextView) {
        if (categories.isEmpty()) {
            Toast.makeText(this, "Categories are still loading. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val items = categories.map { it.name }
        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items)
        listView.adapter = adapter

        for (i in 0 until listView.count) {
            if (selectedItems.contains(items[i])) {
                listView.setItemChecked(i, true)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                (listView.adapter as ArrayAdapter<*>).filter.filter(newText)
                return false
            }
        })

        val builder = AlertDialog.Builder(this)
            .setTitle("Select Categories")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedPositions = listView.checkedItemPositions
                val newSelectedItems = mutableListOf<String>()

                for (i in 0 until listView.count) {
                    if (selectedPositions[i]) {
                        newSelectedItems.add(items[i])
                        val categoryId = categories[i].id
                        selectedCategoryIds.add(categoryId)
                    }
                }

                val selectedCategoriesText = newSelectedItems.joinToString(", ")
                categoryTextView.text = selectedCategoriesText

                selectedItems.clear()
                selectedItems.addAll(newSelectedItems)
            }
            .setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = selectedItems.isNotEmpty()
            listView.setOnItemClickListener { _, _, _, _ ->
                positiveButton.isEnabled = listView.checkedItemCount > 0
            }
        }

        dialog.show()
    }

    private fun showCitySelectionDialog(cityTextView: TextView) {
        if (cities.isEmpty()) {
            Toast.makeText(this, "Cities are still loading. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val cityNames = cities.map { it.name }
        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, cityNames)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        for (i in cityNames.indices) {
            if (selectedCityNames.contains(cityNames[i])) {
                listView.setItemChecked(i, true)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        AlertDialog.Builder(this)
            .setTitle("Select Cities")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val checkedPositions = listView.checkedItemPositions
                selectedCityNames.clear()
                selectedCityIds.clear()

                for (i in 0 until listView.count) {
                    if (checkedPositions[i]) {
                        val cityName = adapter.getItem(i)!!
                        val city = cities.find { it.name == cityName }
                        if (city != null) {
                            selectedCityNames.add(city.name)
                            selectedCityIds.add(city.id)
                        }
                    }
                }
                cityTextView.text = selectedCityNames.joinToString(", ")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchCategories() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.isSuccessful) {
                    categories = response.body() ?: emptyList()
                } else {
                    Toast.makeText(this@RequestsOffersActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Toast.makeText(this@RequestsOffersActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCities() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCities().enqueue(object : Callback<List<City>> {
            override fun onResponse(call: Call<List<City>>, response: Response<List<City>>) {
                if (response.isSuccessful) {
                    cities = response.body() ?: emptyList()
                } else {
                    Toast.makeText(this@RequestsOffersActivity, "Failed to load cities", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<City>>, t: Throwable) {
                Toast.makeText(this@RequestsOffersActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToTaskDetails(task: TaskResponse) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra("TASK_ID", task.id)
        startActivity(intent)
    }

    private fun navigateToRequestDetails(request: TaskResponse) {
        val intent = Intent(this, RequestDetailActivity::class.java)
        intent.putExtra("task_id", request.id)
        startActivity(intent)
    }
}
