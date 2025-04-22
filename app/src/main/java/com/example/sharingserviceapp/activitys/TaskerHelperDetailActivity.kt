package com.example.sharingserviceapp.activitys

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.MyTaskerProfileActivity
import com.example.sharingserviceapp.models.TaskerHelper
import com.example.sharingserviceapp.adapters.GalleryAdapter
import com.example.sharingserviceapp.adapters.ReviewAdapter
import com.example.sharingserviceapp.models.Review
import com.example.sharingserviceapp.models.TaskerProfileResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class TaskerHelperDetailActivity : AppCompatActivity() {
    private lateinit var galleryAdapter: GalleryAdapter
    private var isExpanded = false
    private var userId: Int = -1
    private var categoryId: Int = -1
    private var selectedCityIds: List<String> = emptyList()
    private var selectedCategoryIds: List<String> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasker_helper_detail)

        // Retrieve TaskerHelper object from Intent
        userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid Tasker ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadTaskerProfile(userId)
        categoryId = intent.getIntExtra("category_id", -1)
        if (categoryId == -1) {
            Toast.makeText(this, "Invalid category ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // Select Button
       setupSelectButton()

        //setupReviews()
        setupBackButton()

        loadTaskerProfile(userId)
    }
    private fun setupBackButton() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            navigateBackActivity()
        }
    }

    private fun navigateBackActivity() {
        val intent = Intent(this, HelperListActivity::class.java).apply {
            putExtra("category_id", intent.getIntExtra("category_id", -1))
        }
        startActivity(intent)
        finish()
    }

    private fun setupSelectButton() {
        findViewById<Button>(R.id.select_button).setOnClickListener {
            navigateToRequestTaskActivity()
        }
    }

    private fun navigateToRequestTaskActivity() {
        val intent = Intent(this, RequestTaskActivity::class.java).apply{
            putExtra("user_id", userId)
            putStringArrayListExtra("allowed_city_ids", ArrayList(selectedCityIds))        // List<String>
            putStringArrayListExtra("allowed_category_ids", ArrayList(selectedCategoryIds)) // List<String>
            putExtra("category_id", categoryId)
        }
        startActivity(intent)
        finish()
    }
//

    private fun loadTaskerProfile(userId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getTaskerProfileById("Bearer $token", userId) // Make sure this function exists in your API interface

        call.enqueue(object : Callback<TaskerHelper> {
            override fun onResponse(call: Call<TaskerHelper>, response: Response<TaskerHelper>) {
                if (response.isSuccessful && response.body() != null) {
                    showTaskerProfile(response.body()!!)
                } else {
                    Toast.makeText(this@TaskerHelperDetailActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskerHelper>, t: Throwable) {
                Toast.makeText(this@TaskerHelperDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun showTaskerProfile(profileResponse: TaskerHelper) {
        val detailProfileImage: ImageView = findViewById(R.id.detail_profile_image)
        val detailName: TextView = findViewById(R.id.detail_name)
        val detailRating: TextView = findViewById(R.id.detail_rating)
        val detailReviews: TextView = findViewById(R.id.detail_reviews)
        val detailCategories: TextView = findViewById(R.id.detail_categories)
        val detailCities: TextView = findViewById(R.id.detail_cities)
        val detailHourlyRate: TextView = findViewById(R.id.helper_price)
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
        detailHourlyRate.text = "$${profileResponse.hourly_rate}/h"

        // Set cities and categories for passing later
        selectedCityIds = profileResponse.cities.map { it.id.toString() }
        selectedCategoryIds = profileResponse.categories.map { it.id.toString() }

        // Handle description (Read More functionality)
        val description = profileResponse.description

        if (description.length > 200) {
            val shortDescription = description.take(200) + "..."
            detailDescription.text = shortDescription
            readMore.visibility = View.VISIBLE

            readMore.setOnClickListener {
                if (isExpanded) {
                    detailDescription.text = shortDescription
                    readMore.text = "Read More"
                } else {
                    detailDescription.text = description
                    readMore.text = "Read Less"
                }
                isExpanded = !isExpanded
            }
        } else {
            detailDescription.text = description
            readMore.visibility = View.GONE
        }
        val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), profileResponse.profile_photo)
        Glide.with(this)
            .load(fullImageUrl)
            .placeholder(R.drawable.placeholder_image_user)
            .error(R.drawable.error)
            .circleCrop()
            .into(detailProfileImage)

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

// Handle gallery
//        val galleryRecyclerView = findViewById<RecyclerView>(R.id.galleryRecyclerView)
//        galleryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        galleryAdapter = GalleryAdapter(tasker.galleryImages) { position ->
//            showZoomDialog(tasker.galleryImages, position)
//        }


//        galleryRecyclerView.adapter = galleryAdapter
//    private fun setupReviews() {
//        val reviewRecyclerView: RecyclerView = findViewById(R.id.reviewRecyclerView)
//        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
//
//        val reviews = listOf(
//            Review("Alice Smith", R.drawable.user, 4.5, "March 2024", "Great service! Highly recommended."),
//            Review("John Doe", R.drawable.user, 5.0, "February 2024", "Very professional and fast."),
//            Review("Emma Wilson", R.drawable.user, 3.8, "January 2024", "Good, but could improve on timing."),
//            Review("Michael Brown", R.drawable.user, 4.0, "December 2023", "Satisfied with the work.")
//        )
//
//        val reviewAdapter = ReviewAdapter(reviews)
//        reviewRecyclerView.adapter = reviewAdapter
//    }
//
//    fun showZoomDialog(images: List<Int>, startPosition: Int) {
//        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
//        dialog.setContentView(R.layout.dialog_zoom_image)
//
//        val imageView = dialog.findViewById<ImageView>(R.id.zoomedImageView)
//        val closeButton = dialog.findViewById<ImageView>(R.id.close_button)
//        val arrowLeft = dialog.findViewById<ImageView>(R.id.arrow_left)
//        val arrowRight = dialog.findViewById<ImageView>(R.id.arrow_right)
//
//        var currentIndex = startPosition
//        imageView.setImageResource(images[currentIndex])
//
//        updateArrowsVisibility(currentIndex, images.size, arrowLeft, arrowRight)
//
//        arrowLeft.setOnClickListener {
//            if (currentIndex > 0) {
//                currentIndex--
//                imageView.setImageResource(images[currentIndex])
//                updateArrowsVisibility(currentIndex, images.size, arrowLeft, arrowRight)
//            }
//        }
//
//        arrowRight.setOnClickListener {
//            if (currentIndex < images.size - 1) {
//                currentIndex++
//                imageView.setImageResource(images[currentIndex])
//                updateArrowsVisibility(currentIndex, images.size, arrowLeft, arrowRight)
//            }
//        }
//
//        closeButton.setOnClickListener { dialog.dismiss() }
//
//        dialog.show()
//    }
//
//    private fun updateArrowsVisibility(currentIndex: Int, totalSize: Int, arrowLeft: ImageView, arrowRight: ImageView) {
//        arrowLeft.visibility = if (currentIndex > 0) View.VISIBLE else View.GONE
//        arrowRight.visibility = if (currentIndex < totalSize - 1) View.VISIBLE else View.GONE
//    }