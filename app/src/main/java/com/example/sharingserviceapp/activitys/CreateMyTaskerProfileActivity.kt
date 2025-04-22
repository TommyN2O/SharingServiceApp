package com.example.sharingserviceapp.activitys

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.AvailabilitySlot
import com.example.sharingserviceapp.models.Category
import com.example.sharingserviceapp.models.City
import com.example.sharingserviceapp.models.TaskerProfileRequest
import com.example.sharingserviceapp.models.TaskerProfileResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
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
    private lateinit var saveButton: Button
    private lateinit var citiesTextView: TextView
    private lateinit var categoriesTextView: TextView

    private var profilePhotoUri: Uri? = null // Store selected image URI
    private var galleryUris: MutableList<Uri> = mutableListOf()
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
        saveButton = findViewById(R.id.btn_submit_tasker_profile)
        citiesTextView = findViewById(R.id.tv_selected_cities)
        categoriesTextView = findViewById(R.id.tv_selected_categories)  // Assuming you have a button to show selected cities

        citiesTextView.setOnClickListener {
            showCitySelectionDialog()  // Your dialog function for city selection
        }

        categoriesTextView.setOnClickListener {
            showCategorySelectionDialog()  // Your dialog function for city selection
        }

        // Fetch categories from backend
        fetchCategories()
        //fetch cities
        fetchCities()


        // Handle back button click
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Profile
        findViewById<Button>(R.id.btn_upload_photo).setOnClickListener {
            checkStoragePermissionAndOpenGallery(isForProfile = true)
        }
        // Gallery
        findViewById<Button>(R.id.btn_add_photos).setOnClickListener {
            checkStoragePermissionAndOpenGallery(isForProfile = false)
        }


        // Handle Save Profile button click
        saveButton.setOnClickListener {
            createTaskerProfile()
            navigateToMoreActivity()
        }

        // Handle select days and times button click
        val btnSelectDaysTime = findViewById<Button>(R.id.btn_select_days_time)
        btnSelectDaysTime.setOnClickListener {
            // Start the DaysAndTimeActivity to select available days and times
//            val intent = Intent(this, DaysAndTimeActivity::class.java)
//            startActivityForResult(intent, REQUEST_CODE_SELECT_DAYS_AND_TIME)
            val availabilitySlotList = availabilityList.map {
                val parts = it.split(" ")
                AvailabilitySlot(parts[0], parts[1])
            }

// Pass it using putParcelableArrayListExtra
            val intent = Intent(this, DaysAndTimeActivity::class.java)
            intent.putParcelableArrayListExtra("PREVIOUS_AVAILABILITY", ArrayList(availabilitySlotList))
            startActivityForResult(intent, REQUEST_CODE_SELECT_DAYS_AND_TIME)

        }
    }

    // Function to open profile photo image picker (Only one photo)
    private fun openProfilePhotoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PROFILE_IMAGE_PICK_REQUEST)
    }


    private fun navigateToMoreActivity() {
        val intent = Intent(this, MoreActivity::class.java)
        startActivity(intent)
        finish()
    }


    ///new
    private fun showCitySelectionDialog() {
        // Get city names to display in the dialog
        val items = cities.map { it.name }

        // This will hold the selected cities
        val selectedItems = mutableListOf<String>()

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

        // Set up the ListView with the items
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items)
        listView.adapter = adapter

        // Handle search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                (listView.adapter as ArrayAdapter<*>).filter.filter(newText)
                return false
            }
        })

        // Create the AlertDialog
        val builder = AlertDialog.Builder(this)
            .setTitle("Select Cities")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                // Collect the selected cities
                val selectedPositions = listView.checkedItemPositions
                selectedItems.clear()
                for (i in 0 until listView.count) {
                    if (selectedPositions[i]) {
                        selectedItems.add(items[i])
                    }
                }
                // Update the TextView with the selected cities
                val selectedCitiesText = selectedItems.joinToString(", ") // Join cities with a comma and space
                val citiesTextView = findViewById<TextView>(R.id.tv_selected_cities) // Your TextView ID
                citiesTextView.text = selectedCitiesText
            }
            .setNegativeButton("Cancel", null)

        // Show the dialog
        builder.create().show()
    }


    private fun showCategorySelectionDialog() {
        // Get city names to display in the dialog
        val items = categories.map { it.name }

        // This will hold the selected cities
        val selectedItems = mutableListOf<String>()

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

        // Set up the ListView with the items
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items)
        listView.adapter = adapter

        // Handle search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                (listView.adapter as ArrayAdapter<*>).filter.filter(newText)
                return false
            }
        })

        // Create the AlertDialog
        val builder = AlertDialog.Builder(this)
            .setTitle("Select Categories")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                // Collect the selected cities
                val selectedPositions = listView.checkedItemPositions
                selectedItems.clear()
                for (i in 0 until listView.count) {
                    if (selectedPositions[i]) {
                        selectedItems.add(items[i])
                    }
                }
                // Update the TextView with the selected cities
                val selectedCategorysText = selectedItems.joinToString(", ") // Join cities with a comma and space
                val citiesTextView = findViewById<TextView>(R.id.tv_selected_categories) // Your TextView ID
                citiesTextView.text = selectedCategorysText
            }
            .setNegativeButton("Cancel", null)

        // Show the dialog
        builder.create().show()
    }


    // Function to open image picker
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)  // Allow multiple selection for gallery images
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }


    companion object {
        const val REQUEST_CODE_SELECT_DAYS_AND_TIME = 2
        private const val IMAGE_PICK_REQUEST = 1
        private const val PROFILE_IMAGE_PICK_REQUEST = 100
        private const val REQUEST_CODE_STORAGE_PERMISSION = 1
    }
    private fun checkStoragePermissionAndOpenGallery(isForProfile: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                android.Manifest.permission.READ_MEDIA_IMAGES
            else
                android.Manifest.permission.READ_EXTERNAL_STORAGE

            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), REQUEST_CODE_STORAGE_PERMISSION)
            } else {
                if (isForProfile) openProfilePhotoGallery() else openGallery()
            }
        } else {
            if (isForProfile) openProfilePhotoGallery() else openGallery()
        }
    }

    // Handle selected image result
    // Handle selected image result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PROFILE_IMAGE_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // Profile photo selection (only one image)
            profilePhotoUri = data.data
            profileImageView.setImageURI(profilePhotoUri)  // Display selected profile photo
        }

        // Handle gallery image selection (multiple images)
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            galleryUris.clear()

            if (data.clipData != null) {
                // Multiple images selected
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    galleryUris.add(imageUri)
                }
            } else if (data.data != null) {
                // Single image selected
                val imageUri = data.data!!
                galleryUris.add(imageUri)
            }

            // Optionally show the first image selected in the gallery
            if (galleryUris.isNotEmpty()) {
                // Show the first image selected from the gallery in the profile image view
                profileImageView.setImageURI(galleryUris.firstOrNull())
            }
            val galleryContainer = findViewById<LinearLayout>(R.id.gallery_container)
            galleryContainer.removeAllViews() // Clear previous thumbnails

            for (uri in galleryUris) {
                val imageView = ImageView(this)
                imageView.setImageURI(uri)
                val layoutParams = LinearLayout.LayoutParams(200, 200)
                layoutParams.setMargins(10, 10, 10, 10)
                imageView.layoutParams = layoutParams
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                galleryContainer.addView(imageView)
            }
        }

        // Handle the result from DaysAndTimeActivity
        if (requestCode == REQUEST_CODE_SELECT_DAYS_AND_TIME && resultCode == RESULT_OK) {
            val selectedAvailability = data?.getParcelableArrayListExtra<AvailabilitySlot>("SELECTED_AVAILABILITY")
            if (selectedAvailability != null) {
                availabilityList = selectedAvailability.map { "${it.date} ${it.time}" }
                Toast.makeText(this, "Availability: ${availabilityList.joinToString("\n")}", Toast.LENGTH_LONG).show()
            }
        }
    }



    // Function to create tasker profile
    private fun createTaskerProfile() {
        val name = nameEditText.text.toString().trim()
        val surname = surnameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val hourlyRate = hourlyRateEditText.text.toString().toDoubleOrNull()

        val selectedCityNames = citiesTextView.text.toString().split(",").map { it.trim() }
        val selectedCities = cities.filter { selectedCityNames.contains(it.name) }

        val selectedCategoryNames = categoriesTextView.text.toString().split(",").map { it.trim() }
        val selectedCategories = categories.filter { selectedCategoryNames.contains(it.name) }

        if (name.isEmpty() || surname.isEmpty() || description.isEmpty() || hourlyRate == null || selectedCities.isEmpty() || selectedCategories.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select at least one city and category", Toast.LENGTH_SHORT).show()
            return
        }
        val taskerProfileRequest = TaskerProfileRequest(
            name = name,
            surname = surname,
            description = description,
            hourly_rate = hourlyRate,
            categories = selectedCategories,
            cities = selectedCities,
            availability = availabilityList.map {
                val parts = it.split(" ")
                AvailabilitySlot(parts[0], parts[1])
            }
        )

// Convert the taskerProfileRequest object to JSON using Gson
        val gson = Gson()
        val taskerProfileJson = gson.toJson(taskerProfileRequest)
        val taskerProfileRequestBody = RequestBody.create("application/json".toMediaTypeOrNull(), taskerProfileJson)


        val profilePhotoPart = profilePhotoUri?.let { uri ->
            val file = File(getRealPathFromURI(uri))
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profile_photo", file.name, requestFile)
        }

// Prevent null crash
        if (profilePhotoPart == null) {
            Toast.makeText(this, "Profile image is required", Toast.LENGTH_SHORT).show()
            return
        }

        val galleryParts = galleryUris.map { uri ->
            val file = File(getRealPathFromURI(uri))
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("galleryImages", file.name, requestFile)
        }

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null) ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            return
        }
// Now make the API call with the profilePhotoPart and galleryParts
        val call = ApiServiceInstance.Auth.apiServices.createTaskerProfile(
            "Bearer $token",
            profileImage = profilePhotoPart,  // Pass the profile photo part
            taskerProfileJson = taskerProfileRequestBody,
            galleryImages = galleryParts  // Pass the gallery images parts
        )

        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(call: Call<TaskerProfileResponse>, response: Response<TaskerProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, "Profile created successfully!", Toast.LENGTH_SHORT).show()
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//                    finish()
                } else {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, "Error creating profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(this@CreateMyTaskerProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    fun getRealPathFromURI(contentUri: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, proj, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val filePath = cursor?.getString(columnIndex ?: -1)
        cursor?.close()
        return filePath ?: ""  // Return a default empty string if file path is not found
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