package com.example.sharingserviceapp.activitys

import android.app.Activity
import android.app.AlertDialog
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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.AvailabilitySlot
import com.example.sharingserviceapp.models.Category
import com.example.sharingserviceapp.models.City
import com.example.sharingserviceapp.models.TaskRequestBody
import com.example.sharingserviceapp.models.TaskerHelper
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RequestTaskActivity : AppCompatActivity() {
    private lateinit var TaskDescription: EditText
    private lateinit var categoriesTextView: TextView
    private lateinit var citiesTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var btnAddPhoto: Button
    private lateinit var allowedCityIds: List<String>
    private lateinit var allowedCategoryIds: List<String>
    private lateinit var btnSubmitRequest: Button
    private lateinit var spinnerDay: Spinner
    private lateinit var spinnerTime: Spinner
    private lateinit var galleryContainer: LinearLayout
    private lateinit var errorDescription: TextView
    private lateinit var errorCities: TextView
    private lateinit var errorDuration: TextView
    private lateinit var errorPhotos: TextView
    private var selectedCategoryName: String? = null
    private var categories: List<Category> = listOf()
    private var selectedCategoryId: Int? = 0
    private var cities: List<City> = listOf()
    private var selectedCity: String? = null
    private var selectedDuration: String? = null
    private val galleryUris = mutableListOf<Uri>()
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
        errorDescription = findViewById(R.id.error_description)
        errorCities = findViewById(R.id.error_cities)
        errorDuration = findViewById(R.id.error_duration)
        errorPhotos = findViewById(R.id.error_photos)
        allowedCityIds = intent.getStringArrayListExtra("allowed_city_ids") ?: listOf()
        allowedCategoryIds = intent.getStringArrayListExtra("allowed_category_ids") ?: listOf()
        selectedCategoryId = intent.getIntExtra("category_id",0)
        spinnerDay = findViewById(R.id.spinner_day)
        spinnerTime = findViewById(R.id.spinner_time)

        val taskerUserId = intent.getIntExtra("user_id", -1)
        if (taskerUserId == -1) {
            Toast.makeText(this, getString(R.string.error_invalid_taskerID), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        fetchTaskerAvailability()
        fetchCategories()
        fetchCities()
        setupListeners()
    }
    private fun setupListeners() {
        citiesTextView.setOnClickListener {
            showCitySelectionDialog()
        }
        categoriesTextView.setOnClickListener {
            showCategorySelectionDialog()
        }
        durationTextView.setOnClickListener {
            showDurationSelectionDialog()
        }
        btnAddPhoto.setOnClickListener {
            checkStoragePermissionAndOpenGallery()
        }
        btnSubmitRequest.setOnClickListener {
            sendTaskerRequest()
        }
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, TaskerHelperDetailActivity::class.java).apply {
                putExtra("user_id", intent.getIntExtra("user_id", -1))
                putExtra("category_id", selectedCategoryId)
            }
            startActivity(intent)
            finish()
        }
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

    private fun fetchCategories() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.isSuccessful) {
                    val allCategories = response.body() ?: emptyList()
                    categories = allCategories.filter { allowedCategoryIds.contains(it.id.toString()) }
                    setSelectedCategory()
                } else {
                    Toast.makeText(this@RequestTaskActivity, getString(R.string.create_task_failed_load_category), Toast.LENGTH_SHORT).show()
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
            selectedCategoryName = it.name
            categoriesTextView.setTextColor(getColor(android.R.color.black))
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
                    Toast.makeText(this@RequestTaskActivity, getString(R.string.create_task_failed_load_cities), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<City>>, t: Throwable) {
                Toast.makeText(this@RequestTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCitySelectionDialog() {
        var tempSelectedCity: String? = selectedCity
        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)
        val originalItems = cities.map { it.name }
        val displayedItems = originalItems.toMutableList()

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_single_choice,
            displayedItems
        ) {
            override fun getCount(): Int = displayedItems.size
            override fun getItem(position: Int): String = displayedItems[position]
        }
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.create_task_select_location_dialog_title))
            .setView(dialogView)
            .setPositiveButton("Patvirtinti", null)
            .setNegativeButton("Atšaukti", null)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            tempSelectedCity = displayedItems[position]
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim()?.lowercase().orEmpty()
                displayedItems.clear()
                displayedItems.addAll(
                    if (query.isEmpty()) originalItems
                    else originalItems.filter { it.lowercase().contains(query) }
                )
                adapter.notifyDataSetChanged()
                listView.clearChoices()
                val selectedIndex = displayedItems.indexOf(tempSelectedCity)
                if (selectedIndex != -1) {
                    listView.setItemChecked(selectedIndex, true)
                }
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = tempSelectedCity != null

                return true
            }
        })
        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val selectedIndex = displayedItems.indexOf(tempSelectedCity)
            if (selectedIndex != -1) {
                listView.setItemChecked(selectedIndex, true)
            } else {
                listView.clearChoices()
            }
            positiveButton.isEnabled = tempSelectedCity != null
            positiveButton.setOnClickListener {
                selectedCity = tempSelectedCity
                citiesTextView.text = selectedCity ?: ""
                citiesTextView.setTextColor(resources.getColor(android.R.color.black, theme))
                alertDialog.dismiss()
            }
        }
        alertDialog.show()
    }

    private fun showDurationSelectionDialog() {
        val durations = arrayOf("1 val.", "2 val.", "3 val.", "4 val.", "5 val.", "6 val.", "7 val.")
        val selectedIndex = selectedDuration?.let { durations.indexOf(it) } ?: -1

        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.create_task_select_duration_dialog_title))
            .setSingleChoiceItems(durations, selectedIndex) { dialog, which ->
                selectedDuration = durations[which]
            }
            .setPositiveButton("Patvirtinti") { dialog, _ ->
                durationTextView.text = selectedDuration ?: getString(R.string.create_task_select_duration_dialog_title)
                durationTextView.setTextColor(resources.getColor(android.R.color.black, theme))
            }
            .setNegativeButton("Atšaukti", null)
        builder.show()
    }

    private fun showCategorySelectionDialog() {
        val originalItems = categories.map { it.name }
        val displayedItems = originalItems.toMutableList()

        var tempSelectedCategoryName: String? = selectedCategoryName
        var tempSelectedCategoryId: Int? = selectedCategoryId

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_single_choice,
            displayedItems
        ) {
            override fun getCount() = displayedItems.size
            override fun getItem(position: Int) = displayedItems[position]
        }

        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.create_task_select_category_dialog_title))
            .setView(dialogView)
            .setPositiveButton("Patvirtinti", null)
            .setNegativeButton("Atšaukti", null)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedName = displayedItems[position]
            val selectedCategory = categories.find { it.name == selectedName }
            tempSelectedCategoryName = selectedCategory?.name
            tempSelectedCategoryId = selectedCategory?.id
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim()?.lowercase().orEmpty()
                displayedItems.clear()
                displayedItems.addAll(
                    if (query.isEmpty()) originalItems
                    else originalItems.filter { it.lowercase().contains(query) }
                )
                adapter.notifyDataSetChanged()
                listView.clearChoices()

                val selectedIndex = displayedItems.indexOf(tempSelectedCategoryName)
                if (selectedIndex != -1) {
                    listView.setItemChecked(selectedIndex, true)
                }

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = tempSelectedCategoryId != null
                return true
            }
        })

        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)

            val selectedIndex = displayedItems.indexOf(tempSelectedCategoryName)
            if (selectedIndex != -1) {
                listView.setItemChecked(selectedIndex, true)
            } else {
                listView.clearChoices()
            }

            positiveButton.isEnabled = tempSelectedCategoryId != null
            positiveButton.setOnClickListener {
                if (tempSelectedCategoryId != null && tempSelectedCategoryName != null) {
                    selectedCategoryId = tempSelectedCategoryId
                    selectedCategoryName = tempSelectedCategoryName
                    categoriesTextView.text = selectedCategoryName
                    categoriesTextView.setTextColor(resources.getColor(android.R.color.black, theme))
                }
                alertDialog.dismiss()
            }
        }
        alertDialog.show()
    }

    private fun fetchTaskerAvailability() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
        val duration = selectedDuration?.replace(" val.", "")?.toIntOrNull() ?: 1
        val cityName = citiesTextView.text.toString()
        val selectedCity = cities.find { it.name == cityName }
        val categoryNames = categoriesTextView.text.toString().split(",").map { it.trim() }
        val selectedCategories = categories.filter { categoryNames.contains(it.name) }

        clearErrors()
        var isValid = true

        if (description.isEmpty()) {
            errorDescription.visibility = View.VISIBLE
            TaskDescription.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            TaskDescription.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }
        if (selectedCity == null) {
            errorCities.visibility = View.VISIBLE
            citiesTextView.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            citiesTextView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }
        if (selectedDuration.isNullOrEmpty()) {
            errorDuration.visibility = View.VISIBLE
            durationTextView.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            durationTextView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }
        if (galleryUris.size > 5) {
            errorPhotos.visibility = View.VISIBLE
            isValid = false
        }

        if (!isValid) return

        val availabilitySlot = AvailabilitySlot(selectedDay!!, selectedTime!!)
        val userid = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1)

        val request = TaskRequestBody(
            description = description,
            city = selectedCity!!,
            categories = selectedCategories,
            duration = duration,
            availability = listOf(availabilitySlot),
            sender_id = userid,
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

        val token = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("auth_token", null) ?: ""
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
                    Toast.makeText(this@RequestTaskActivity, getString(R.string.task_request_send_task_request_successful), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RequestTaskActivity, RequestsOffersActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@RequestTaskActivity, getString(R.string.task_request_failed_send_task_request), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TaskRequestBody>, t: Throwable) {
                Toast.makeText(this@RequestTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun clearErrors(){
        errorDescription.visibility = View.GONE
        errorCities.visibility = View.GONE
        errorDuration.visibility = View.GONE
        errorPhotos.visibility = View.GONE

        TaskDescription.setBackgroundResource(R.drawable.rounded_edittext)
        citiesTextView.setBackgroundResource(R.drawable.rounded_edittext)
        durationTextView.setBackgroundResource(R.drawable.rounded_edittext)
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

    companion object {
        const val IMAGE_PICK_REQUEST = 1002
        const val REQUEST_CODE_STORAGE_PERMISSION= 1003
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
            .setTitle(getString(R.string.create_task_delete_image_dialog_title))
            .setMessage(getString(R.string.create_task_delete_image_dialog_text))
            .setPositiveButton("Taip") { _, _ -> onConfirm() }
            .setNegativeButton("Ne", null)
            .show()
    }
}
