package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.view.MenuInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.TaskerProfileRequest
import com.example.sharingserviceapp.models.TaskerProfileResponse
import com.example.sharingserviceapp.models.UserProfileResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyTaskerProfileActivity : AppCompatActivity() {

    private var isExpanded = false  // Track if the description is expanded or not

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_tasker_profile)

        // Check if the tasker profile exists from the backend
        checkIfTaskerProfileExists()

        // Setup view references
        val menuButton: ImageView = findViewById(R.id.menu_button)
        val readMoreButton: TextView = findViewById(R.id.read_more)
        val descriptionTextView: TextView = findViewById(R.id.detail_description)

        // Set the dynamic tasker's description (this is a placeholder, replace with real data)
        val taskerDescription = "This is the tasker's short description."
        val fullDescription = "This is the tasker's full description with more details."

        // Set initial short description
        descriptionTextView.text = taskerDescription

        // Set up Back Button
        val backButton: ImageView = findViewById(R.id.back_arrow)
        backButton.setOnClickListener {
            finish() // Go back to the previous screen
        }

        // Set the menu button's click listener to show PopupMenu
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

        // Set up Read More functionality for description
        readMoreButton.setOnClickListener {
            if (isExpanded) {
                descriptionTextView.text = taskerDescription
                readMoreButton.text = "Read More"
            } else {
                descriptionTextView.text = fullDescription
                readMoreButton.text = "Read Less"
            }
            isExpanded = !isExpanded
        }
    }

    // 🔹 Method to check if the tasker profile exists from the backend
    private fun checkIfTaskerProfileExists() {
        // Retrieve the token and userId dynamically from SharedPreferences (or wherever they are stored)
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null) // Retrieve token from SharedPreferences
        val userId = sharedPreferences.getInt("user_id", 0) // Retrieve user_id from SharedPreferences

        // Check if the token and userId exist
        if (token.isNullOrEmpty() || userId == 0) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this@MyTaskerProfileActivity, LoginActivity::class.java))
            finish()
            return
        }

        // Call the API to check if the user is a tasker
        val call = ApiServiceInstance.Auth.apiServices.getUserTaskerProfile("Bearer $token")

        call.enqueue(object : Callback<TaskerProfileResponse> {
            override fun onResponse(
                call: Call<TaskerProfileResponse>,
                response: Response<TaskerProfileResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val userProfile = response.body()!!
                    showTaskerProfile(userProfile)
                    Toast.makeText(this@MyTaskerProfileActivity, "Profile loaded as Tasker!", Toast.LENGTH_SHORT).show()

                } else {
                    // Profile does not exist, navigate to Create Profile Activity
                    Toast.makeText(this@MyTaskerProfileActivity, "No profile found. Create a new profile!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MyTaskerProfileActivity, CreateMyTaskerProfileActivity::class.java))
                    finish() // Close this activity
                }
            }

            override fun onFailure(call: Call<TaskerProfileResponse>, t: Throwable) {
                Toast.makeText(this@MyTaskerProfileActivity, "Error checking profile: ${t.message}", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@MyTaskerProfileActivity, CreateMyTaskerProfileActivity::class.java))
                finish() // Close this activity if the check fails
            }
        })
    }


    // Methods to handle menu item clicks
    private fun editAccount() {
        startActivity(Intent(this, EditMyTaskerProfileActivity::class.java))
    }

    private fun setTimeAndDateAvailability() {
        Toast.makeText(this, "Set Time and Date Availability", Toast.LENGTH_SHORT).show()
    }

    private fun deleteAccount() {
        Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show()
    }

    private fun showTaskerProfile(profileResponse: TaskerProfileResponse) {
        val detailProfileImage: ImageView = findViewById(R.id.detail_profile_image)
        val detailName: TextView = findViewById(R.id.detail_name)
        val detailRating: TextView = findViewById(R.id.detail_rating)
        val detailReviews: TextView = findViewById(R.id.detail_reviews)
        val detailCategories: TextView = findViewById(R.id.detail_categories)
        val detailCities: TextView = findViewById(R.id.detail_cities)
        val detailHourlyRate: TextView = findViewById(R.id.detail_hourly_rate)
        val detailDescription: TextView = findViewById(R.id.detail_description)
        val readMore: TextView = findViewById(R.id.read_more)
//        val galleryRecyclerView: RecyclerView = findViewById(R.id.galleryRecyclerView)
//        val reviewRecyclerView: RecyclerView = findViewById(R.id.reviewRecyclerView)

        // Set name
        detailName.text = "${profileResponse.name ?: "Unknown"} ${profileResponse.surname?.firstOrNull()?.uppercase() ?: ""}.".trim()


        // Set rating
      detailRating.text = "Rating: ${profileResponse.rating}"

        // Set reviews count
        detailReviews.text = "(${profileResponse.reviewCount} reviews)"

        // Set categories
        detailCategories.text = profileResponse.categories.joinToString(", "){ it.name }

        // Set cities
        detailCities.text = profileResponse.cities.joinToString(", "){ it.name }

        // Set hourly rate
        detailHourlyRate.text = "Hourly Rate: $${profileResponse.hourly_rate}"

        // Handle description (Read More functionality)
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

//         Load profile image using Glide
//        Glide.with(this)
//            .load(profileResponse.profile_photo_url)
//            .placeholder(R.drawable.default_profile) // Placeholder image
//            .error(R.drawable.error_image) // Error image
//            .into(detailProfileImage)
//
//        // Set up Gallery RecyclerView
//        galleryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        galleryRecyclerView.adapter = GalleryAdapter(profileResponse.galleryImages)
////
//        // Set up Reviews RecyclerView
//        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
//        reviewRecyclerView.adapter = ReviewAdapter(profileResponse.reviews)


    }
}
