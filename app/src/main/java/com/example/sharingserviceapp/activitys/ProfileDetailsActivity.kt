package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.example.sharingserviceapp.models.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ProfileDetailsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var editButton: TextView
    private lateinit var textName: TextView
    private lateinit var textSurname: TextView
    private lateinit var textBirthdate: TextView
    private lateinit var profileImage: ImageView
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_details)

        backButton = findViewById(R.id.button_back)
        editButton = findViewById(R.id.text_edit)
        textName = findViewById(R.id.text_name)
        textSurname = findViewById(R.id.text_surname)
        textBirthdate = findViewById(R.id.text_birthdate)
        profileImage = findViewById(R.id.img_profile_photo)
        deleteButton = findViewById(R.id.btn_delete_account)

        setUpButtonListeners()

        fetchUserProfile()
    }

    private fun setUpButtonListeners() {

        backButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        editButton.setOnClickListener {
            val intent = Intent(this, EditProfileDetailsActivity::class.java)
            startActivity(intent)
            finish()
        }

        deleteButton.setOnClickListener {
            Toast.makeText(this, "Account deletion triggered", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserProfile() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            val api = ApiServiceInstance.Auth.apiServices
            val call = api.getUserProfile("Bearer $token")

            call.enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    if (response.isSuccessful) {
                        val userProfile = response.body()
                        userProfile?.let {

                            textName.text = it.name
                            textSurname.text = it.surname
                            textBirthdate.text = formatDate(it.date_of_birth)


                            if (it.profile_photo.isNullOrEmpty()) {
                                Glide.with(this@ProfileDetailsActivity)
                                    .load(R.drawable.placeholder_image_user)
                                    .circleCrop()
                                    .into(profileImage)
                            } else {
                                val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), it.profile_photo)

                                Glide.with(this@ProfileDetailsActivity)
                                    .load(fullImageUrl.toString())
                                    .placeholder(R.drawable.placeholder_image_user)
                                    .error(R.drawable.error)
                                    .circleCrop()
                                    .into(profileImage)
                            }
                        }
                    } else {
                        Toast.makeText(this@ProfileDetailsActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileDetailsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
    fun formatDate(dateString: String): String {
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
}
