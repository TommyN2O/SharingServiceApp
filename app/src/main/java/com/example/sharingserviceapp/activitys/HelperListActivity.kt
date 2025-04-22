package com.example.sharingserviceapp.activitys

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.TaskerHelperAdapter
import com.example.sharingserviceapp.adapters.CustomerHelperAdapter
import com.example.sharingserviceapp.models.TaskerHelper
import com.example.sharingserviceapp.models.CustumerHelper
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HelperListActivity : AppCompatActivity() {

    private lateinit var taskerhelperAdapter: TaskerHelperAdapter
    private lateinit var cutomerhelperAdapter: CustomerHelperAdapter

    private var taskerList: MutableList<TaskerHelper> = mutableListOf()
    private var custumerList: MutableList<CustumerHelper> = mutableListOf()

    private var isTaskerMode: Boolean = false // Flag to determine mode

    private lateinit var btnCustomer: Button
    private lateinit var btnTasker: Button
    private lateinit var recyclerView: RecyclerView

    // New fields for filter
    private var selectedCity: String = ""
    private var selectedPrice: Int = 0
    private var selectedTime: String = ""

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helper_list)

        sharedPreferences = getSharedPreferences("FilterPreferences", MODE_PRIVATE)

        setupBackButton()

        val categoryName = intent.getStringExtra("category_name")
        isTaskerMode = intent.getBooleanExtra("is_tasker_mode", false)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load lists for both modes
        //customerList = getCustomerListForCategory(categoryName).toMutableList()
        custumerList = getTaskerListForCategory(categoryName).toMutableList()

        // Initialize adapters
        taskerhelperAdapter = TaskerHelperAdapter(this, taskerList) { tasker -> navigateToTaskerDetails(tasker) }
        cutomerhelperAdapter = CustomerHelperAdapter(this, custumerList) { helper -> navigateToCustomerDetails(helper) }

        btnCustomer = findViewById(R.id.btn_customer)
        btnTasker = findViewById(R.id.btn_tasker)

        // Set default mode (Customer)
        toggleTaskerMode(false)

        btnCustomer.setOnClickListener { toggleTaskerMode(false) }
        btnTasker.setOnClickListener { toggleTaskerMode(true) }

        findViewById<ImageView>(R.id.btn_filter).setOnClickListener { showFilterDialog() }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menu_home
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Handle home item click
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.menu_tasks -> {
                    // Handle tasks item click
                    startActivity(Intent(this, PlanedTasksActivity::class.java))
                    finish()// Check here
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.menu_messages -> {
                    // Handle messages item click
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
        fetchTaskers()
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

    private fun navigateToCustomerDetails(tasker: CustumerHelper) {
        val intent = Intent(this, TaskDetailOfferActivity::class.java).apply {
            putExtra("tasker_name", tasker.name)
            putExtra("tasker_budget", tasker.budget)
            putExtra("tasker_city", tasker.city)
        }
        startActivity(intent)
    }

    private fun fetchTaskers() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)


        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val categoryId = intent.getIntExtra("category_id", -1)

        val apiService = ApiServiceInstance.Auth.apiServices

        // Make sure you're passing the correct token
        apiService.getTaskerList("Bearer $token").enqueue(object : Callback<List<TaskerHelper>> {
            override fun onResponse(
                call: Call<List<TaskerHelper>>,
                response: Response<List<TaskerHelper>>
            ) {
                if (response.isSuccessful) {
                    val taskers = response.body() ?: emptyList()

                    // Create a mutable list of CustomerHelper objects
                    val filteredTaskers = taskers.filter { tasker ->
                        // Check if any category in the tasker's categories matches the passed categoryId
                        tasker.categories.any { category -> category.id == categoryId }
                    }
                    taskerList.clear()
                    taskerList.addAll(filteredTaskers)

                    // Create and set the adapter for the RecyclerView
                    taskerhelperAdapter = TaskerHelperAdapter(this@HelperListActivity, taskerList) { selectedTasker ->
                        val intent = Intent(this@HelperListActivity, TaskerHelperDetailActivity::class.java).apply {
                            // Add necessary data to the intent
                            putExtra("user_id", selectedTasker.id)
                            putExtra("category_id", categoryId)
                        }
                        startActivity(intent)
                    }

                    // Set the adapter to the RecyclerView
                    recyclerView.adapter = taskerhelperAdapter
                } else {
                    Toast.makeText(this@HelperListActivity, "Failed to load taskers", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TaskerHelper>>, t: Throwable) {
                Toast.makeText(this@HelperListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Load mock data for Tasker helpers (to be replaced with actual data retrieval logic)
    private fun getTaskerListForCategory(category: String?): List<CustumerHelper> {
        return when (category) {
            "Cleaning" -> listOf(
                CustumerHelper(
                    "Alice Brown", R.drawable.user, "San Francisco", "Need deep cleaning",
                    70, "Tomorrow", R.drawable.clean_category, listOf(R.drawable.clean_category, R.drawable.user)
                )
            )
            "Painting" -> listOf(
                CustumerHelper(
                    "Robert Wilson", R.drawable.user, "Seattle", "Looking for wall painting",
                    80, "Next Week", R.drawable.clean_category,listOf(R.drawable.clean_category, R.drawable.user)
                )
            )
            else -> emptyList()
        }
    }

    // Filter Logic
    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.filter_bottom_sheet, null)
        dialog.setContentView(view)

        val citySpinner = view.findViewById<Spinner>(R.id.spinner_city)
        val priceSeekBar = view.findViewById<SeekBar>(R.id.seekbar_price)
        val timeSpinner = view.findViewById<Spinner>(R.id.spinner_specific_time)
        val btnApplyFilters = view.findViewById<Button>(R.id.btn_apply_filters)

        // Initialize city picker (Spinner) with city options
        val cities = arrayOf("New York", "Los Angeles", "Chicago", "San Francisco")
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = cityAdapter
        citySpinner.setSelection(cities.indexOf(selectedCity))

        // Set price seekbar progress
        priceSeekBar.progress = selectedPrice

        // Show previously selected time or empty
        timeSpinner.setSelection(if (selectedTime.isNotEmpty()) cities.indexOf(selectedTime) else 0)

        // Time spinner item selected
        timeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTime = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case if nothing is selected (optional)
            }
        }

        btnApplyFilters.setOnClickListener {
            selectedCity = citySpinner.selectedItem.toString()
            selectedPrice = priceSeekBar.progress
            //applyFilters(selectedCity, selectedPrice, selectedTime)
            saveFilterSettings(selectedCity, selectedPrice, selectedTime)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveFilterSettings(city: String, price: Int, time: String) {
        val editor = sharedPreferences.edit()
        if (isTaskerMode) {
            // Save tasker filters
            editor.putString("tasker_city", city)
            editor.putInt("tasker_price", price)
            editor.putString("tasker_time", time)
        } else {
            // Save customer filters
            editor.putString("customer_city", city)
            editor.putInt("customer_price", price)
            editor.putString("customer_time", time)
        }
        editor.apply()
    }

    private fun loadFilterSettings() {
        if (isTaskerMode) {

        } else {

        }
    }

//    private fun applyFilters(city: String, price: Int, time: String) {
//        if (isTaskerMode) {
//            // For Taskers, apply the filters only if they're not empty.
//            val filteredList = custumerList.filter {
//                (it.city.contains(city, ignoreCase = true) || city.isEmpty()) &&
//                        (it.budget <= price || price == 0)
//
//            }
//            cutomerhelperAdapter.updateList(filteredList.toMutableList())
//        } else {
//            // For Customers, apply the filters only if they're not empty.
//            val filteredList = taskerList.filter { helper ->
//                (helper.availableCities.contains(city) || city.isEmpty()) &&
//                        (helper.price <= price || price == 0) &&
//                        (helper.availableTimes.contains(time) || time.isEmpty())
//            }
//            taskerhelperAdapter.updateList(filteredList.toMutableList())
//        }
//    }

    private fun toggleTaskerMode(isTasker: Boolean) {
        isTaskerMode = isTasker
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

        loadFilterSettings()  // Load saved filters based on mode
        recyclerView.adapter = if (isTaskerMode) cutomerhelperAdapter else taskerhelperAdapter
    }
    fun showZoomDialog(images: List<Int>, position: Int) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = layoutInflater.inflate(R.layout.dialog_zoom_image, null)
        dialog.setContentView(view)

        val imageView: ImageView = view.findViewById(R.id.zoomedImageView)
        val arrowLeft: ImageView = view.findViewById(R.id.arrow_left)
        val arrowRight: ImageView = view.findViewById(R.id.arrow_right)
        val closeButton: ImageView = view.findViewById(R.id.close_button)

        var currentPosition = position
        val totalImages = images.size

        // Set the initial image
        imageView.setImageResource(images[currentPosition])

        // Update arrow visibility
        updateArrowsVisibility(currentPosition, totalImages, arrowLeft, arrowRight)

        // Left Arrow Click
        arrowLeft.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                imageView.setImageResource(images[currentPosition])
                updateArrowsVisibility(currentPosition, totalImages, arrowLeft, arrowRight)
            }
        }

        // Right Arrow Click
        arrowRight.setOnClickListener {
            if (currentPosition < totalImages - 1) {
                currentPosition++
                imageView.setImageResource(images[currentPosition])
                updateArrowsVisibility(currentPosition, totalImages, arrowLeft, arrowRight)
            }
        }

        // Close Button Click
        closeButton.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog to close it
        }

        dialog.show()
    }

    private fun updateArrowsVisibility(
        currentIndex: Int,
        totalSize: Int,
        arrowLeft: ImageView,
        arrowRight: ImageView
    ) {
        // Show/hide arrows based on the current position
        arrowLeft.visibility = if (currentIndex > 0) View.VISIBLE else View.GONE
        arrowRight.visibility = if (currentIndex < totalSize - 1) View.VISIBLE else View.GONE
    }

}

