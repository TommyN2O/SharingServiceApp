package com.example.sharingserviceapp.activitys

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.CreateMyTaskerProfileActivity
import com.example.sharingserviceapp.activitys.EditMyTaskerProfileActivity
import com.example.sharingserviceapp.models.AvailabilitySlot
import com.example.sharingserviceapp.models.Category
import com.example.sharingserviceapp.models.City
import com.example.sharingserviceapp.models.TaskRequestBody
import com.example.sharingserviceapp.models.TaskerHelper
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.gson.Gson
import com.yalantis.ucrop.util.BitmapLoadUtils.calculateInSampleSize
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class RequestTaskActivity : AppCompatActivity() {

    private lateinit var TaskDescription: EditText
    private lateinit var categoriesTextView: TextView
    private lateinit var citiesTextView: TextView
    private lateinit var durationTextView: TextView
     private lateinit var btnAddPhoto: Button

    private lateinit var btnSubmitRequest: Button
    private var selectedCategoryId: Int = 0
    private lateinit var allowedCityIds: List<String>
    private lateinit var allowedCategoryIds: List<String>
    private var categories: List<Category> = listOf()
    private var cities: List<City> = listOf()
    private var selectedItems = mutableListOf<String>()
    private var selectedCity: String? = null
    private var selectedDuration: String? = null

    private lateinit var spinnerDay: Spinner
    private lateinit var spinnerTime: Spinner
    private val galleryUris = mutableListOf<Uri>()
    private lateinit var galleryContainer: LinearLayout

    private var availableSlots: List<AvailabilitySlot> = emptyList()
    private var availableDates: List<String> = emptyList()
    private var availableTimesForSelectedDate: List<String> = emptyList()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_task)


        TaskDescription = findViewById(R.id.edit_task_description)
        citiesTextView = findViewById(R.id.tv_selected_cities)
        categoriesTextView = findViewById(R.id.tv_selected_categories)
        durationTextView = findViewById(R.id.tv_selected_duration)
       btnAddPhoto = findViewById(R.id.btn_add_photo)
        btnSubmitRequest = findViewById(R.id.btn_submit_request)
       galleryContainer = findViewById(R.id.gallery_container)
        allowedCityIds = intent.getStringArrayListExtra("allowed_city_ids") ?: listOf()
        allowedCategoryIds = intent.getStringArrayListExtra("allowed_category_ids") ?: listOf()
        selectedCategoryId = intent.getIntExtra("category_id",0)


        citiesTextView.setOnClickListener {
            showCitySelectionDialog()
        }

        // Handle categories TextView click to show category selection dialog
        categoriesTextView.setOnClickListener {
            showCategorySelectionDialog()
        }

        durationTextView.setOnClickListener {
            showDurationSelectionDialog()
        }


        btnAddPhoto.setOnClickListener {
            checkStoragePermissionAndOpenGallery()
        }

//savin id of tasker
        val taskerUserId = intent.getIntExtra("user_id", -1)

        if (taskerUserId == -1) {
            Toast.makeText(this, "Tasker ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        spinnerDay = findViewById(R.id.spinner_day)
        spinnerTime = findViewById(R.id.spinner_time)

        fetchTaskerAvailability()


        fetchCategories()
        fetchCities()
        setupBackButton()

        // Submit Request
        btnSubmitRequest.setOnClickListener {
            sendTaskerRequest()
//            startActivity(Intent(this, RequestsOffersActivity::class.java))
//            finish()
        }
    }

    companion object {
        const val REQUEST_CODE_SELECT_AVAILABILITY = 1001
        const val IMAGE_PICK_REQUEST = 1002
        const val REQUEST_CODE_STORAGE_PERMISSION= 1003
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            if (data.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    if (!galleryUris.contains(imageUri)) galleryUris.add(imageUri)
                }
            } else if (data.data != null) {
                val imageUri = data.data!!
                if (!galleryUris.contains(imageUri)) galleryUris.add(imageUri)
            }

            displayImagesInGallery()
        }
    }


    private fun setupBackButton() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            navigateBackActivity()
        }
    }

    private fun navigateBackActivity() {
        val intent = Intent(this, TaskerHelperDetailActivity::class.java).apply {
            putExtra("user_id", intent.getIntExtra("user_id", -1))
            putExtra("category_id", selectedCategoryId)
        }
        startActivity(intent)
        finish()
    }

    // Inside RequestTaskActivity - when categories are fetched
    private fun fetchCategories() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.isSuccessful) {
                    val allCategories = response.body() ?: emptyList()
                    categories = allCategories.filter { allowedCategoryIds.contains(it.id.toString()) }
                    // After categories are fetched, set the selected category name in the TextView
                    setSelectedCategory()
                } else {
                    Toast.makeText(this@RequestTaskActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Toast.makeText(this@RequestTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setSelectedCategory() {
        val selectedCategory = categories.find { it.id == selectedCategoryId }
        selectedCategory?.let {
            categoriesTextView.text = it.name
        }
    }


    private fun fetchCities() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCities().enqueue(object : Callback<List<City>> {
            override fun onResponse(call: Call<List<City>>, response: Response<List<City>>) {
                if (response.isSuccessful) {
                    val allCities = response.body() ?: emptyList()
                    cities = allCities.filter { allowedCityIds.contains(it.id.toString()) }
                } else {
                    Toast.makeText(this@RequestTaskActivity, "Failed to load cities", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<City>>, t: Throwable) {
                Toast.makeText(this@RequestTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCitySelectionDialog() {
        val items = cities.map { it.name }.toMutableList()
        var tempSelectedCity: String? = selectedCity

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, items)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        selectedCity?.let {
            val index = items.indexOf(it)
            if (index != -1) {
                listView.setItemChecked(index, true)
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            tempSelectedCity = listView.getItemAtPosition(position).toString()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        val builder = AlertDialog.Builder(this)
            .setTitle("Select Location")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                selectedCity = tempSelectedCity
                citiesTextView.text = selectedCity ?: "Select City"
                citiesTextView.setTextColor(resources.getColor(android.R.color.black, theme))
            }
            .setNegativeButton("Cancel", null)

        builder.show()
    }


    private fun showDurationSelectionDialog() {
        val durations = arrayOf("1h", "2h", "3h", "4h", "More")
        val selectedIndex = selectedDuration?.let { durations.indexOf(it) } ?: -1

        val builder = AlertDialog.Builder(this)
            .setTitle("Select Duration")
            .setSingleChoiceItems(durations, selectedIndex) { dialog, which ->
                selectedDuration = durations[which]
            }
            .setPositiveButton("OK") { dialog, _ ->
                durationTextView.text = selectedDuration ?: "Select Duration"
                durationTextView.setTextColor(resources.getColor(android.R.color.black, theme))
            }
            .setNegativeButton("Cancel", null)

        builder.show()
    }

    private fun showCategorySelectionDialog() {
        val items = categories.map { it.name }
        categoriesTextView.setTextColor(resources.getColor(android.R.color.black, theme))
        if (selectedItems.isEmpty()) {
            val selectedCategory = categories.find { it.id == selectedCategoryId }
            selectedCategory?.let {
                selectedItems.add(it.name)
                categoriesTextView.setTextColor(resources.getColor(android.R.color.black, theme))
            }
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items)
        listView.adapter = adapter

        for (i in 0 until listView.count) {
            if (selectedItems.contains(items[i])) {
                listView.setItemChecked(i, true)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                (listView.adapter as ArrayAdapter<*>).filter.filter(newText)
                return false
            }
        })

        val builder = AlertDialog.Builder(this)
            .setTitle("Select Categories")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->

                val selectedPositions = listView.checkedItemPositions
                val newSelectedItems = mutableListOf<String>()

                for (i in 0 until listView.count) {
                    if (selectedPositions[i]) {
                        newSelectedItems.add(items[i])
                    }
                }
                categoriesTextView.setTextColor(resources.getColor(android.R.color.black, theme))
                if (newSelectedItems.isEmpty() || newSelectedItems == selectedItems) {
                    Toast.makeText(this, "Please select at least one category.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton // Prevent closing the dialog
                }

                selectedItems.clear()
                selectedItems.addAll(newSelectedItems)

                val selectedCategorysText = selectedItems.joinToString(", ")
                val citiesTextView = findViewById<TextView>(R.id.tv_selected_categories)
                citiesTextView.text = selectedCategorysText
            }
            .setNegativeButton("Cancel", null)


        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = selectedItems.isNotEmpty()
            listView.setOnItemClickListener { _, _, _, _ ->
                positiveButton.isEnabled = listView.checkedItemCount > 0 && hasSelectionChanged(listView)
            }
        }

        dialog.show()
    }

    private fun hasSelectionChanged(listView: ListView): Boolean {
        val currentSelectedItems = mutableListOf<String>()
        for (i in 0 until listView.count) {
            if (listView.isItemChecked(i)) {
                currentSelectedItems.add(categories[i].name)
            }
        }
        return currentSelectedItems != selectedItems
    }

    private fun fetchTaskerAvailability() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) return

        val call = ApiServiceInstance.Auth.apiServices.getTaskerProfileById("Bearer $token",userId)

        call.enqueue(object : Callback<TaskerHelper> {
            override fun onResponse(call: Call<TaskerHelper>, response: Response<TaskerHelper>) {
                if (response.isSuccessful) {
                    availableSlots = response.body()?.availability ?: emptyList()

                    availableDates = availableSlots.map { it.date }.distinct()
                    setupDaySpinner()
                } else {
                    Toast.makeText(this@RequestTaskActivity, "Failed to load availability", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskerHelper>, t: Throwable) {
                Toast.makeText(this@RequestTaskActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDaySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availableDates)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDay.adapter = adapter

        spinnerDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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
        spinnerTime.adapter = adapter
    }


    private fun sendTaskerRequest() {
        val description = TaskDescription.text.toString().trim()
        val selectedDay = spinnerDay.selectedItem?.toString()
        val selectedTime = spinnerTime.selectedItem?.toString()
        val duration = selectedDuration
        val cityName = citiesTextView.text.toString()
        val categoryNames = categoriesTextView.text.toString().split(",").map { it.trim() }

        if (description.isEmpty() || selectedDay.isNullOrEmpty() || selectedTime.isNullOrEmpty() ||
            duration.isNullOrEmpty() || cityName.isEmpty() || categoryNames.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCity = cities.find { it.name == cityName }
        val selectedCategories = categories.filter { categoryNames.contains(it.name) }

        if (selectedCity == null || selectedCategories.isEmpty()) {
            Toast.makeText(this, "Invalid city or category", Toast.LENGTH_SHORT).show()
            return
        }

        val availabilitySlot = AvailabilitySlot(selectedDay, selectedTime)

        val request = TaskRequestBody(
            description = description,
            city = selectedCity,
            categories = selectedCategories,
            duration = duration,
            availability = listOf(availabilitySlot),
            sender_id = getUserIdFromPrefs(),
            tasker_id = intent.getIntExtra("user_id", -1)
        )

        val gson = Gson()
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

        val galleryParts = galleryUris.mapIndexed { index, uri ->
            val file = File(getRealPathFromURI(uri))
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("galleryImages", file.name, requestFile)
        }

        val token = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("auth_token", null) ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.sendTaskRequest(
            "Bearer $token",
            requestBody,
            galleryParts
        )

        call.enqueue(object : Callback<TaskRequestBody> {
            override fun onResponse(call: Call<TaskRequestBody>, response: Response<TaskRequestBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RequestTaskActivity, "Request sent!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@RequestTaskActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskRequestBody>, t: Throwable) {
                Toast.makeText(this@RequestTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getUserIdFromPrefs(): Int {
        return getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("user_id", -1)
    }

    fun getRealPathFromURI(contentUri: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, proj, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val filePath = cursor?.getString(columnIndex ?: -1)
        cursor?.close()
        return filePath ?: ""
    }
    private fun displayImagesInGallery() {
        galleryContainer.removeAllViews()

        for ((index, uri) in galleryUris.withIndex()) {
            val container = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    setMargins(10, 10, 10, 10)
                }
            }

            val imageView = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(decodeSampledBitmapFromUri(uri, 300, 300))
            }

            val removeButton = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(50, 50).apply {
                    gravity = Gravity.END or Gravity.TOP
                }
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                setPadding(6, 6, 6, 6)
                setOnClickListener {
                    showDeleteConfirmationDialog {
                        galleryUris.removeAt(index)
                        displayImagesInGallery()
                    }
                }
            }
            container.addView(imageView)
            container.addView(removeButton)
            galleryContainer.addView(container)
        }
    }

    private fun decodeSampledBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }

    private fun checkStoragePermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                android.Manifest.permission.READ_MEDIA_IMAGES
            else
                android.Manifest.permission.READ_EXTERNAL_STORAGE

            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), REQUEST_CODE_STORAGE_PERMISSION)
            } else {
                 openGallery()
            }
        } else {
             openGallery()
        }
    }
    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to remove this image?")
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }
}
