package com.example.sharingserviceapp.activitys

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.example.sharingserviceapp.R
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.sharingserviceapp.models.AvailabilitySlot
import com.example.sharingserviceapp.models.TaskerProfileResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.text.SimpleDateFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DaysAndTimeActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private val selectedTimeSlots = mutableMapOf<String, MutableSet<Button>>() // Store time slots per date
    private val allButtons = mutableListOf<Button>()
    private val timeSlotFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val markedDays = mutableListOf<EventDay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_days_and_time)

        calendarView = findViewById(R.id.calendarView)
        val backButton = findViewById<ImageView>(R.id.backButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // Back button click
        backButton.setOnClickListener {
            finish() }


        val preSelectedAvailability = intent.getParcelableArrayListExtra<AvailabilitySlot>("PREVIOUS_AVAILABILITY")

        if (preSelectedAvailability != null) {
            for (slot in preSelectedAvailability) {
                Log.d("Received", "Date: ${slot.date}, Time: ${slot.time}")
                // ✅ Update your UI here (checkboxes, RecyclerView, etc.)
            }
        } else {
            Log.d("Received", "No availability passed")
        }
        fetchAvailabilityFromDatabase()
        // Initialize time slot buttons
        setupTimeSlots()

        // Auto-select today and show available times
        val today = Calendar.getInstance()
        showAvailableTimeSlots(today)

        // Auto-select today's date on launch
        calendarView.setDate(today)

        // Set minimum date to prevent selecting past months/days
        calendarView.setMinimumDate(today)

        // Handle calendar day selection
        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val selectedDate = eventDay.calendar

                // Disable past day selection
                if (isPastDate(selectedDate)) {
                    Toast.makeText(this@DaysAndTimeActivity, "You can't select past dates!", Toast.LENGTH_SHORT).show()
                    return
                }

                showAvailableTimeSlots(selectedDate)
            }
        })

        // Save button click
        saveButton.setOnClickListener {
            if (selectedTimeSlots.isEmpty()) {
                Toast.makeText(this, "Please select a date and time!", Toast.LENGTH_SHORT).show()
            } else {
                val selectedSlots = ArrayList<AvailabilitySlot>()

                // Loop through the selected time slots map
                selectedTimeSlots.forEach { (date, buttons) ->
                    buttons.forEach { button ->
                        val time = button.text.toString() + ":00" // Add seconds to time
                        val slot = AvailabilitySlot(date, time)
                        selectedSlots.add(slot)
                    }
                }

                // Return the selected slots to the calling activity
                // Already correct
                val resultIntent = Intent()
                resultIntent.putParcelableArrayListExtra("SELECTED_AVAILABILITY", selectedSlots)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()

            }
        }

    }

    private fun updateCalendarMarkers() {
        val markedDays = mutableListOf<EventDay>()

        // Loop through selectedTimeSlots to accumulate events for all days
        for ((date, buttons) in selectedTimeSlots) {
            if (buttons.isNotEmpty()) {
                val calendar = Calendar.getInstance()
                val dateParts = date.split("-").map { it.toInt() }
                calendar.set(dateParts[0], dateParts[1] - 1, dateParts[2]) // Set year, month, day

                // Add an event marker for this day
                markedDays.add(EventDay(calendar, R.drawable.marked_date_background))
            }
        }

        // Set all accumulated events to the calendar view
        calendarView.setEvents(markedDays)
    }

    private fun setupTimeSlots() {
        val timeSlots = listOf(
            R.id.timeSlot_07_00, R.id.timeSlot_08_00, R.id.timeSlot_09_00, R.id.timeSlot_10_00,
            R.id.timeSlot_11_00, R.id.timeSlot_12_00, R.id.timeSlot_13_00, R.id.timeSlot_14_00,
            R.id.timeSlot_15_00, R.id.timeSlot_16_00, R.id.timeSlot_17_00, R.id.timeSlot_18_00,
            R.id.timeSlot_19_00, R.id.timeSlot_20_00, R.id.timeSlot_21_00, R.id.timeSlot_22_00
        )

        for (slotId in timeSlots) {
            val button = findViewById<Button>(slotId)
            button.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY))
            button.isEnabled = false

            button.setOnClickListener {
                val selectedDate = calendarView.selectedDate ?: Calendar.getInstance() // Default to today if null
                toggleTimeSlot(button, selectedDate)
            }
            allButtons.add(button)
        }

    }

    private fun toggleTimeSlot(button: Button, selectedDate: Calendar) {
        // Ensure selectedDate is valid
        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date first!", Toast.LENGTH_SHORT).show()
            return
        }

        val formattedDate = formatDate(selectedDate)
        val timeSlotsForDay = selectedTimeSlots.getOrPut(formattedDate) { mutableSetOf() }

        if (timeSlotsForDay.contains(button)) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY))
            timeSlotsForDay.remove(button)
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN))
            timeSlotsForDay.add(button)
        }

        updateCalendarMarkers() // Refresh markers
    }

    private fun showAvailableTimeSlots(selectedDate: Calendar) {
        for (button in allButtons) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY))
            button.isEnabled = false
        }

        val currentTime = Calendar.getInstance()
        val formattedDate = formatDate(selectedDate)

        if (isSameDay(selectedDate, currentTime)) {
            enableTimeSlotsForToday()
        } else {
            enableAllTimeSlots()
        }

        // Restore previously selected time slots for this date
        selectedTimeSlots[formattedDate]?.forEach { button ->
            button.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN))
        }
    }

    private fun enableTimeSlotsForToday() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 2

        for (button in allButtons) {
            val hour = button.text.toString().split(":")[0].toInt()
            if (hour >= currentHour) {
                button.isEnabled = true
                button.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY))
                button.visibility = View.VISIBLE // Show only available time slots
            } else {
                button.visibility = View.GONE // Hide past slots for today
            }
        }
    }

    private fun enableAllTimeSlots() {
        for (button in allButtons) {
            button.isEnabled = true
            button.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY))
            button.visibility = View.VISIBLE // Ensure all buttons are visible for future dates
        }
    }

    private fun isSameDay(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isPastDate(date: Calendar): Boolean {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        return date.before(today)
    }

    private fun formatDate(date: Calendar): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(date.time)
    }

    private fun markSlotOnCalendar(slot: AvailabilitySlot) {
        val calendar = Calendar.getInstance()
        val dateParts = slot.date.split("-").map { it.toInt() }
        calendar.set(dateParts[0], dateParts[1] - 1, dateParts[2]) // Set year, month, day

        // Check if the event already exists for this date
        val eventDay = EventDay(calendar, R.drawable.marked_date_background)

        // Add the event to the list of events for this date
        if (!markedDays.contains(eventDay)) {
            markedDays.add(eventDay)  // Only add if it doesn't exist
        }

        // Get the button for the given time slot
        val timeButton = getTimeSlotButtonForTime(slot.time)

        // Check if the button exists
        if (timeButton != null) {
            // Mark the time slot button as selected
            timeButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN))

            // Add the time slot to the selectedTimeSlots map for the given date
            val dateFormatted = slot.date
            val timeSlotsForDay = selectedTimeSlots.getOrPut(dateFormatted) { mutableSetOf() }
            timeSlotsForDay.add(timeButton)
        } else {
            Log.e("DaysAndTimeActivity", "Button for time ${slot.time} not found.")
        }

        // Apply the events after adding the new event
        calendarView.setEvents(markedDays)

    }

    private fun getTimeSlotButtonForTime(time: String): Button? {
        // Match time to button (in "HH:00" format)
        return when (time) {
            "07:00:00" -> findViewById<Button>(R.id.timeSlot_07_00)
            "08:00:00" -> findViewById<Button>(R.id.timeSlot_08_00)
            "09:00:00" -> findViewById<Button>(R.id.timeSlot_09_00)
            "10:00:00" -> findViewById<Button>(R.id.timeSlot_10_00)
            "11:00:00" -> findViewById<Button>(R.id.timeSlot_11_00)
            "12:00:00" -> findViewById<Button>(R.id.timeSlot_12_00)
            "13:00:00" -> findViewById<Button>(R.id.timeSlot_13_00)
            "14:00:00" -> findViewById<Button>(R.id.timeSlot_14_00)
            "15:00:00" -> findViewById<Button>(R.id.timeSlot_15_00)
            "16:00:00" -> findViewById<Button>(R.id.timeSlot_16_00)
            "17:00:00" -> findViewById<Button>(R.id.timeSlot_17_00)
            "18:00:00" -> findViewById<Button>(R.id.timeSlot_18_00)
            "19:00:00" -> findViewById<Button>(R.id.timeSlot_19_00)
            "20:00:00" -> findViewById<Button>(R.id.timeSlot_20_00)
            "21:00:00" -> findViewById<Button>(R.id.timeSlot_21_00)
            "22:00:00" -> findViewById<Button>(R.id.timeSlot_22_00)

            // Add other time slots as needed
            else -> null // Return null if the time slot doesn't match

        }
    }
    private fun fetchAvailabilityFromDatabase() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.getUserTaskerProfile("Bearer $token")
        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(call: Call<TaskerProfileResponse>, response: Response<TaskerProfileResponse>) {
                if (response.isSuccessful) {
                    val availabilityList = response.body()?.availability

                    if (!availabilityList.isNullOrEmpty()) {
                        for (slot in availabilityList) {
                            markSlotOnCalendar(slot)
                        }
                        Toast.makeText(this@DaysAndTimeActivity, "Availability loaded", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("DaysAndTime", "No availability data found")
                    }
                } else {
                    Log.e("DaysAndTime", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(this@DaysAndTimeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("DaysAndTime", "Fetch error", t)
            }
        })
    }

}
