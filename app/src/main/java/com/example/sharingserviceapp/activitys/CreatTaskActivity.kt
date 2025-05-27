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
import android.view.View
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
    private lateinit var btnSelectDaysTime: Button
    private lateinit var backButton: ImageView
    private lateinit var galleryContainer: LinearLayout
    private lateinit var errorDescription: TextView
    private lateinit var errorBudget: TextView
    private lateinit var errorCities: TextView
    private lateinit var errorCategory: TextView
    private lateinit var errorDuration: TextView
    private lateinit var errorAvailability: TextView
    private lateinit var errorPhotos: TextView

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
        TaskDescription = findViewById(R.id.edit_task_description)
        citiesTextView = findViewById(R.id.tv_selected_cities)
        categoriesTextView = findViewById(R.id.tv_selected_categories)
        durationTextView = findViewById(R.id.tv_selected_duration)
        budgetEditText = findViewById(R.id.edit_budget)
        galleryContainer = findViewById(R.id.gallery_container)
        errorDescription = findViewById(R.id.error_description)
        errorBudget = findViewById(R.id.error_budget)
        errorCities = findViewById(R.id.error_cities)
        errorCategory = findViewById(R.id.error_category)
        errorDuration = findViewById(R.id.error_duration)
        errorAvailability = findViewById(R.id.error_availability)
        errorPhotos = findViewById(R.id.error_photos)
        btnAddPhoto = findViewById(R.id.btn_add_photo)
        btnCreateRequest = findViewById(R.id.btn_create_request)
        backButton = findViewById(R.id.btn_back)
        btnSelectDaysTime =findViewById(R.id.btn_select_days_time)
        fetchCategories()
        fetchCities()
        setupListeners()
    }

    private fun setupListeners() {
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
                        Toast.makeText(this@CreatTaskActivity, getString(R.string.create_task_failed_load_category), Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@CreatTaskActivity, getString(R.string.create_task_failed_load_cities), Toast.LENGTH_SHORT).show()
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

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.create_task_select_duration_dialog_title))
            .setSingleChoiceItems(durations, selectedIndex) { _, which ->
                selectedDuration = durations[which]
            }
            .setPositiveButton("Patvirtinti") { _, _ ->
                durationTextView.text = selectedDuration ?: ""
                durationTextView.setTextColor(resources.getColor(android.R.color.black, theme))
            }
            .setNegativeButton("Atšaukti", null)
            .show()
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
                if (tempSelectedCategoryId != null) {
                    selectedCategoryName = tempSelectedCategoryName
                    selectedCategoryId = tempSelectedCategoryId
                    categoriesTextView.text = selectedCategoryName
                    categoriesTextView.setTextColor(resources.getColor(android.R.color.black, theme))
                }
                alertDialog.dismiss()
            }
        }
        alertDialog.show()
    }

    private fun createOpenTask() {
        val description = TaskDescription.text.toString().trim()
        val duration = selectedDuration?.replace(" val.", "")?.toIntOrNull() ?: 1
        val budgetText = budgetEditText.text.toString().toDoubleOrNull()
        val cityName = citiesTextView.text.toString()
        val selectedCity = cities.find { it.name == cityName }

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

        if (budgetText == null || budgetText <= 0) {
            errorBudget.visibility = View.VISIBLE
            budgetEditText.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            budgetEditText.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
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

        if (selectedCategoryId == null) {
            errorCategory.visibility = View.VISIBLE
            categoriesTextView.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            categoriesTextView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
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

        if (availabilityList.isEmpty()) {
            errorAvailability.visibility = View.VISIBLE
            isValid = false
        }

        if (galleryUris.size > 5) {
            errorPhotos.visibility = View.VISIBLE
            isValid = false
        }
        if (!isValid) return

        val request = OpenTaskBody(
            description = description,
            location_id = selectedCity!!.id,
            category_id = selectedCategoryId!!,
            duration = duration,
            budget = budgetText!!,
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

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiServiceInstance.Auth.apiServices.createOpenTask(
            "Bearer $token",
            requestBody,
            galleryParts
        ).enqueue(object : Callback<OpenTaskBody> {
            override fun onResponse(call: Call<OpenTaskBody>, response: Response<OpenTaskBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreatTaskActivity, getString(R.string.create_task_open_task_created), Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@CreatTaskActivity, MoreActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CreatTaskActivity, getString(R.string.create_task_open_task_failed_create), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<OpenTaskBody>, t: Throwable) {
                Toast.makeText(this@CreatTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearErrors(){
        errorDescription.visibility = View.GONE
        errorBudget.visibility = View.GONE
        errorCities.visibility = View.GONE
        errorCategory.visibility = View.GONE
        errorDuration.visibility = View.GONE
        errorAvailability.visibility = View.GONE
        errorPhotos.visibility = View.GONE

        TaskDescription.setBackgroundResource(R.drawable.rounded_edittext)
        budgetEditText.setBackgroundResource(R.drawable.rounded_edittext)
        citiesTextView.setBackgroundResource(R.drawable.rounded_edittext)
        categoriesTextView.setBackgroundResource(R.drawable.rounded_edittext)
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
                Toast.makeText(this, "Data ir laikas: ${availabilityList.joinToString("\n")}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
