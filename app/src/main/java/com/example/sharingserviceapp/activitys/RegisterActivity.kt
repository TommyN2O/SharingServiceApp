package com.example.sharingserviceapp.activitys

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.RegisterRequest
import com.example.sharingserviceapp.models.AuthResponse
import com.example.sharingserviceapp.network.ApiService
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var surnameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var dateOfBirthInput: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize UI elements
        nameInput = findViewById(R.id.first_name)
        surnameInput = findViewById(R.id.surname)
        emailInput = findViewById(R.id.email)
        passwordInput = findViewById(R.id.password)
        dateOfBirthInput = findViewById(R.id.age)
        registerButton = findViewById(R.id.register_button)
        progressBar = findViewById(R.id.pb_loading)

        // Set button click listener
        registerButton.setOnClickListener {
            registerUser()
        }
//showdate
        dateOfBirthInput.setOnClickListener {
            showDatePicker()
        }
    }

    private fun registerUser() {
        val name = nameInput.text.toString().trim()
        val surname = surnameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val dateOfBirth = dateOfBirthInput.text.toString().trim()  // Expecting yyyy-MM-dd

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Debug: Check what is being sent
        android.util.Log.d("RegisterDebug", "Sending: name=$name, surname=$surname, email=$email, dob=$dateOfBirth")

        val registerRequest = RegisterRequest(name, surname, email, password, dateOfBirth)

        ApiServiceInstance.Auth.apiServices.registerUser(registerRequest)
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        if (authResponse != null) {
                            saveToken(authResponse.token)
                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        android.util.Log.e("RegisterDebug", "Response Error: ${response.errorBody()?.string()}")
                        Toast.makeText(this@RegisterActivity, "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    android.util.Log.e("RegisterDebug", "Network Error: ${t.message}")
                    Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }



    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.apply()
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Convert to "yyyy-MM-dd" format
                val selectedDate =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)

                dateOfBirthInput.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}