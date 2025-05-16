package com.example.sharingserviceapp.activitys

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.AvailabilitySlot
import com.example.sharingserviceapp.models.OpenTaskOffer
import com.example.sharingserviceapp.models.OpenedTasksHelper
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskDetailOfferActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var descriptionEditText: EditText
    private lateinit var hourlyRateEditText: EditText
    private lateinit var durationTextView: TextView
    private lateinit var daySpinner: Spinner
    private lateinit var timeSpinner: Spinner
    private lateinit var sendButton: Button

    private var selectedDuration: String? = null
    private var availableSlots: List<AvailabilitySlot> = emptyList()
    private var availableDates: List<String> = emptyList()
    private var availableTimesForSelectedDate: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail_offer)

        // View binding
        btnBack = findViewById(R.id.btn_back)
        descriptionEditText = findViewById(R.id.edit_task_description)
        hourlyRateEditText = findViewById(R.id.edit_budget)
        durationTextView = findViewById(R.id.tv_selected_duration)
        daySpinner = findViewById(R.id.spinner_day)
        timeSpinner = findViewById(R.id.spinner_time)
        sendButton = findViewById(R.id.btn_submit_request)

        btnBack.setOnClickListener { finish() }

        val taskId = intent.getIntExtra("task_id", -1)
        if (taskId == -1) return

        durationTextView.setOnClickListener {
            showDurationSelectionDialog()
        }

        sendButton.setOnClickListener {
            sendOffer(taskId)
        }

        fetchOfferAvailability(taskId)
    }

    private fun showDurationSelectionDialog() {
        val durations = arrayOf("1h", "2h", "3h", "4h", "5h", "6h", "7h")
        val selectedIndex = selectedDuration?.let { durations.indexOf(it) } ?: -1

        AlertDialog.Builder(this)
            .setTitle("Select Duration")
            .setSingleChoiceItems(durations, selectedIndex) { _, which ->
                selectedDuration = durations[which]
            }
            .setPositiveButton("OK") { _, _ ->
                durationTextView.text = selectedDuration ?: "Select Duration"
                durationTextView.setTextColor(getColor(android.R.color.black))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchOfferAvailability(taskId: Int) {
        val token = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show()
            return
        }


        ApiServiceInstance.Auth.apiServices.getOpenTaskById("Bearer $token", taskId)
            .enqueue(object : Callback<OpenedTasksHelper> {
                override fun onResponse(call: Call<OpenedTasksHelper>, response: Response<OpenedTasksHelper>) {
                    if (response.isSuccessful) {
                        availableSlots = response.body()?.availability ?: emptyList()
                        availableDates = availableSlots.map { it.date }.distinct()
                        setupDaySpinner()
                    } else {
                        Toast.makeText(this@TaskDetailOfferActivity, "Failed to load availability", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OpenedTasksHelper>, t: Throwable) {
                    Toast.makeText(this@TaskDetailOfferActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupDaySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availableDates)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpinner.adapter = adapter

        daySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDate = availableDates[position]
                availableTimesForSelectedDate = availableSlots
                    .filter { it.date == selectedDate }
                    .map { it.time }
                setupTimeSpinner()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupTimeSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availableTimesForSelectedDate)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeSpinner.adapter = adapter
    }

    private fun sendOffer(taskId: Int) {
        val description = descriptionEditText.text.toString().trim()
        val hourlyRate = hourlyRateEditText.text.toString().trim().toDouble()
        val selectedDay = daySpinner.selectedItem?.toString()
        val selectedTime = timeSpinner.selectedItem?.toString()
        val duration = selectedDuration?.replace("h", "")?.toIntOrNull() ?: 1


        if (description.isEmpty() || hourlyRate == null || selectedDay.isNullOrEmpty() || selectedTime.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val slot = AvailabilitySlot(selectedDay, selectedTime)

        val request = OpenTaskOffer(
            description = description,
            price = hourlyRate,
            availability = slot,
            duration = duration,
        )

        val token = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("auth_token", null) ?: ""

        ApiServiceInstance.Auth.apiServices.sendOpenTaskOffer("Bearer $token",request, taskId)
            .enqueue(object : Callback<OpenTaskOffer> {
                override fun onResponse(call: Call<OpenTaskOffer>, response: Response<OpenTaskOffer>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@TaskDetailOfferActivity, "Offer sent!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@TaskDetailOfferActivity, "Failed to send offer", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OpenTaskOffer>, t: Throwable) {
                    Toast.makeText(this@TaskDetailOfferActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
