package com.example.sharingserviceapp.activitys

import android.app.AlertDialog
import android.content.Intent
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
    private lateinit var btnBack: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var hourlyRateEditText: EditText
    private lateinit var durationTextView: TextView
    private lateinit var daySpinner: Spinner
    private lateinit var timeSpinner: Spinner
    private lateinit var sendButton: Button
    private lateinit var errorDescription: TextView
    private lateinit var errorHourlyRate: TextView
    private var selectedDuration: String? = null
    private var taskId: Int = -1
    private var availableSlots: List<AvailabilitySlot> = emptyList()
    private var availableDates: List<String> = emptyList()
    private var availableTimesForSelectedDate: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail_offer)
        btnBack = findViewById(R.id.btn_back)
        descriptionEditText = findViewById(R.id.edit_task_description)
        hourlyRateEditText = findViewById(R.id.edit_budget)
        durationTextView = findViewById(R.id.tv_selected_duration)
        daySpinner = findViewById(R.id.spinner_day)
        timeSpinner = findViewById(R.id.spinner_time)
        sendButton = findViewById(R.id.btn_submit_request)
        errorDescription = findViewById(R.id.error_description)
        errorHourlyRate = findViewById(R.id.error_hourly_rate)

        taskId = intent.getIntExtra("task_id", -1)

        val durationSet = intent.getIntExtra("duration", -1)
        val durationStrings = arrayOf("1 val.", "2 val.", "3 val.", "4 val.", "5 val.", "6 val.", "7 val.")
        if (durationSet in 1..7) {
            selectedDuration = durationStrings[durationSet - 1]
            durationTextView.text = selectedDuration
            durationTextView.setTextColor(getColor(android.R.color.black))
        }
        fetchOfferAvailability(taskId)
        setupListeners()
    }
    private fun setupListeners() {
        durationTextView.setOnClickListener {
            showDurationSelectionDialog()
        }
        sendButton.setOnClickListener {
            sendOffer(taskId)
        }
        btnBack.setOnClickListener { finish() }
    }

    private fun showDurationSelectionDialog() {
        val durations = arrayOf("1 val.", "2 val.", "3 val.", "4 val.", "5 val.", "6 val.", "7 val.")
        val selectedIndex = selectedDuration?.let { durations.indexOf(it) } ?: -1
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.create_task_select_duration_dialog_title))
            .setSingleChoiceItems(durations, selectedIndex) { _, which ->
                selectedDuration = durations[which]
            }
            .setPositiveButton("Patvirtinti") { _, _ ->
                durationTextView.text = selectedDuration ?: getString(R.string.create_task_select_duration_dialog_title)
                durationTextView.setTextColor(getColor(android.R.color.black))
            }
            .setNegativeButton("Atšaukti", null)
            .show()
    }

    private fun fetchOfferAvailability(taskId: Int) {
        val token = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
                        Toast.makeText(this@TaskDetailOfferActivity, getString(R.string.task_request_failed_load_availability), Toast.LENGTH_SHORT).show()
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
        val hourlyRate = hourlyRateEditText.text.toString().trim().toDoubleOrNull()
        val selectedDay = daySpinner.selectedItem?.toString()
        val selectedTime = timeSpinner.selectedItem?.toString()
        val duration = selectedDuration?.replace(" val.", "")?.toIntOrNull() ?: 1

        clearErrors()
        var isValid = true

        if (description.isEmpty()) {
            errorDescription.visibility = View.VISIBLE
            descriptionEditText.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            descriptionEditText.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }

        if (hourlyRate == null || hourlyRate <= 0) {
            errorHourlyRate.visibility = View.VISIBLE
            hourlyRateEditText.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            hourlyRateEditText.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }
        if (!isValid) return

        val slot = AvailabilitySlot(selectedDay!!, selectedTime!!)

        val request = OpenTaskOffer(
            description = description,
            price = hourlyRate!!,
            availability = slot,
            duration = duration,
        )
        val token = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
       val id = taskId
        ApiServiceInstance.Auth.apiServices.sendOpenTaskOffer("Bearer $token",request, id)
            .enqueue(object : Callback<OpenTaskOffer> {
                override fun onResponse(call: Call<OpenTaskOffer>, response: Response<OpenTaskOffer>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@TaskDetailOfferActivity, getString(R.string.create_offer_offer_sent_successful), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@TaskDetailOfferActivity, getString(R.string.create_offer_offer_send_failed), Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<OpenTaskOffer>, t: Throwable) {
                    Toast.makeText(this@TaskDetailOfferActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun clearErrors(){
        errorDescription.visibility = View.GONE
        errorHourlyRate.visibility = View.GONE

        descriptionEditText.setBackgroundResource(R.drawable.rounded_edittext)
        hourlyRateEditText.setBackgroundResource(R.drawable.rounded_edittext)
    }
}
