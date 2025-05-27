package com.example.sharingserviceapp.activitys

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.EditMyTaskerProfileActivity
import com.example.sharingserviceapp.models.AvailabilitySlot
import com.example.sharingserviceapp.models.Category
import com.example.sharingserviceapp.models.City
import com.example.sharingserviceapp.models.TaskerProfileRequest
import com.example.sharingserviceapp.models.TaskerProfileResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CreateMyTaskerProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var hourlyRateEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var citiesTextView: TextView
    private lateinit var categoriesTextView: TextView
    private lateinit var errorDescription: TextView
    private lateinit var errorCities: TextView
    private lateinit var errorCategory: TextView
    private lateinit var errorHourlyRate: TextView

    private val deletedGalleryUrls = mutableListOf<String>()
    private val existingGalleryUrls = mutableListOf<String>()
    private var profilePhotoUri: Uri? = null
    private var galleryUris: MutableList<Uri> = mutableListOf()
    private var availabilityList: List<String> = listOf()
    private var categories: List<Category> = listOf()
    private var cities: List<City> = listOf()
    private val selectedCityNames = mutableListOf<String>()
    private val selectedCategoryNames = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creat_my_tasker_profile)
        profileImageView = findViewById(R.id.img_profile_photo)
        descriptionEditText = findViewById(R.id.edit_description)
        hourlyRateEditText = findViewById(R.id.edit_hourly_rate)
        saveButton = findViewById(R.id.btn_submit_tasker_profile)
        citiesTextView = findViewById(R.id.tv_selected_cities)
        categoriesTextView = findViewById(R.id.tv_selected_categories)
        errorDescription = findViewById(R.id.error_description)
        errorCities = findViewById(R.id.error_cities)
        errorCategory = findViewById(R.id.error_category)
        errorHourlyRate = findViewById(R.id.error_hourly_rate)

        fetchCategories()
        fetchCities()
        setupListeners()
    }
    private fun setupListeners(){

        citiesTextView.setOnClickListener {
            showCitySelectionDialog()
        }
        categoriesTextView.setOnClickListener {
            showCategorySelectionDialog()
        }
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.btn_upload_photo).setOnClickListener {
            checkStoragePermissionAndOpenGallery(isForProfile = true)
        }
        findViewById<Button>(R.id.btn_add_photos).setOnClickListener {
            checkStoragePermissionAndOpenGallery(isForProfile = false)
        }
        saveButton.setOnClickListener {
            createTaskerProfile()
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

    private fun openProfilePhotoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PROFILE_IMAGE_PICK_REQUEST)
    }

    private fun showCitySelectionDialog() {
        val originalItems = cities.map { it.name }
        val selectedItems = mutableSetOf<String>()

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)
        val displayedItems = originalItems.toMutableList()

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            displayedItems
        ) {
            override fun getCount(): Int = displayedItems.size
            override fun getItem(position: Int): String = displayedItems[position]
        }
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        selectedItems.addAll(selectedCityNames)
        fun refreshCheckedStates() {
            for (i in displayedItems.indices) {
                val item = displayedItems[i]
                listView.setItemChecked(i, selectedItems.contains(item))
            }
        }
        refreshCheckedStates()
        listView.setOnItemClickListener { _, _, position, _ ->
            val clickedItem = displayedItems[position]
            if (selectedItems.contains(clickedItem)) {
                selectedItems.remove(clickedItem)
            } else {
                selectedItems.add(clickedItem)
            }
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim()?.lowercase() ?: ""
                displayedItems.clear()
                displayedItems.addAll(
                    if (query.isEmpty()) originalItems
                    else originalItems.filter { it.lowercase().contains(query) }
                )
                adapter.notifyDataSetChanged()
                refreshCheckedStates()
                return true
            }
        })
        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.create_my_tasker_select_city_dialog_title))
            .setView(dialogView)
            .setPositiveButton("Patvirtinti") { _, _ ->
                selectedCityNames.clear()
                selectedCityNames.addAll(selectedItems)

                val selectedCitiesText = selectedItems.joinToString(", ")
                val citiesTextView = findViewById<TextView>(R.id.tv_selected_cities)
                citiesTextView.text = selectedCitiesText
            }
            .setNegativeButton("Atšaukti", null)
        builder.create().show()
    }

    private fun showCategorySelectionDialog() {
        val originalItems = categories.map { it.name }
        val displayedItems = originalItems.toMutableList()

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            displayedItems
        ) {
            override fun getCount() = displayedItems.size
            override fun getItem(position: Int) = displayedItems[position]
        }

        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        fun restoreCheckedStates() {
            listView.clearChoices()
            for (i in displayedItems.indices) {
                if (selectedCategoryNames.contains(displayedItems[i])) {
                    listView.setItemChecked(i, true)
                }
            }
            adapter.notifyDataSetChanged()
        }
        restoreCheckedStates()
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = displayedItems[position]
            if (listView.isItemChecked(position)) {
                selectedCategoryNames.add(item)
            } else {
                selectedCategoryNames.remove(item)
            }
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
                restoreCheckedStates()
                return true
            }
        })

        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.create_my_tasker_select_category_dialog_title))
            .setView(dialogView)
            .setPositiveButton("Patvirtinti") { _, _ ->
                val categoriesTextView = findViewById<TextView>(R.id.tv_selected_categories)
                categoriesTextView.text = selectedCategoryNames.joinToString(", ")
            }
            .setNegativeButton("Atšaukti", null)
        builder.create().show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PROFILE_IMAGE_PICK_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data?.data != null) {
                    val sourceUri = data.data!!

                    val destinationFile =
                        File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
                    val destinationUri = Uri.fromFile(destinationFile)

                    val options = UCrop.Options().apply {
                        setCircleDimmedLayer(true)
                        setShowCropFrame(false)
                        setShowCropGrid(false)
                        setCompressionFormat(Bitmap.CompressFormat.JPEG)
                        setCompressionQuality(90)
                    }

                    UCrop.of(sourceUri, destinationUri)
                        .withAspectRatio(1f, 1f)
                        .withMaxResultSize(200, 200)
                        .withOptions(options)
                        .start(this)
                }
            }

            UCrop.REQUEST_CROP -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val resultUri = UCrop.getOutput(data)
                    if (resultUri != null) {
                        Glide.with(this)
                            .load(resultUri)
                            .circleCrop()
                            .into(profileImageView)

                        profilePhotoUri = resultUri
                    } else {
                        Toast.makeText(this, getString(R.string.create_my_tasker_failed_cropping), Toast.LENGTH_SHORT).show()
                    }
                } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
                    val cropError = UCrop.getError(data)
                    cropError?.printStackTrace()
                    Toast.makeText(this, "Error: ${cropError?.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            IMAGE_PICK_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (data.clipData != null) {
                        val count = data.clipData!!.itemCount
                        for (i in 0 until count) {
                            val imageUri = data.clipData!!.getItemAt(i).uri
                            if (!galleryUris.contains(imageUri)) {
                                galleryUris.add(imageUri)
                            }
                        }
                    } else if (data.data != null) {
                        val imageUri = data.data!!
                        if (!galleryUris.contains(imageUri)) {
                            galleryUris.add(imageUri)
                        }
                    }
                    updateGalleryView()
                }
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

    private fun createTaskerProfile() {
        val description = descriptionEditText.text.toString().trim()
        val hourlyRate = hourlyRateEditText.text.toString().toDoubleOrNull()
        val selectedCityNames = citiesTextView.text.toString().split(",").map { it.trim() }
        val selectedCities = cities.filter { selectedCityNames.contains(it.name) }
        val selectedCategoryNames = categoriesTextView.text.toString().split(",").map { it.trim() }
        val selectedCategories = categories.filter { selectedCategoryNames.contains(it.name) }

        clearErrors()
        var isValid = true

        if (description.isEmpty()) {
            errorDescription.visibility = View.VISIBLE
            descriptionEditText.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            descriptionEditText.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }

        if (hourlyRate == null || hourlyRate <= 0) {
            errorHourlyRate.visibility = View.VISIBLE
            hourlyRateEditText.background = ContextCompat.getDrawable(this, R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            hourlyRateEditText.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }

        if (selectedCategories.isEmpty()) {
            errorCategory.visibility = View.VISIBLE
            categoriesTextView.background = ContextCompat.getDrawable(this, R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            categoriesTextView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }

        if (selectedCities.isEmpty()) {
            errorCities.visibility = View.VISIBLE
            citiesTextView.background = ContextCompat.getDrawable(this, R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            citiesTextView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, getString(R.string.create_my_tasker_error_fill_field), Toast.LENGTH_SHORT).show()
            return
        }

        val taskerProfileRequest = TaskerProfileRequest(
            description = description,
            hourly_rate = hourlyRate!!,
            categories = selectedCategories,
            cities = selectedCities,
            availability = availabilityList.map {
                val parts = it.split(" ")
                AvailabilitySlot(parts[0], parts[1])
            }
        )

        val gson = Gson()
        val taskerProfileJson = gson.toJson(taskerProfileRequest)
        val taskerProfileRequestBody = taskerProfileJson.toRequestBody("application/json".toMediaTypeOrNull())

        val profilePhotoPart: MultipartBody.Part? = profilePhotoUri?.let { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("profile_", ".jpg", cacheDir)
                tempFile.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profile_photo", tempFile.name, requestFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        val galleryParts = galleryUris.map { uri ->
            val file = File(getRealPathFromURI(uri))
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("galleryImages", file.name, requestFile)
        }

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null) ?: ""

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.createTaskerProfile(
            "Bearer $token",
            profileImage = profilePhotoPart,
            taskerProfileJson = taskerProfileRequestBody,
            galleryImages = galleryParts
        )

        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(call: Call<TaskerProfileResponse>, response: Response<TaskerProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, getString(R.string.create_my_tasker_created_successful), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@CreateMyTaskerProfileActivity, MoreActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, getString(R.string.create_my_tasker_failed_create), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(this@CreateMyTaskerProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun clearErrors() {
        errorCities.visibility = View.GONE
        citiesTextView.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext)
        errorCategory.visibility = View.GONE
        categoriesTextView.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext)
        errorHourlyRate.visibility = View.GONE
        hourlyRateEditText.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext)
        errorDescription.visibility = View.GONE
        descriptionEditText.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext)
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

    private fun fetchCategories() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.isSuccessful) {
                    categories = response.body() ?: emptyList()

                    val categoryNames = categories.map { it.name }
                    val adapter = ArrayAdapter(this@CreateMyTaskerProfileActivity, android.R.layout.simple_spinner_item, categoryNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                } else {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, getString(R.string.create_my_tasker_failed_load_categories), Toast.LENGTH_SHORT).show()
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
                    val cityNames = cities.map { it.name }
                    val adapter = ArrayAdapter(this@CreateMyTaskerProfileActivity, android.R.layout.simple_spinner_item, cityNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                } else {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, getString(R.string.create_my_tasker_failed_load_cities), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<City>>, t: Throwable) {
                Toast.makeText(this@CreateMyTaskerProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateGalleryView() {
        val galleryContainer = findViewById<LinearLayout>(R.id.gallery_container)
        galleryContainer.removeAllViews()
        val linearLayoutParams = LinearLayout.LayoutParams(200, 200).apply {
            setMargins(10, 10, 10, 10)
        }

        val existingSet = existingGalleryUrls.map { it.toString() }.toSet()
        val glideOptions = RequestOptions()
            .override(300, 300)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error)
            .dontTransform()

        fun createImageFrame(imageView: ImageView, onDelete: () -> Unit): FrameLayout {
            val frameLayout = FrameLayout(this@CreateMyTaskerProfileActivity).apply {
                layoutParams = linearLayoutParams
            }

            val deleteButton = ImageView(this@CreateMyTaskerProfileActivity).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                backgroundTintList = getColorStateList(android.R.color.holo_red_dark)
                layoutParams = FrameLayout.LayoutParams(60, 60, Gravity.END or Gravity.TOP).apply {
                    setMargins(5, 5, 5, 5)
                }
                setOnClickListener {
                    showDeleteConfirmationDialog(onDelete)
                }
            }
            frameLayout.addView(imageView)
            frameLayout.addView(deleteButton)
            return frameLayout
        }

        for (url in existingGalleryUrls) {
            val imageView = ImageView(this@CreateMyTaskerProfileActivity).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            Glide.with(this)
                .load(url)
                .apply(glideOptions)
                .into(imageView)

            val frame = createImageFrame(imageView) {
                deletedGalleryUrls.add(url)
                existingGalleryUrls.remove(url)

                updateGalleryView()
            }

            galleryContainer.addView(frame)
        }

        for (uri in galleryUris) {
            if (uri.toString() !in existingSet) {
                val imageView = ImageView(this@CreateMyTaskerProfileActivity).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }

                Glide.with(this)
                    .load(uri)
                    .apply(glideOptions)
                    .into(imageView)

                val frame = createImageFrame(imageView) {
                    galleryUris.remove(uri)
                    updateGalleryView()
                }

                galleryContainer.addView(frame)
            }
        }
    }
     fun showDeleteConfirmationDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.create_my_tasker_delete_image_dialog_title))
            .setMessage(getString(R.string.create_my_tasker_delete_image_dialog_text))
            .setPositiveButton("Taip") { _, _ -> onConfirm() }
            .setNegativeButton("Ne", null)
            .show()
    }
}