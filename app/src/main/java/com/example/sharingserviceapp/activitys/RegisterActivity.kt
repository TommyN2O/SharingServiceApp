package com.example.sharingserviceapp.activitys

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.LoginActivity
import com.example.sharingserviceapp.models.RegisterRequest
import com.example.sharingserviceapp.models.AuthResponse
import com.example.sharingserviceapp.network.ApiService
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var nameInput: TextInputEditText
    private lateinit var surnameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var dateOfBirthInput: TextInputEditText
    private lateinit var nameLayout: TextInputLayout
    private lateinit var surnameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var ageLayout: TextInputLayout
    private lateinit var registerButton: Button
    private lateinit var backButton: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        nameInput = findViewById(R.id.first_name)
        surnameInput = findViewById(R.id.surname)
        emailInput = findViewById(R.id.email)
        passwordInput = findViewById(R.id.password)
        dateOfBirthInput = findViewById(R.id.age)
        registerButton = findViewById(R.id.register_button)
        progressBar = findViewById(R.id.pb_loading)
        backButton = findViewById(R.id.backButton)
        nameLayout = findViewById(R.id.first_name_layout)
        surnameLayout = findViewById(R.id.surname_layout)
        emailLayout = findViewById(R.id.email_layout)
        passwordLayout = findViewById(R.id.password_layout)
        ageLayout = findViewById(R.id.age_layout)
        setupListeners()
        termsAndCondisionsListener()
    }

    private fun setupListeners() {
        backButton.setOnClickListener{
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        registerButton.setOnClickListener {
            registerUser()
        }
        dateOfBirthInput.setOnClickListener {
            showDatePicker()
        }
    }

    private fun registerUser() {
        val name = nameInput.text.toString().trim()
        val surname = surnameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val dateOfBirth = dateOfBirthInput.text.toString().trim()

        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/~`|\\\\])(?=.*\\d).{6,}\$")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        nameLayout.error = null
        surnameLayout.error = null
        emailLayout.error = null
        passwordLayout.error = null
        ageLayout.error = null

        var isValid = true

        if (name.isEmpty()) {
            nameLayout.error = getString(R.string.reg_error_empty_name)
            isValid = false
        }

        if (surname.isEmpty()) {
            surnameLayout.error = getString(R.string.reg_error_empty_surname)
            isValid = false
        }

        if (email.isEmpty()) {
            emailLayout.error = getString(R.string.reg_error_empty_email)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.login_error_format_email)
            isValid = false
        }

        if (password.isEmpty()) {
            passwordLayout.error = getString(R.string.reg_error_empty_psw)
            isValid = false
        } else if (!password.matches(passwordPattern)) {
            passwordLayout.error = getString(R.string.reg_error_bad_psw)
            isValid = false
        }

        if (dateOfBirth.isEmpty()) {
            ageLayout.error = getString(R.string.reg_error_empty_date_of_birth)
            isValid = false
        } else {
            val dobDate = try {
                dateFormat.parse(dateOfBirth)
            } catch (e: Exception) {
                null
            }
            if (dobDate != null) {
                val calendar = Calendar.getInstance().apply { add(Calendar.YEAR, -16) }
                if (dobDate.after(calendar.time)) {
                    ageLayout.error = getString(R.string.reg_error_smallest_year_old_date_of_birth)
                    isValid = false
                }
            }
        }

        if (!isValid) {
            Toast.makeText(this, getString(R.string.reg_error_fill_error), Toast.LENGTH_SHORT).show()
            return
        }

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
                        Toast.makeText(this@RegisterActivity, getString(R.string.reg_failed_register), Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
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

    private fun termsAndCondisionsListener() {
        val textView = findViewById<TextView>(R.id.text_terms)
        textView.movementMethod = LinkMovementMethod.getInstance()

        val spannableString = SpannableString(getString(R.string.reg_terms))

        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val termsUri = Uri.parse("https://www.treks.lt/")
                val intent = Intent(Intent.ACTION_VIEW, termsUri)
                startActivity(intent)
            }
        }
        val conditionsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val termsUri = Uri.parse("https://www.treks.lt/")
                val intent = Intent(Intent.ACTION_VIEW, termsUri)
                startActivity(intent)
            }
        }

        val termsStartIndex = spannableString.indexOf("taisyklėmis")
        val termsEndIndex = termsStartIndex + "taisyklėmis".length
        val conditionsStartIndex = spannableString.indexOf("privatumo politika")
        val conditionsEndIndex = conditionsStartIndex + "privatumo politika".length

        spannableString.setSpan(termsClickableSpan, termsStartIndex, termsEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(conditionsClickableSpan, conditionsStartIndex, conditionsEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val localeLt = Locale("lt")
        Locale.setDefault(localeLt)
        val config = resources.configuration
        config.setLocale(localeLt)
        val greenTheme = R.style.CustomDatePickerDialogTheme
        val datePickerDialog = DatePickerDialog(
            ContextThemeWrapper(this, greenTheme),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                dateOfBirthInput.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Patvirtinti", datePickerDialog)
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Atšaukti", datePickerDialog)
        datePickerDialog.show()
    }
}