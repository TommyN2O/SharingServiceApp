package com.example.sharingserviceapp.activitys

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import java.util.*

class TaskDetailOfferActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnSelectDate: Button
    private lateinit var spinnerTime: Spinner
    private lateinit var switchShareTask: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail_offer)

        // Initialize Views
        btnBack = findViewById(R.id.btn_back)
        btnSelectDate = findViewById(R.id.btn_select_date)
        spinnerTime = findViewById(R.id.spinner_time)

        // Set up the back button
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        // Set up Date Picker
        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${month + 1}/$year"
                    // Do something with the selected date (e.g., set it in a TextView)
                    Toast.makeText(this, "Selected date: $selectedDate", Toast.LENGTH_SHORT).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Set up Time Picker Spinner
        val timeOptions = arrayOf("Morning", "Afternoon", "Evening")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTime.adapter = adapter

        spinnerTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTime = parentView.getItemAtPosition(position).toString()
                Toast.makeText(this@TaskDetailOfferActivity, "Selected time: $selectedTime", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Handle no item selected case
            }
        }

    }
}
