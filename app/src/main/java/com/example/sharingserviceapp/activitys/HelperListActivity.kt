package com.example.sharingserviceapp.activitys

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.CustomerHelperAdapter
import com.example.sharingserviceapp.adapters.TaskerHelperAdapter
import com.example.sharingserviceapp.models.CustomerHelper
import com.example.sharingserviceapp.models.TaskerHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class HelperListActivity : AppCompatActivity() {

    private lateinit var customerAdapter: CustomerHelperAdapter
    private lateinit var taskerAdapter: TaskerHelperAdapter

    private var customerList: MutableList<CustomerHelper> = mutableListOf()
    private var taskerList: MutableList<TaskerHelper> = mutableListOf()

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
        customerList = getCustomerListForCategory(categoryName).toMutableList()
        taskerList = getTaskerListForCategory(categoryName).toMutableList()

        // Initialize adapters
        customerAdapter = CustomerHelperAdapter(this, customerList) { helper -> navigateToCustomerDetails(helper) }
        taskerAdapter = TaskerHelperAdapter(this, taskerList) { tasker -> navigateToTaskerDetails(tasker) }

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
                    startActivity(Intent(this, TasksActivity::class.java))
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

    private fun navigateToCustomerDetails(helper: CustomerHelper) {
        val intent = Intent(this, HelperDetailActivity::class.java).apply {
            putExtra("customerHelper", helper)
        }
        startActivity(intent)
    }

    private fun navigateToTaskerDetails(tasker: TaskerHelper) {
        val intent = Intent(this, TaskDetailOfferActivity::class.java).apply {
            putExtra("tasker_name", tasker.name)
            putExtra("tasker_budget", tasker.budget)
            putExtra("tasker_city", tasker.city)
        }
        startActivity(intent)
    }

    // Load mock data for Customer helpers (to be replaced with actual data retrieval logic)
    private fun getCustomerListForCategory(category: String?): List<CustomerHelper> {
        return when (category) {
            "Cleaning" -> listOf(
                CustomerHelper(
                    "John Doe", 4.5, 120, "Professional cleaner", R.drawable.user, 50,
                    listOf("New York", "Brooklyn"), listOf("08:00", "10:30"),
                    listOf(R.drawable.clean_category), listOf(R.drawable.clean_category)
                ),
                CustomerHelper(
                    "Jane Smith", 4.8, 200, "Eco-friendly cleaner", R.drawable.user, 40,
                    listOf("Los Angeles"), listOf("09:00", "11:30"),
                    listOf(R.drawable.clean_category, R.drawable.user), listOf(R.drawable.clean_category)
                )
            )
            "Painting" -> listOf(
                CustomerHelper(
                    "Mike Johnson", 4.2, 80, "Home painter", R.drawable.user, 60,
                    listOf("Chicago"), listOf("07:30", "12:00"),
                    listOf(R.drawable.clean_category), listOf(R.drawable.clean_category)
                )
            )
            else -> emptyList()
        }
    }

    // Load mock data for Tasker helpers (to be replaced with actual data retrieval logic)
    private fun getTaskerListForCategory(category: String?): List<TaskerHelper> {
        return when (category) {
            "Cleaning" -> listOf(
                TaskerHelper(
                    "Alice Brown", R.drawable.user, "San Francisco", "Need deep cleaning",
                    70, "Tomorrow", R.drawable.clean_category, listOf(R.drawable.clean_category, R.drawable.user)
                )
            )
            "Painting" -> listOf(
                TaskerHelper(
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
            applyFilters(selectedCity, selectedPrice, selectedTime)
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

    private fun applyFilters(city: String, price: Int, time: String) {
        if (isTaskerMode) {
            // For Taskers, apply the filters only if they're not empty.
            val filteredList = taskerList.filter {
                (it.city.contains(city, ignoreCase = true) || city.isEmpty()) &&
                        (it.budget <= price || price == 0)

            }
            taskerAdapter.updateList(filteredList.toMutableList())
        } else {
            // For Customers, apply the filters only if they're not empty.
            val filteredList = customerList.filter { helper ->
                (helper.availableCities.contains(city) || city.isEmpty()) &&
                        (helper.price <= price || price == 0) &&
                        (helper.availableTimes.contains(time) || time.isEmpty())
            }
            customerAdapter.updateList(filteredList.toMutableList())
        }
    }

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
        recyclerView.adapter = if (isTaskerMode) taskerAdapter else customerAdapter
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

