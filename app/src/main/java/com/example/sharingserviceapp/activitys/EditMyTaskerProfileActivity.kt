package com.example.sharingserviceapp.activitys

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.GalleryAdapter
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
import java.io.FileNotFoundException
import java.net.URL

class EditMyTaskerProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var hourlyRateEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var citiesTextView: TextView
    private lateinit var categoriesTextView: TextView
    private lateinit var selectDaysTimeButton: Button
    private lateinit var galleryContainer: LinearLayout

    private var profilePhotoUri: Uri? = null
    private val deletedGalleryUrls = mutableListOf<String>()
    private val galleryUris = mutableListOf<Uri>()
    private val existingGalleryUrls = mutableListOf<String>()

    private var availabilityList: List<String> = listOf()
    private var categories: List<Category> = listOf()
    private var cities: List<City> = listOf()
    private var availabilitySlots: List<AvailabilitySlot> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_my_tasker_profile)
        profileImageView = findViewById(R.id.img_profile_photo)
        descriptionEditText = findViewById(R.id.edit_description)
        hourlyRateEditText = findViewById(R.id.edit_hourly_rate)
        saveButton = findViewById(R.id.btn_submit_tasker_profile)
        citiesTextView = findViewById(R.id.tv_selected_cities)
        categoriesTextView = findViewById(R.id.tv_selected_categories)
        selectDaysTimeButton = findViewById(R.id.btn_select_days_time)
        galleryContainer = findViewById(R.id.gallery_container)

        fetchCategories()
        fetchCities()

        fetchTaskerProfile()

        val backButton: ImageView = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            updateTaskerProfile()
        }

        citiesTextView.setOnClickListener {
            showCitySelectionDialog()
        }

        categoriesTextView.setOnClickListener {
            showCategorySelectionDialog()
        }

        findViewById<Button>(R.id.btn_upload_photo).setOnClickListener {
            checkStoragePermissionAndOpenGallery(isForProfile = true)
        }

        findViewById<Button>(R.id.btn_add_photos).setOnClickListener {
            checkStoragePermissionAndOpenGallery(isForProfile = false)
        }

        selectDaysTimeButton.setOnClickListener {
            val intent = Intent(this, DaysAndTimeActivity::class.java)
            val availabilitySlotList = availabilitySlots.map { "${it.date} ${it.time}" }
            intent.putStringArrayListExtra("PREVIOUS_AVAILABILITY", ArrayList(availabilitySlotList))
            startActivityForResult(intent, REQUEST_CODE_SELECT_DAYS_AND_TIME)
        }
    }

    private fun fetchCategories() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.isSuccessful) {
                    categories = response.body() ?: emptyList()
                } else {
                    Toast.makeText(this@EditMyTaskerProfileActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Toast.makeText(this@EditMyTaskerProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCities() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCities().enqueue(object : Callback<List<City>> {
            override fun onResponse(call: Call<List<City>>, response: Response<List<City>>) {
                if (response.isSuccessful) {
                    cities = response.body() ?: emptyList()
                } else {
                    Toast.makeText(this@EditMyTaskerProfileActivity, "Failed to load cities", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<City>>, t: Throwable) {
                Toast.makeText(this@EditMyTaskerProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchTaskerProfile() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null) ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.getUserTaskerProfile("Bearer $token")
        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(call: Call<TaskerProfileResponse>, response: Response<TaskerProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body()

                    profile?.let {
                        descriptionEditText.setText(it.description)
                        hourlyRateEditText.setText(it.hourly_rate.toString())

                        citiesTextView.text = it.cities.joinToString(", ") { city -> city.name }
                        categoriesTextView.text =
                            it.categories.joinToString(", ") { category -> category.name }

                        val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), it.profile_photo)
                        Glide.with(this@EditMyTaskerProfileActivity)
                            .load(fullImageUrl)
                            .placeholder(R.drawable.placeholder_image_user)
                            .error(R.drawable.error)
                            .circleCrop()
                            .into(profileImageView)

                        profilePhotoUri = Uri.parse(fullImageUrl.toString())
                        val galleryUrls = it.gallery.map { path ->
                            URL(URL(ApiServiceInstance.BASE_URL), path).toString()
                        }

                        existingGalleryUrls.clear()
                        existingGalleryUrls.addAll(galleryUrls)

                        updateGalleryView()

                    }
                } else {
                    Toast.makeText(this@EditMyTaskerProfileActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(this@EditMyTaskerProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCitySelectionDialog() {
        val items = cities.map { it.name }
        val selectedItems = mutableListOf<String>()
        val selectedCityNames = citiesTextView.text.toString().split(",").map { it.trim() }
        selectedItems.addAll(selectedCityNames)

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
            .setTitle("Select Cities")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedPositions = listView.checkedItemPositions
                selectedItems.clear()
                for (i in 0 until listView.count) {
                    if (selectedPositions[i]) {
                        selectedItems.add(items[i])
                    }
                }
                citiesTextView.text = selectedItems.joinToString(", ")
            }
            .setNegativeButton("Cancel", null)

        builder.show()
    }

    private fun showCategorySelectionDialog() {
        val items = categories.map { it.name }
        val selectedItems = mutableListOf<String>()
        val selectedCategoryNames = categoriesTextView.text.toString().split(",").map { it.trim() }
        selectedItems.addAll(selectedCategoryNames)

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
                selectedItems.clear()
                for (i in 0 until listView.count) {
                    if (selectedPositions[i]) {
                        selectedItems.add(items[i])
                    }
                }
                categoriesTextView.text = selectedItems.joinToString(", ")
            }
            .setNegativeButton("Cancel", null)

        builder.show()
    }


    private fun updateTaskerProfile() {
        val description = descriptionEditText.text.toString().trim()
        val hourlyRate = hourlyRateEditText.text.toString().toDouble()

        val selectedCityNames = citiesTextView.text.toString().split(",").map { it.trim() }
        val selectedCities = cities.filter { selectedCityNames.contains(it.name) }

        val selectedCategoryNames = categoriesTextView.text.toString().split(",").map { it.trim() }
        val selectedCategories = categories.filter { selectedCategoryNames.contains(it.name) }



        val taskerProfileRequest = TaskerProfileRequest(
            description = description,
            hourly_rate = hourlyRate,
            categories = selectedCategories,
            cities = selectedCities,
            availability = availabilityList.map {
                val parts = it.split(" ")
                AvailabilitySlot(parts[0], parts[1])
            },
            deletedGalleryImages = deletedGalleryUrls
        )
        if (selectedCities.isEmpty() || selectedCategories.isEmpty()) {
            Toast.makeText(this, "Please select at least one city and one category", Toast.LENGTH_SHORT).show()
            return
        }



        val gson = Gson()
        val taskerProfileJson = gson.toJson(taskerProfileRequest)
        val taskerProfileRequestBody = RequestBody.create("application/json".toMediaTypeOrNull(), taskerProfileJson)


        val profilePhotoPart = profilePhotoUri?.let { uri ->
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

        val galleryParts = galleryUris.mapNotNull { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("gallery_", ".jpg", cacheDir)
                tempFile.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("galleryImages", tempFile.name, requestFile)
            } catch (e: Exception) {
                null
            }
        }

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null) ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.updateTaskerProfile(
            "Bearer $token",
            profileImage = profilePhotoPart,
            taskerProfileJson = taskerProfileRequestBody,
            galleryImages = galleryParts
        )

        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(call: Call<TaskerProfileResponse>, response: Response<TaskerProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditMyTaskerProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@EditMyTaskerProfileActivity, MyTaskerProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@EditMyTaskerProfileActivity, "Error updating profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(this@EditMyTaskerProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PROFILE_IMAGE_PICK_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data?.data != null) {
                    val sourceUri = data.data!!

                    val destinationFile = File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
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
                        Toast.makeText(this, "Cropping failed", Toast.LENGTH_SHORT).show()
                    }
                } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
                    val cropError = UCrop.getError(data)
                    cropError?.printStackTrace()
                    Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
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

            EditMyTaskerProfileActivity.REQUEST_CODE_SELECT_DAYS_AND_TIME -> {
                if (resultCode == RESULT_OK) {
                    val selectedAvailability = data?.getParcelableArrayListExtra<AvailabilitySlot>("SELECTED_AVAILABILITY")
                    if (!selectedAvailability.isNullOrEmpty()) {
                        availabilityList = selectedAvailability.map { "${it.date} ${it.time}" }
                        Toast.makeText(this, "Availability: ${availabilityList.joinToString("\n")}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
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

    private fun openProfilePhotoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,
            EditMyTaskerProfileActivity.PROFILE_IMAGE_PICK_REQUEST
        )
    }

    companion object {
        private const val REQUEST_CODE_SELECT_DAYS_AND_TIME = 101
        private const val IMAGE_PICK_REQUEST = 102
        private const val PROFILE_IMAGE_PICK_REQUEST = 103
        private const val REQUEST_CODE_STORAGE_PERMISSION = 104

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
            .placeholder(R.drawable.placeholder_image_user)
            .error(R.drawable.error)
            .dontTransform()

        fun createImageFrame(imageView: ImageView, onDelete: () -> Unit): FrameLayout {
            val frameLayout = FrameLayout(this@EditMyTaskerProfileActivity).apply {
                layoutParams = linearLayoutParams
            }

            val deleteButton = ImageView(this@EditMyTaskerProfileActivity).apply {
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
            val imageView = ImageView(this@EditMyTaskerProfileActivity).apply {
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
                val imageView = ImageView(this@EditMyTaskerProfileActivity).apply {
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

    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to remove this image?")
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }
}
