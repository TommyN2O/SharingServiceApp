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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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

    private val deletedGalleryUrls = mutableListOf<String>()
    private val existingGalleryUrls = mutableListOf<String>()
    private var profilePhotoUri: Uri? = null
    private var galleryUris: MutableList<Uri> = mutableListOf()
    private var availabilityList: List<String> = listOf()
    private var categories: List<Category> = listOf()
    private var cities: List<City> = listOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creat_my_tasker_profile)

        profileImageView = findViewById(R.id.img_profile_photo)
        descriptionEditText = findViewById(R.id.edit_description)
        hourlyRateEditText = findViewById(R.id.edit_hourly_rate)
        saveButton = findViewById(R.id.btn_submit_tasker_profile)
        citiesTextView = findViewById(R.id.tv_selected_cities)
        categoriesTextView = findViewById(R.id.tv_selected_categories)

        citiesTextView.setOnClickListener {
            showCitySelectionDialog()
        }

        categoriesTextView.setOnClickListener {
            showCategorySelectionDialog()
        }

        fetchCategories()
        fetchCities()

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
            navigateToMoreActivity()
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

    private fun navigateToMoreActivity() {
        val intent = Intent(this, MoreActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showCitySelectionDialog() {
        val items = cities.map { it.name }
        val selectedItems = mutableListOf<String>()
        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items)
        listView.adapter = adapter

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

                val selectedCitiesText = selectedItems.joinToString(", ")
                val citiesTextView = findViewById<TextView>(R.id.tv_selected_cities)
                citiesTextView.text = selectedCitiesText
            }
            .setNegativeButton("Cancel", null)

        builder.create().show()
    }


    private fun showCategorySelectionDialog() {
        val items = categories.map { it.name }

        val selectedItems = mutableListOf<String>()

        val dialogView = layoutInflater.inflate(R.layout.dialog_multiselect, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items)
        listView.adapter = adapter

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

                val selectedCategorysText = selectedItems.joinToString(", ")
                val citiesTextView = findViewById<TextView>(R.id.tv_selected_categories)
                citiesTextView.text = selectedCategorysText
            }
            .setNegativeButton("Cancel", null)

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
                        Toast.makeText(this, "Cropping failed", Toast.LENGTH_SHORT).show()
                    }
                } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
                    val cropError = UCrop.getError(data)
                    cropError?.printStackTrace()
                    Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT)
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
                Toast.makeText(this, "Availability: ${availabilityList.joinToString("\n")}", Toast.LENGTH_LONG).show()
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

        if (description.isEmpty() || hourlyRate == null || selectedCities.isEmpty() || selectedCategories.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select at least one city and category", Toast.LENGTH_SHORT).show()
            return
        }

        val taskerProfileRequest = TaskerProfileRequest(
            description = description,
            hourly_rate = hourlyRate,
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

        if (token.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.createTaskerProfile(
            "Bearer $token",
            profileImage = profilePhotoPart, // null if no image!
            taskerProfileJson = taskerProfileRequestBody,
            galleryImages = galleryParts
        )

        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(call: Call<TaskerProfileResponse>, response: Response<TaskerProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateMyTaskerProfileActivity, "Profile created successfully!", Toast.LENGTH_SHORT).show()
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
    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to remove this image?")
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }


}