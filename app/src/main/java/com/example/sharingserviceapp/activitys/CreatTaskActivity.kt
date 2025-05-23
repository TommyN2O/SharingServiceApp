package com.example.sharingserviceapp.activitys

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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.CreateMyTaskerProfileActivity.Companion.REQUEST_CODE_SELECT_DAYS_AND_TIME
import com.example.sharingserviceapp.activitys.RequestTaskActivity.Companion.IMAGE_PICK_REQUEST
import com.example.sharingserviceapp.activitys.RequestTaskActivity.Companion.REQUEST_CODE_STORAGE_PERMISSION
import com.example.sharingserviceapp.models.*
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

class CreatTaskActivity : AppCompatActivity() {

    private lateinit var TaskDescription: EditText
    private lateinit var categoriesTextView: TextView
    private lateinit var citiesTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var budgetEditText: EditText
    private lateinit var btnAddPhoto: Button
    private lateinit var btnCreateRequest: Button
    private lateinit var backButton: ImageView
    private lateinit var galleryContainer: LinearLayout

    private var selectedCategoryName: String? = null
    private val galleryUris = mutableListOf<Uri>()
    private var selectedCity: String? = null
    private var selectedDuration: String? = null
    private var selectedCategoryId: Int? = null
    private var categories = listOf<Category>()
    private var cities = listOf<City>()
    private var availabilityList: List<String> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creat_task)
        fetchCategories()
        fetchCities()
        setupListeners()
    }

    private fun setupListeners() {
        TaskDescription = findViewById(R.id.edit_task_description)
        citiesTextView = findViewById(R.id.tv_selected_cities)
        categoriesTextView = findViewById(R.id.tv_selected_categories)
        durationTextView = findViewById(R.id.tv_selected_duration)
        btnAddPhoto = findViewById(R.id.btn_add_photo)
        budgetEditText = findViewById(R.id.edit_budget)
        btnCreateRequest = findViewById(R.id.btn_create_request)
        galleryContainer = findViewById(R.id.gallery_container)
        backButton = findViewById(R.id.btn_back)

        backButton.setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
            finish()
        }

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
        btnCreateRequest.setOnClickListener {
            createOpenTask()


        }

        val btnSelectDaysTime = findViewById<Button>(R.id.btn_select_days_time)
        btnSelectDaysTime.setOnClickListener {
            val availabilitySlotList = availabilityList.map {
                val parts = it.split(" ")
                AvailabilitySlot(parts[0], parts[1])
            }
            val intent = Intent(this, DaysAndTimeActivity::class.java)
            intent.putParcelableArrayListExtra("PREVIOUS_AVAILABILITY", ArrayList(availabilitySlotList))
            startActivityForResult(intent, REQUEST_CODE_SELECT_DAYS_AND_TIME)
        }
    }

    private fun fetchCategories() {
        ApiServiceInstance.Auth.apiServices.getCategories()
            .enqueue(object : Callback<List<Category>> {
                override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                    if (response.isSuccessful) {
                        categories = response.body() ?: emptyList()
                        setSelectedCategory()
                    } else {
                        Toast.makeText(this@CreatTaskActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                    Toast.makeText(this@CreatTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchCities() {
        ApiServiceInstance.Auth.apiServices.getCities()
            .enqueue(object : Callback<List<City>> {
                override fun onResponse(call: Call<List<City>>, response: Response<List<City>>) {
                    if (response.isSuccessful) {
                        cities = response.body() ?: emptyList()
                    } else {
                        Toast.makeText(this@CreatTaskActivity, "Failed to load cities", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<City>>, t: Throwable) {
                    Toast.makeText(this@CreatTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setSelectedCategory() {
        val selectedCategory = categories.find { it.id == selectedCategoryId }
        selectedCategory?.let {
            categoriesTextView.text = it.name
        }
    }

    private fun showCitySelectionDialog() {
        val items = cities.map { it.name }
        var tempSelectedCity: String? = selectedCity

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

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

        AlertDialog.Builder(this)
            .setTitle("Select Location")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                selectedCity = tempSelectedCity
                citiesTextView.text = selectedCity ?: "Select City"
                citiesTextView.setTextColor(resources.getColor(android.R.color.black, theme))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDurationSelectionDialog() {
        val durations = arrayOf("1h", "2h", "3h", "4h", "5h", "6h", "7h")
        val selectedIndex = selectedDuration?.let { durations.indexOf(it) } ?: -1

        AlertDialog.Builder(this)
            .setTitle("Select Duration")
            .setSingleChoiceItems(durations, selectedIndex) { _, which ->
                selectedDuration = durations[which]
            }
            .setPositiveButton("OK") { _, _ ->
                durationTextView.text = selectedDuration ?: "Select Duration"
                durationTextView.setTextColor(resources.getColor(android.R.color.black, theme))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCategorySelectionDialog() {
        val items = categories.map { it.name }
        var tempSelectedCategoryName: String? = selectedCategoryName
        var tempSelectedCategoryId: Int? = selectedCategoryId

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, items)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        selectedCategoryName?.let {
            val index = items.indexOf(it)
            if (index != -1) {
                listView.setItemChecked(index, true)
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = categories[position]
            tempSelectedCategoryName = selected.name
            tempSelectedCategoryId = selected.id
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        AlertDialog.Builder(this)
            .setTitle("Select Category")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                if (tempSelectedCategoryId != null) {
                    selectedCategoryName = tempSelectedCategoryName
                    selectedCategoryId = tempSelectedCategoryId
                    categoriesTextView.text = selectedCategoryName
                    categoriesTextView.setTextColor(resources.getColor(android.R.color.black, theme))
                } else {
                    Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createOpenTask() {
        val description = TaskDescription.text.toString().trim()
        val duration = selectedDuration?.replace("h", "")?.toIntOrNull() ?: 1
        val budgetText = budgetEditText.text.toString().toDoubleOrNull()
        val cityName = citiesTextView.text.toString()
        val selectedCity = cities.find { it.name == cityName }

        if (selectedCity == null) {
            Toast.makeText(this, "Invalid city", Toast.LENGTH_SHORT).show()
            return
        }
        val locationId = selectedCity.id

        if (selectedCategoryId == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        if (budgetText == null) {
            Toast.makeText(this, "Please enter a valid budget", Toast.LENGTH_SHORT).show()
            return
        }

        val request = OpenTaskBody(
            description = description,
            location_id = locationId,
            category_id = selectedCategoryId!!,
            duration = duration,
            budget =budgetText,
            availability = availabilityList.map { item ->
                val parts = item.split(" ")
                AvailabilitySlot(parts[0], parts[1])
            }

        )

        val gson = Gson()
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

        val galleryParts = galleryUris.mapIndexed { _, uri ->
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

        ApiServiceInstance.Auth.apiServices.createOpenTask(
            "Bearer $token",
            requestBody,
            galleryParts
        ).enqueue(object : Callback<OpenTaskBody> {
            override fun onResponse(call: Call<OpenTaskBody>, response: Response<OpenTaskBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreatTaskActivity, "Request sent!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@CreatTaskActivity, MoreActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CreatTaskActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OpenTaskBody>, t: Throwable) {
                Toast.makeText(this@CreatTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_REQUEST) {
            data?.let {
                if (it.clipData != null) {
                    val count = it.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = it.clipData!!.getItemAt(i).uri
                        galleryUris.add(imageUri)
                    }
                } else if (it.data != null) {
                    val imageUri = it.data!!
                    galleryUris.add(imageUri)
                }
                displayImagesInGallery()
            }
        }

        if (requestCode == REQUEST_CODE_SELECT_DAYS_AND_TIME && resultCode == RESULT_OK) {
            val selectedAvailability = data?.getParcelableArrayListExtra<AvailabilitySlot>("SELECTED_AVAILABILITY")
            if (selectedAvailability != null) {
                availabilityList = selectedAvailability.map { "${it.date} ${it.time}" }
                Toast.makeText(this, "Availability: ${availabilityList.joinToString("\n")}", Toast.LENGTH_LONG).show()
            }
        }
    }

}
