package com.example.sharingserviceapp.activitys

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Category
import com.example.sharingserviceapp.models.City
import com.example.sharingserviceapp.models.TaskerProfileRequest
import com.example.sharingserviceapp.models.TaskerProfileResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CreateMyTaskerProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var hourlyRateEditText: EditText
    private lateinit var categoriesSpinner: Spinner
    private lateinit var citiesSpinner: Spinner
    private lateinit var saveButton: Button
    private var profilePhotoUri: Uri? = null // Store selected image URI
    private var availabilityList: List<String> = listOf() // Store selected days and times
    private var categories: List<Category> = listOf()
    private var cities: List<City> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creat_my_tasker_profile)

        // Initialize UI elements
        nameEditText = findViewById(R.id.edit_name)
        surnameEditText = findViewById(R.id.edit_surname)
        profileImageView = findViewById(R.id.img_profile_photo)
        descriptionEditText = findViewById(R.id.edit_description)
        hourlyRateEditText = findViewById(R.id.edit_hourly_rate)
        categoriesSpinner = findViewById(R.id.spinner_categories)
        citiesSpinner = findViewById(R.id.spinner_cities)
        saveButton = findViewById(R.id.btn_submit_tasker_profile)

        // Fetch categories from backend
        fetchCategories()
        //fetch cities
        fetchCities()
        // Handle back button click
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish() // Close activity
        }

        // Handle profile image selection
        findViewById<Button>(R.id.btn_upload_photo).setOnClickListener {
            openGallery()
        }

        // Handle Save Profile button click
        saveButton.setOnClickListener {
            createTaskerProfile()
        }

        // Handle select days and times button click
        val btnSelectDaysTime = findViewById<Button>(R.id.btn_select_days_time)
        btnSelectDaysTime.setOnClickListener {
            // Start the DaysAndTimeActivity to select available days and times
            val intent = Intent(this, DaysAndTimeActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SELECT_DAYS_AND_TIME)
        }
    }

    // Function to open image picker
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }

    companion object {
        const val REQUEST_CODE_SELECT_DAYS_AND_TIME = 2
        private const val IMAGE_PICK_REQUEST = 1
    }

    // Handle selected image result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle profile image selection result
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            profilePhotoUri = data.data
            profileImageView.setImageURI(profilePhotoUri)
        }

        // Handle the result from DaysAndTimeActivity
        if (requestCode == REQUEST_CODE_SELECT_DAYS_AND_TIME && resultCode == RESULT_OK) {
            val selectedDates = data?.getStringExtra("SELECTED_DATES")
            val selectedTimes = data?.getStringExtra("SELECTED_TIMES")

            // Store the selected dates and times for later use
            availabilityList = listOf(selectedDates.orEmpty(), selectedTimes.orEmpty())
            Toast.makeText(this, "Selected Dates: $selectedDates\nSelected Times: $selectedTimes", Toast.LENGTH_LONG).show()
        }
    }

    // Function to create tasker profile
    private fun createTaskerProfile() {
        val name = nameEditText.text.toString().trim()
        val surname = surnameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val hourlyRate = hourlyRateEditText.text.toString().toDoubleOrNull()

        // Get the selected category object from the list based on the name
        val selectedCategoryName = categoriesSpinner.selectedItem as String
        val selectedCategory = categories.find { it.name == selectedCategoryName }

        val selectedCityName = citiesSpinner.selectedItem as String
        val selectedCity = cities.firstOrNull { it.name == selectedCityName }

        if (selectedCity == null) {
            Toast.makeText(this, "Please select a valid city", Toast.LENGTH_SHORT).show()
            return
        }


        // Ensure category is selected
        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
            return
        }


        if (name.isEmpty() || surname.isEmpty() || description.isEmpty() || hourlyRate == null){ //|| selectedCityId == 0) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val profilePhotoPart: MultipartBody.Part? = profilePhotoUri?.let {
            val file = File(it.path!!)
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("profile_photo", file.name, requestFile)
        }

        // Now we can pass the entire Category object to the request
        val taskerProfileRequest = TaskerProfileRequest(
            name = name,
            surname = surname,
            description = description,
            hourly_rate = hourlyRate,
            categories = listOf(selectedCategory), // Pass the entire Category object
            cities = listOf(selectedCity),
            availability = availabilityList,
            profile_photo = profilePhotoPart
        )

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null) ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.postUserTaskerProfile("Bearer $token", taskerProfileRequest)
        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(call: Call<TaskerProfileResponse>, response: Response<TaskerProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, "Profile created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, "Error creating profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(this@CreateMyTaskerProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCategories() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.isSuccessful) {
                    categories = response.body() ?: emptyList()

                    // Populate the spinner with the category names
                    val categoryNames = categories.map { it.name }
                    val adapter = ArrayAdapter(this@CreateMyTaskerProfileActivity, android.R.layout.simple_spinner_item, categoryNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categoriesSpinner.adapter = adapter
                } else {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Toast.makeText(this@CreateMyTaskerProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun fetchCities() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCities().enqueue(object : Callback<List<City>> {
            override fun onResponse(call: Call<List<City>>, response: Response<List<City>>) {
                if (response.isSuccessful) {
                    cities = response.body() ?: emptyList()

                    // Populate the spinner with only city names
                    val cityNames = cities.map { it.name }

                    val adapter = ArrayAdapter(this@CreateMyTaskerProfileActivity, android.R.layout.simple_spinner_item, cityNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    citiesSpinner.adapter = adapter
                } else {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, "Failed to load cities", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<City>>, t: Throwable) {
                Toast.makeText(this@CreateMyTaskerProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



}