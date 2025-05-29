package com.example.sharingserviceapp.activitys

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.UserProfileResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.net.URL

class EditProfileDetailsActivity : AppCompatActivity() {

    private lateinit var editFullname: EditText
    private lateinit var editSurname: EditText
    private lateinit var editBirthdate: TextView
    private lateinit var profileImage: ImageView
    private lateinit var btnUploadPhoto: Button
    private lateinit var errorName: TextView
    private lateinit var errorSurname: TextView
    private lateinit var errorDateOfBirth: TextView
    private lateinit var errorIban: TextView
    private lateinit var editIban: EditText
    private val PROFILE_IMAGE_PICK_REQUEST = 1
    private var profilePhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_details)
        editFullname = findViewById(R.id.edit_fullname)
        editSurname = findViewById(R.id.edit_surname)
        editBirthdate = findViewById(R.id.edit_birthdate)
        profileImage = findViewById(R.id.img_profile_photo)
        btnUploadPhoto = findViewById(R.id.btn_upload_photo)
        errorName = findViewById(R.id.error_name)
        errorSurname = findViewById(R.id.error_surname)
        errorDateOfBirth = findViewById(R.id.error_day_of_birth)
        editIban = findViewById(R.id.edit_iban)
        errorIban = findViewById(R.id.error_iban)
        fetchUserProfile()
        setupListeners()
    }
    private fun setupListeners() {
        val backButton = findViewById<ImageView>(R.id.button_back)
        backButton?.setOnClickListener {
            val intent = Intent(this, ProfileDetailsActivity::class.java)
            startActivity(intent)
            finish()
        }
        val updateButton = findViewById<Button>(R.id.btn_submit_profile)
        updateButton?.setOnClickListener {
            updateProfile()
        }
        editBirthdate.setOnClickListener { showDatePickerDialog() }
        btnUploadPhoto.setOnClickListener { openGallery() }
    }

    private fun fetchUserProfile() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getUserProfile("Bearer $token")

        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    userProfile?.let { user ->
                        editFullname.setText(user.name)
                        editSurname.setText(user.surname)
                        editBirthdate.setText(formatDate(user.date_of_birth))
                        if (user.wallet_bank_iban != null){
                            editIban.setText(user.wallet_bank_iban)
                        }
                        if (user.profile_photo.isNullOrEmpty()) {
                            Glide.with(this@EditProfileDetailsActivity)
                                .load(R.drawable.placeholder_image_user)
                                .circleCrop()
                                .into(profileImage)
                        } else {
                            val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), user.profile_photo)
                            Glide.with(this@EditProfileDetailsActivity)
                                .load(fullImageUrl.toString())
                                .placeholder(R.drawable.placeholder_image_user)
                                .error(R.drawable.error)
                                .circleCrop()
                                .into(profileImage)
                        }
                    }
                } else {
                    Toast.makeText(this@EditProfileDetailsActivity, getString(R.string.edit_profile_details_error_fetch), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@EditProfileDetailsActivity, getString(R.string.edit_profile_details_network_error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return try {
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            dateString
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedYear-${(selectedMonth + 1).toString().padStart(2, '0')}-${
                    selectedDay.toString().padStart(2, '0')}"
                editBirthdate.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PROFILE_IMAGE_PICK_REQUEST)
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
                            .into(profileImage)

                        profilePhotoUri = resultUri
                    } else {
                        Toast.makeText(this, getString(R.string.edit_profile_details_failed_cropping), Toast.LENGTH_SHORT).show()
                    }
                } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
                    val cropError = UCrop.getError(data)
                    cropError?.printStackTrace()
                    Toast.makeText(this, "Error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateProfile() {
        val fullname = editFullname.text.toString().trim()
        val surname = editSurname.text.toString().trim()
        val birthdate = editBirthdate.text.toString().trim()
        val iban = editIban.text.toString().trim()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        clearErrors()
        var isValid = true

        if (fullname.isEmpty()) {
            errorName.visibility = View.VISIBLE
            editFullname.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            editFullname.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }
        if (surname.isEmpty()) {
            errorSurname.visibility = View.VISIBLE
            editSurname.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            editSurname.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }
        if (birthdate.isEmpty()) {
            errorDateOfBirth.visibility = View.VISIBLE
            editBirthdate.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            editBirthdate.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }else {
            val dobDate = try {
                dateFormat.parse(birthdate)
            } catch (e: Exception) {
                null
            }
            if (dobDate != null) {
                val calendar = Calendar.getInstance().apply { add(Calendar.YEAR, -16) }
                if (dobDate.after(calendar.time)) {
                    errorDateOfBirth.text = getString(R.string.reg_error_smallest_year_old_date_of_birth)
                    val scale = resources.displayMetrics.density
                    val paddingInPx = (12 * scale + 0.5f).toInt()
                    editBirthdate.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
                    isValid = false
                }
            }
        }
        if (iban.isNotEmpty() && iban.length < 20) {
            errorIban.visibility = View.VISIBLE
            editIban.setBackgroundResource(R.drawable.spinner_border_error)
            val scale = resources.displayMetrics.density
            val paddingInPx = (12 * scale + 0.5f).toInt()
            editIban.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, getString(R.string.edit_my_tasker_profile_error_fill_field), Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null) ?: ""

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val profilePhoto = profilePhotoUri?.let { uri ->
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

        val profileData = mapOf(
            "fullname" to fullname,
            "surname" to surname,
            "birthdate" to birthdate,
            "wallet_bank_iban" to iban
        )

        val gson = Gson()
        val jsonProfileData = gson.toJson(profileData)
        val body= RequestBody.create("application/json".toMediaTypeOrNull(), jsonProfileData)

        val call = ApiServiceInstance.Auth.apiServices.updateUserProfile(
            "Bearer $token",
            profilePhoto,
            body
        )

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditProfileDetailsActivity, getString(R.string.edit_profile_details_update_successful), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@EditProfileDetailsActivity, ProfileDetailsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@EditProfileDetailsActivity, getString(R.string.edit_profile_details_update_failed), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EditProfileDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun clearErrors() {
        errorName.visibility = View.GONE
        editFullname.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext)
        errorSurname.visibility = View.GONE
        editSurname.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext)
        errorDateOfBirth.visibility = View.GONE
        editBirthdate.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext)
        errorIban.visibility = View.GONE
        editIban.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext)
    }

}
