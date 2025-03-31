package com.example.sharingserviceapp.activitys

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import java.util.*

class RequestTaskActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var editTaskDescription: EditText
    private lateinit var editBudget: EditText
    private lateinit var editLocation: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerTime: Spinner
    private lateinit var btnSelectDate: Button
    private lateinit var btnAddPhoto: Button
    private lateinit var btnSubmitRequest: Button
    private lateinit var galleryContainer: LinearLayout

    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_task)

        // Initialize UI elements
        btnBack = findViewById(R.id.btn_back)
        editTaskDescription = findViewById(R.id.edit_task_description)
        editBudget = findViewById(R.id.edit_budget)
        editLocation = findViewById(R.id.edit_location)
        spinnerCategory = findViewById(R.id.spinner_category)
        spinnerTime = findViewById(R.id.spinner_time)
        btnSelectDate = findViewById(R.id.btn_select_date)
        btnAddPhoto = findViewById(R.id.btn_add_photo)
        btnSubmitRequest = findViewById(R.id.btn_submit_request)
        galleryContainer = findViewById(R.id.gallery_container)

        // Back Button
        btnBack.setOnClickListener {
            finish()
        }

        // Populate Category Spinner
        val categories = arrayOf("Cleaning", "Delivery", "Gardening", "Tech Support", "Other")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = categoryAdapter

        // Populate Time Spinner (7:00 - 22:00 every 30 min)
        val timeSlots = generateTimeSlots(7, 22, 30)
        val timeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timeSlots)
        spinnerTime.adapter = timeAdapter
        spinnerTime.setSelection(0)

        // Date Picker
        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                btnSelectDate.text = selectedDate
            }, year, month, day)
            datePicker.show()
        }

        // Add Photo (Gallery Picker)
        btnAddPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Submit Request
        btnSubmitRequest.setOnClickListener {
            val taskDescription = editTaskDescription.text.toString()
            val budget = editBudget.text.toString()
            val location = editLocation.text.toString()
            val category = spinnerCategory.selectedItem.toString()
            selectedTime = spinnerTime.selectedItem.toString()

            if (taskDescription.isEmpty() || budget.isEmpty() || location.isEmpty() || selectedDate.isEmpty() || selectedTime == "Select Time") {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Handle the request (API call or storage logic)
            Toast.makeText(this, "Request Sent!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Time Slot Generator (7:00 AM - 10:00 PM every 30 min)
    private fun generateTimeSlots(startHour: Int, endHour: Int, interval: Int): List<String> {
        val timeSlots = mutableListOf("Select Time")
        for (hour in startHour until endHour) {
            timeSlots.add(String.format("%02d:00", hour))
            timeSlots.add(String.format("%02d:30", hour))
        }
        timeSlots.add(String.format("%02d:00", endHour)) // 22:00
        return timeSlots
    }

    // Image Picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            addImageToGallery(it)
        }
    }

    // Add Image to Gallery View
    private fun addImageToGallery(imageUri: Uri) {
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(150, 150).apply {
                setMargins(8, 8, 8, 8)
            }
            setImageURI(imageUri)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        galleryContainer.addView(imageView)
    }
}
