package com.example.sharingserviceapp.activitys

import android.app.AlertDialog
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
    private lateinit var editButton: ImageView
    private lateinit var textName: TextView
    private lateinit var textSurname: TextView
    private lateinit var textBirthdate: TextView
    private lateinit var profileImage: ImageView
    private lateinit var deleteButton: Button
    private lateinit var textEmail: TextView
    private lateinit var textIban: TextView

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
        textIban = findViewById(R.id.text_iban)
        textEmail = findViewById(R.id.text_email)

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
            deleteUserProfile()
        }
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
                    userProfile?.let {
                        textName.text = it.name
                        textSurname.text = it.surname
                        textBirthdate.text = formatDate(it.date_of_birth)
                        textEmail.text = it.email
                        textIban.text = it.wallet_bank_iban
                        if(textIban == null)
                        {
                            textIban.text =" "
                        }
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
                    Toast.makeText(this@ProfileDetailsActivity, getString(R.string.profile_details_error_fetch), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@ProfileDetailsActivity, getString(R.string.profile_details_network_error), Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun deleteUserProfile() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val api = ApiServiceInstance.Auth.apiServices
        val call = api.deleteUserProfile("Bearer $token")
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        remove("auth_token")
                        remove("user_id")
                        remove("email")
                        remove("password")
                        remove("password")
                        apply()
                    }
                    startActivity(Intent(this@ProfileDetailsActivity, MainActivity::class.java))
                    finish()
                } else {
                    if(response.code() == 605)
                    {
                        showActiveTaskerTasksDialog()
                    }
                    else if(response.code() == 606)
                    {
                        showActiveUserTasksDialog()
                    }
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProfileDetailsActivity, getString(R.string.profile_details_network_error), Toast.LENGTH_SHORT).show()
            }
        })
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
    private fun showActiveTaskerTasksDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.my_tasker_profile_delete_profile_dialog_header))
            .setMessage(getString(R.string.my_tasker_profile_delete_profile_dialog_text))
            .setPositiveButton("Patvirtinti") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
    private fun showActiveUserTasksDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.profile_delete_profile_dialog_header))
            .setMessage(getString(R.string.profile_delete_profile_dialog_text))
            .setPositiveButton("Patvirtinti") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
