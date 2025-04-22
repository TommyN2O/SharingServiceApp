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
    private lateinit var btn_select_days_time: Button
    private lateinit var spinnerTime: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail_offer)

        // Initialize Views
        btnBack = findViewById(R.id.btn_back)
//        btnSelectDate = findViewById(R.id.btn_select_date)
//        spinnerTime = findViewById(R.id.spinner_time)

        // Set up the back button
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }


//        // Set up Time Picker Spinner
//        val timeOptions = arrayOf("Morning", "Afternoon", "Evening")
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeOptions)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerTime.adapter = adapter

//        spinnerTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
//                val selectedTime = parentView.getItemAtPosition(position).toString()
//                Toast.makeText(this@TaskDetailOfferActivity, "Selected time: $selectedTime", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onNothingSelected(parentView: AdapterView<*>) {
//                // Handle no item selected case
//            }
//        }

    }
}
