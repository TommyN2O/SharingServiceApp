package com.example.sharingserviceapp.activitys

import android.app.Dialog
import android.content.Intent
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.HelperListActivity
import com.example.sharingserviceapp.adapters.TaskerHelperAdapter
import com.example.sharingserviceapp.adapters.OpenTasksHelperAdapter
import com.example.sharingserviceapp.models.TaskerHelper
import com.example.sharingserviceapp.models.OpenedTasksHelper
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.sharingserviceapp.models.City
import com.example.sharingserviceapp.models.TaskerCheck
import com.google.android.material.bottomsheet.BottomSheetBehavior
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.Calendar

class HelperListActivity : AppCompatActivity() {

    private lateinit var taskerhelperAdapter: TaskerHelperAdapter
    private lateinit var opentaskshelperAdapter: OpenTasksHelperAdapter

    private var taskerList: MutableList<TaskerHelper> = mutableListOf()
    private var opentasksList: MutableList<OpenedTasksHelper> = mutableListOf()

    private var isTaskerMode: Boolean = false

    private lateinit var btnCustomer: Button
    private lateinit var btnTasker: Button
    private lateinit var recyclerView: RecyclerView

    private lateinit var citiesTextView: TextView
    private var cities: List<City> = emptyList()
    private var selectedCityIds: MutableList<Int> = mutableListOf()
    private var selectedCityNames: MutableList<String> = mutableListOf()
    private var selectedMinPrice: Int? = null
    private var selectedMaxPrice: Int? = null
    private var selectedMinBudget: Int? = null
    private var selectedMaxBudget: Int? = null
    private var selectedDuration: Int? = null
    private var selectedCity: String? = null
    private var selectedDate: String? = null
    private var selectedTimeFrom: String? = null
    private var selectedTimeTo: String? = null
    private var selectedRating: Int? = null


    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helper_list)

        sharedPreferences = getSharedPreferences("FilterPreferences", MODE_PRIVATE)

        setupBackButton()
        fetchCities()
        isTaskerMode = intent.getBooleanExtra("is_tasker_mode", false)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        taskerhelperAdapter =
            TaskerHelperAdapter(this, taskerList) { tasker -> navigateToTaskerDetails(tasker) }
        opentaskshelperAdapter = OpenTasksHelperAdapter(
            this,
            opentasksList
        ) { tasker -> navigateToCustomerDetails(tasker) }

        btnCustomer = findViewById(R.id.btn_customer)
        btnTasker = findViewById(R.id.btn_tasker)

        toggleTaskerMode(false)

        btnCustomer.setOnClickListener { toggleTaskerMode(false) }
        btnTasker.setOnClickListener { toggleTaskerMode(true) }

        val btnFilter = findViewById<ImageView>(R.id.btn_filter)
        btnFilter.setOnClickListener {
            if (isTaskerMode) {
                showOpenedTasksFilterDialog()
            } else {
                showTaskerFilterDialog()
            }
        }
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menu_home
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

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            navigateToHomeActivity()
        }
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToTaskerDetails(selectedTasker: TaskerHelper) {
        val intent = Intent(this, TaskerHelperDetailActivity::class.java).apply {
            putExtra("tasker", selectedTasker)
        }
        startActivity(intent)
    }

    private fun navigateToCustomerDetails(task: OpenedTasksHelper) {
        val intent = Intent(this, TaskDetailOfferActivity::class.java).apply {
            putExtra("tasker_id", task.id)
        }
        startActivity(intent)
    }

    private fun fetchTaskers() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val userId =sharedPreferences.getInt("user_id",0)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val categoryId = intent.getIntExtra("category_id", -1)

        val apiService = ApiServiceInstance.Auth.apiServices

        apiService.getTaskerList(
            token = "Bearer $token",
            city = selectedCity,
            date = selectedDate,
            timeFrom = selectedTimeFrom,
            timeTo = selectedTimeTo,
            minPrice = selectedMinPrice,
            maxPrice = selectedMaxPrice,
            rating = selectedRating,
            category = categoryId,
            excludeUserId = userId,
        ).enqueue(object : Callback<List<TaskerHelper>> {
            override fun onResponse(
                call: Call<List<TaskerHelper>>,
                response: Response<List<TaskerHelper>>
            ) {
                if (response.isSuccessful) {
                    val taskers = response.body() ?: emptyList()
                    taskerList.clear()
                    taskerList.addAll(taskers)

                    taskerhelperAdapter =
                        TaskerHelperAdapter(this@HelperListActivity, taskerList) { selectedTasker ->
                            val intent = Intent(
                                this@HelperListActivity,
                                TaskerHelperDetailActivity::class.java
                            ).apply {
                                putExtra("user_id", selectedTasker.id)
                                putExtra("category_id", categoryId)
                            }
                            startActivity(intent)
                        }
                    recyclerView.adapter = taskerhelperAdapter
                } else {
                    Toast.makeText(
                        this@HelperListActivity,
                        "Failed to load taskers",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<TaskerHelper>>, t: Throwable) {
                Toast.makeText(this@HelperListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun fetchOpenedTasks() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val userId =sharedPreferences.getInt("user_id",0)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val categoryId = intent.getIntExtra("category_id", -1)

        val apiService = ApiServiceInstance.Auth.apiServices

        apiService.getOpenedTasksList(
            token = "Bearer $token",
            category = categoryId,
            city = selectedCity,
            date = selectedDate,
            minBudget = selectedMinBudget,
            maxBudget = selectedMaxBudget,
            duration = selectedDuration,
            excludeUserId = userId,
            ).enqueue(object : Callback<List<OpenedTasksHelper>> {
            override fun onResponse(
                call: Call<List<OpenedTasksHelper>>,
                response: Response<List<OpenedTasksHelper>>
            ) {
                if (response.isSuccessful) {
                    val openedTasks = response.body() ?: emptyList()
                    opentasksList.clear()
                    opentasksList.addAll(openedTasks)
                    recyclerView.adapter = opentaskshelperAdapter
                } else {
                    Toast.makeText(
                        this@HelperListActivity,
                        "Failed to load opened tasks",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<OpenedTasksHelper>>, t: Throwable) {
                Toast.makeText(this@HelperListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
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
                    Toast.makeText(
                        this@HelperListActivity,
                        "Failed to load cities",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<City>>, t: Throwable) {
                Toast.makeText(this@HelperListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun showTaskerFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.tasker_filter_bottom_sheet, null)
        dialog.setContentView(view)
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        BottomSheetBehavior.from(bottomSheet!!).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        citiesTextView = view.findViewById(R.id.text_city)
        val dateTextView = view.findViewById<TextView>(R.id.text_date_picker)
        val timeFromSpinner = view.findViewById<Spinner>(R.id.spinner_time_from)
        val timeToSpinner = view.findViewById<Spinner>(R.id.spinner_time_to)
        val minPriceEdit = view.findViewById<EditText>(R.id.edit_min_price)
        val maxPriceEdit = view.findViewById<EditText>(R.id.edit_max_price)
        val ratingBar = view.findViewById<RatingBar>(R.id.rating_bar)
        val btnApplyFilters = view.findViewById<Button>(R.id.btn_apply_filters)

        citiesTextView.text = selectedCityNames.joinToString(", ")
        dateTextView.text = selectedDate ?: ""

        minPriceEdit.setText(selectedMinPrice?.toString() ?: "")
        maxPriceEdit.setText(selectedMaxPrice?.toString() ?: "")


        ratingBar.rating = selectedRating?.toFloat() ?: 0f

        val timeOptions = (7..21).map { String.format("%02d:00", it) }
        val initialAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("") + timeOptions
        )
        initialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        timeFromSpinner.adapter = initialAdapter
        timeToSpinner.adapter = initialAdapter

        selectedTimeFrom?.let {
            val index = initialAdapter.getPosition(it)
            if (index >= 0) timeFromSpinner.setSelection(index)
        }

        selectedTimeTo?.let {
            val index = initialAdapter.getPosition(it)
            if (index >= 0) timeToSpinner.setSelection(index)
        }

        timeFromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedTime = timeFromSpinner.selectedItem?.toString()
                val newTimeToOptions = if (!selectedTime.isNullOrEmpty()) {
                    val selectedHour = selectedTime.split(":")[0].toInt()
                    val nextHours = ((selectedHour + 1)..21).filter { it <= 21 }
                    listOf("") + nextHours.map { String.format("%02d:00", it) }
                } else {
                    listOf("") + (8..21).map { String.format("%02d:00", it) }
                }

                val toAdapter = ArrayAdapter(
                    this@HelperListActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    newTimeToOptions
                )
                timeToSpinner.adapter = toAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        selectedTimeFrom?.let {
            val index = initialAdapter.getPosition(it)
            if (index >= 0) timeFromSpinner.setSelection(index)
        }

        selectedTimeTo?.let {
            val index = initialAdapter.getPosition(it)
            if (index >= 0) timeToSpinner.setSelection(index)
        }
        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format(
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay
                    )
                    dateTextView.text = formattedDate
                    selectedDate = formattedDate
                }, year, month, day)

            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

            datePickerDialog.show()
        }

        citiesTextView.setOnClickListener {
            showCitySelectionDialog()
        }
        val backButton = view.findViewById<ImageView>(R.id.btn_back_filter)
        backButton.setOnClickListener {
            dialog.dismiss()
        }

        btnApplyFilters.setOnClickListener {
            selectedCity = selectedCityIds.joinToString(",")
            selectedTimeFrom = timeFromSpinner.selectedItem?.toString()
            selectedTimeTo = timeToSpinner.selectedItem?.toString()
            selectedMinPrice = minPriceEdit.text.toString().toIntOrNull()
            selectedMaxPrice = maxPriceEdit.text.toString().toIntOrNull()
            val rating = ratingBar.rating.toInt()
            selectedRating = if (rating == 0) null else rating
            fetchTaskers()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showOpenedTasksFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.opened_tasks_filter_bottom_sheet, null)
        dialog.setContentView(view)

        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        BottomSheetBehavior.from(bottomSheet!!).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        citiesTextView = view.findViewById<TextView>(R.id.text_city)
        val dateTextView = view.findViewById<TextView>(R.id.text_date_picker)
        val minBudgetEdit = view.findViewById<EditText>(R.id.edit_min_budget)
        val maxBudgetEdit = view.findViewById<EditText>(R.id.edit_max_budget)
        val durationSpinner = view.findViewById<Spinner>(R.id.spinner_duration)
        val btnApplyFilters = view.findViewById<Button>(R.id.btn_apply_filters)
        val durationOptions = listOf("") + (1..7).map { "${it}h" }
        val durationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, durationOptions)
        durationSpinner.adapter = durationAdapter

        // Pre-fill previously applied values
        citiesTextView.text = selectedCityNames.joinToString(", ")
        dateTextView.text = selectedDate ?: ""

        minBudgetEdit.setText(selectedMinBudget?.toString() ?: "")
        maxBudgetEdit.setText(selectedMaxBudget?.toString() ?: "")

        selectedDuration?.let {
            val durationStr = "${it}h"
            val index = (durationSpinner.adapter as ArrayAdapter<String>).getPosition(durationStr)
            if (index >= 0) durationSpinner.setSelection(index)
        }


        citiesTextView.setOnClickListener {
            showCitySelectionDialog()
        }

        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->
                val formatted = String.format("%04d-%02d-%02d", y, m + 1, d)
                dateTextView.text = formatted
                selectedDate = formatted
            }, year, month, day)

            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        val backButton = view.findViewById<ImageView>(R.id.btn_back_filter)
        backButton.setOnClickListener {
            dialog.dismiss()
        }
        btnApplyFilters.setOnClickListener {
            selectedCity = selectedCityIds.joinToString(",")
            selectedMinBudget = minBudgetEdit.text.toString().toIntOrNull()
            selectedMaxBudget = maxBudgetEdit.text.toString().toIntOrNull()
            selectedDuration = durationSpinner.selectedItem?.toString()
                ?.filter { it.isDigit() }
                ?.toIntOrNull()


            fetchOpenedTasks()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCitySelectionDialog() {
        if (cities.isEmpty()) {
            Toast.makeText(this, "Cities are still loading. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val cityNames = cities.map { it.name }
        val selectedSet = selectedCityNames.toMutableSet()

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            cityNames.toMutableList()
        ) {
            override fun hasStableIds(): Boolean = true
        }

        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        for (i in 0 until adapter.count) {
            val cityName = adapter.getItem(i)
            if (selectedSet.contains(cityName)) {
                listView.setItemChecked(i, true)
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val cityName = adapter.getItem(position) ?: return@setOnItemClickListener
            if (selectedSet.contains(cityName)) {
                selectedSet.remove(cityName)
            } else {
                selectedSet.add(cityName)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText) {
                    for (i in 0 until adapter.count) {
                        val cityName = adapter.getItem(i)
                        listView.setItemChecked(i, selectedSet.contains(cityName))
                    }
                }
                return true
            }
        })

        AlertDialog.Builder(this)
            .setTitle("Select Cities")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                selectedCityNames.clear()
                selectedCityIds.clear()

                for (city in cities) {
                    if (selectedSet.contains(city.name)) {
                        selectedCityNames.add(city.name)
                        selectedCityIds.add(city.id)
                    }
                }

                citiesTextView.text = selectedCityNames.joinToString(", ")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleTaskerMode(isTasker: Boolean) {
        isTaskerMode = isTasker
        clearFilters()
        if (isTasker) {
            btnCustomer.setBackgroundColor(resources.getColor(R.color.white))
            btnTasker.setBackgroundColor(resources.getColor(R.color.my_light_primary))
            btnCustomer.setTextColor(resources.getColor(R.color.blacktxt))
            btnTasker.setTextColor(resources.getColor(R.color.white))
        } else {
            btnCustomer.setBackgroundColor(resources.getColor(R.color.my_light_primary))
            btnTasker.setBackgroundColor(resources.getColor(R.color.white))
            btnCustomer.setTextColor(resources.getColor(R.color.white))
            btnTasker.setTextColor(resources.getColor(R.color.blacktxt))
        }

        if (isTaskerMode) {
            recyclerView.adapter = opentaskshelperAdapter
            fetchOpenedTasks()
        } else {
            recyclerView.adapter = taskerhelperAdapter
            fetchTaskers()
        }

    }
    private fun clearFilters() {
        selectedCityIds.clear()
        selectedCityNames.clear()
        selectedDate = null
        selectedTimeTo = null
        selectedTimeFrom = null
        selectedCity = null
        selectedMinPrice = null
        selectedMaxPrice = null
        selectedRating = null
        selectedMinBudget = null
        selectedMaxBudget = null
        selectedDuration = null
    }

    fun showZoomDialog(images: List<String>, position: Int, baseUrl: String) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = layoutInflater.inflate(R.layout.dialog_zoom_image, null)
        dialog.setContentView(view)

        val imageView: ImageView = view.findViewById(R.id.zoomedImageView)
        val arrowLeft: ImageView = view.findViewById(R.id.arrow_left)
        val arrowRight: ImageView = view.findViewById(R.id.arrow_right)
        val closeButton: ImageView = view.findViewById(R.id.close_button)

        var currentPosition = position
        val totalImages = images.size

        val imageUrl = URL(URL(baseUrl), images[currentPosition]).toString()
        loadImage(imageUrl, imageView)

        updateArrowsVisibility(currentPosition, totalImages, arrowLeft, arrowRight)

        arrowLeft.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                val prevImageUrl = URL(URL(baseUrl), images[currentPosition]).toString()
                loadImage(prevImageUrl, imageView)
                updateArrowsVisibility(currentPosition, totalImages, arrowLeft, arrowRight)
            }
        }

        arrowRight.setOnClickListener {
            if (currentPosition < totalImages - 1) {
                currentPosition++
                val nextImageUrl = URL(URL(baseUrl), images[currentPosition]).toString()
                loadImage(nextImageUrl, imageView)
                updateArrowsVisibility(currentPosition, totalImages, arrowLeft, arrowRight)
            }
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadImage(imageUrl: String, imageView: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error)
            .into(imageView)
    }


    private fun updateArrowsVisibility(
        currentIndex: Int,
        totalSize: Int,
        arrowLeft: ImageView,
        arrowRight: ImageView
    ) {
        arrowLeft.visibility = if (currentIndex > 0) View.VISIBLE else View.GONE
        arrowRight.visibility = if (currentIndex < totalSize - 1) View.VISIBLE else View.GONE
    }

    fun hasTaskerProfile(callback: (Boolean) -> Unit) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.CheckIfIsTasker("Bearer $token")

        call.enqueue(object : Callback<TaskerCheck> {
            override fun onResponse(call: Call<TaskerCheck>, response: Response<TaskerCheck>) {
                if (response.isSuccessful) {
                    callback(response.body()?.is_tasker == true)
                } else {
                    callback(false)
                    Toast.makeText(this@HelperListActivity, "Failed to check profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskerCheck>, t: Throwable) {
                callback(false)
                Toast.makeText(this@HelperListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

