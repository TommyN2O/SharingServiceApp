package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.GalleryAdapter
import com.example.sharingserviceapp.models.AvailabilitySlot
import com.example.sharingserviceapp.models.TaskerProfileResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import android.app.Dialog
import android.view.View
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody


class MyTaskerProfileActivity : AppCompatActivity() {

    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tasker_profile)

        // Setup view references
        val menuButton: ImageView = findViewById(R.id.menu_button)
        val readMoreButton: TextView = findViewById(R.id.read_more)
        val descriptionTextView: TextView = findViewById(R.id.detail_description)

        loadTaskerProfile()

        val backButton: ImageView = findViewById(R.id.back_arrow)
        backButton.setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
            finish()
        }

        menuButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.profile_menu, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit_account -> {
                        editAccount()
                        true
                    }

                    R.id.set_time_date -> {
                        setTimeAndDateAvailability()
                        true
                    }

                    R.id.delete_account -> {
                        deleteAccount()
                        true
                    }

                    else -> false
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_DAYS_AND_TIME && resultCode == RESULT_OK) {
            val updatedAvailability = data?.getParcelableArrayListExtra<AvailabilitySlot>("SELECTED_AVAILABILITY")
            if (!updatedAvailability.isNullOrEmpty()) {
                Log.d("UpdatedAvailability", updatedAvailability.toString())
                updateAvailabilityToServer(updatedAvailability)
            } else {
                Log.d("UpdatedAvailability", "No new availability selected")
            }
        }
    }

    private fun updateAvailabilityToServer(availabilityList: List<AvailabilitySlot>) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val request = mapOf("availability" to availabilityList)
        val gson = Gson()
        val json = gson.toJson(request)
        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json)

        val call = ApiServiceInstance.Auth.apiServices.updateTaskerAvailability("Bearer $token", body)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MyTaskerProfileActivity, "Availability updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MyTaskerProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MyTaskerProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    companion object {
        const val REQUEST_CODE_SELECT_DAYS_AND_TIME = 1
    }

    private fun editAccount() {
        startActivity(Intent(this, EditMyTaskerProfileActivity::class.java))
    }

    private fun setTimeAndDateAvailability() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.getUserTaskerProfile("Bearer $token")
        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(
                call: Call<TaskerProfileResponse>,
                response: Response<TaskerProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val availabilityList = response.body()?.availability ?: emptyList()

                    val intent = Intent(this@MyTaskerProfileActivity, DaysAndTimeActivity::class.java)
                    intent.putParcelableArrayListExtra("PREVIOUS_AVAILABILITY", ArrayList(availabilityList))
                    startActivityForResult(intent, REQUEST_CODE_SELECT_DAYS_AND_TIME)
                } else {
                    Toast.makeText(this@MyTaskerProfileActivity, "Failed to load availability", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(this@MyTaskerProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteAccount() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null) ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "No auth token found. Please log in again.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.deleteUserTaskerProfile("Bearer $token")
        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(
                call: Call<TaskerProfileResponse>,
                response: Response<TaskerProfileResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MyTaskerProfileActivity,
                        "Profile deleted successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@MyTaskerProfileActivity, MoreActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra(
                        "toast_message",
                        "Profile deleted successfully!"
                    )
                    startActivity(intent)

                    finish()
                } else {
                    Toast.makeText(
                        this@MyTaskerProfileActivity,
                        "Failed to delete profile: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(
                    this@MyTaskerProfileActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun loadTaskerProfile() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val call = ApiServiceInstance.Auth.apiServices.getUserTaskerProfile("Bearer $token")
        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(
                call: Call<TaskerProfileResponse>,
                response: Response<TaskerProfileResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    showTaskerProfile(response.body()!!)
                } else {
                    Toast.makeText(
                        this@MyTaskerProfileActivity,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(
                    this@MyTaskerProfileActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }


    fun showTaskerProfile(profileResponse: TaskerProfileResponse) {
        val detailProfileImage: ImageView = findViewById(R.id.detail_profile_image)
        val detailName: TextView = findViewById(R.id.detail_name)
        val detailRating: TextView = findViewById(R.id.detail_rating)
        val detailReviews: TextView = findViewById(R.id.detail_reviews)
        val detailCategories: TextView = findViewById(R.id.detail_categories)
        val detailCities: TextView = findViewById(R.id.detail_cities)
        val detailHourlyRate: TextView = findViewById(R.id.detail_hourly_rate)
        val detailDescription: TextView = findViewById(R.id.detail_description)
        val readMore: TextView = findViewById(R.id.read_more)

        detailName.text = "${profileResponse.name ?: "Unknown"} ${
            profileResponse.surname?.firstOrNull()?.uppercase() ?: ""
        }.".trim()
        detailRating.text = "Rating: ${profileResponse.rating}"
        detailReviews.text = "(${profileResponse.reviewCount} reviews)"
        detailCategories.text = profileResponse.categories.joinToString(", ") { it.name }
        detailCities.text = profileResponse.cities.joinToString(", ") { it.name }
        detailHourlyRate.text = "Hourly Rate: €${profileResponse.hourly_rate}"

        val shortDescription = profileResponse.description.take(100) + "..."
        detailDescription.text = shortDescription
        readMore.setOnClickListener {
            if (isExpanded) {
                detailDescription.text = shortDescription
                readMore.text = "Read More"
            } else {
                detailDescription.text = profileResponse.description
                readMore.text = "Read Less"
            }
            isExpanded = !isExpanded
        }

        if (profileResponse.profile_photo.isNullOrEmpty()) {
            Glide.with(this)
                .load(R.drawable.placeholder_image_user)
                .circleCrop()
                .into(detailProfileImage)
        } else {
            val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), profileResponse.profile_photo)
            Glide.with(this)
                .load(fullImageUrl.toString())
                .placeholder(R.drawable.placeholder_image_user)
                .error(R.drawable.error)
                .circleCrop()
                .into(detailProfileImage)
        }

        val galleryRecyclerView: RecyclerView = findViewById(R.id.galleryRecyclerView)
        val galleryImages = profileResponse.gallery
        val baseUrl = ApiServiceInstance.BASE_URL

        galleryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        galleryRecyclerView.adapter = GalleryAdapter(galleryImages, { position ->
            showZoomDialog(galleryImages, position, baseUrl)
        }, baseUrl)
    }

    fun showZoomDialog(images: List<String>, startPosition: Int, baseUrl: String) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_zoom_image)

        val photoView = dialog.findViewById<ImageView>(R.id.zoomedImageView)
        val closeButton = dialog.findViewById<ImageView>(R.id.close_button)
        val arrowLeft = dialog.findViewById<ImageView>(R.id.arrow_left)
        val arrowRight = dialog.findViewById<ImageView>(R.id.arrow_right)

        var currentIndex = startPosition
        val imageUrl = URL(URL(baseUrl), images[currentIndex]).toString()
        loadImage(imageUrl, photoView)

        updateArrowsVisibility(currentIndex, images.size, arrowLeft, arrowRight)

        arrowLeft.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                val prevImageUrl = URL(URL(baseUrl), images[currentIndex]).toString()
                loadImage(prevImageUrl, photoView)
                updateArrowsVisibility(currentIndex, images.size, arrowLeft, arrowRight)
            }
        }

        arrowRight.setOnClickListener {
            if (currentIndex < images.size - 1) {
                currentIndex++
                val nextImageUrl = URL(URL(baseUrl), images[currentIndex]).toString()
                loadImage(nextImageUrl, photoView)
                updateArrowsVisibility(currentIndex, images.size, arrowLeft, arrowRight)
            }
        }

        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun loadImage(imageUrl: String, imageView: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error)
            .into(imageView)
    }

    private fun updateArrowsVisibility(currentIndex: Int, totalSize: Int, arrowLeft: ImageView, arrowRight: ImageView) {
        arrowLeft.visibility = if (currentIndex > 0) View.VISIBLE else View.GONE
        arrowRight.visibility = if (currentIndex < totalSize - 1) View.VISIBLE else View.GONE
    }
}