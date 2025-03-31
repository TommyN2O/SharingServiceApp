package com.example.sharingserviceapp.activitys

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.GalleryAdapter
import com.example.sharingserviceapp.adapters.ReviewAdapter
import com.example.sharingserviceapp.models.CustomerHelper
import com.example.sharingserviceapp.models.Review

class HelperDetailActivity : AppCompatActivity() {
    private lateinit var galleryAdapter: GalleryAdapter

    private lateinit var selectedCategories: List<Int>
    private lateinit var selectedCities: List<String>
    private lateinit var selectedTimes: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helper_detail)
        //back btn
        findViewById<ImageView>(R.id.back_arrow).setOnClickListener {
            onBackPressed()
        }
        // Retrieve CustomerHelper object from Intent
        val customerHelper = intent.getParcelableExtra<CustomerHelper>("customerHelper") ?: return

        // Store data without displaying it
        selectedCategories = customerHelper.categories
        selectedCities = customerHelper.availableCities
        selectedTimes = customerHelper.availableTimes

        // Populate UI with helper details (excluding categories, cities, and times)
        findViewById<TextView>(R.id.detail_name).text = customerHelper.name
        findViewById<TextView>(R.id.detail_rating).text = String.format("%.1f", customerHelper.rating)
        findViewById<TextView>(R.id.detail_reviews).text = "${customerHelper.reviewCount} reviews"
        findViewById<TextView>(R.id.detail_description).text = customerHelper.shortDescription
        findViewById<TextView>(R.id.helper_price).text = "Price per hour: \$${customerHelper.price}"
        findViewById<ImageView>(R.id.detail_profile_image).setImageResource(customerHelper.profileImage)

        // Handle gallery images
        val galleryImages = customerHelper.galleryImages ?: listOf()
        val galleryRecyclerView = findViewById<RecyclerView>(R.id.galleryRecyclerView)
        galleryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        galleryAdapter = GalleryAdapter(galleryImages) { position ->
            showZoomDialog(galleryImages, position) // Trigger zoom when image is clicked
        }

        galleryRecyclerView.adapter = galleryAdapter

        // Select Button: Navigates to RequestTaskActivity and passes hidden data
        findViewById<Button>(R.id.select_button).setOnClickListener {
            val intent = Intent(this, RequestTaskActivity::class.java).apply {
                putExtra("customer_name", customerHelper.name)
                putExtra("customer_price", customerHelper.price)
                putExtra("customer_categories", selectedCategories.toIntArray())
                putExtra("customer_cities", selectedCities.toTypedArray())
                putExtra("customer_times", selectedTimes.toTypedArray())
            }
            startActivity(intent)
        }

        // Handle Reviews
        setupReviews()
    }

    private fun setupReviews() {
        val reviewRecyclerView: RecyclerView = findViewById(R.id.reviewRecyclerView)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)

        val reviews = listOf(
            Review("Alice Smith", R.drawable.user, 4.5, "March 2024", "Great service! Highly recommended."),
            Review("John Doe", R.drawable.user, 5.0, "February 2024", "Very professional and fast."),
            Review("Emma Wilson", R.drawable.user, 3.8, "January 2024", "Good, but could improve on timing."),
            Review("Michael Brown", R.drawable.user, 4.0, "December 2023", "Satisfied with the work.")
        )

        val reviewAdapter = ReviewAdapter(reviews)
        reviewRecyclerView.adapter = reviewAdapter
    }

    fun showZoomDialog(images: List<Int>, startPosition: Int) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_zoom_image)

        val imageView = dialog.findViewById<ImageView>(R.id.zoomedImageView)
        val closeButton = dialog.findViewById<ImageView>(R.id.close_button)
        val arrowLeft = dialog.findViewById<ImageView>(R.id.arrow_left)
        val arrowRight = dialog.findViewById<ImageView>(R.id.arrow_right)

        var currentIndex = startPosition
        imageView.setImageResource(images[currentIndex])

        // Update arrow visibility
        updateArrowsVisibility(currentIndex, images.size, arrowLeft, arrowRight)

        // Left Arrow Click
        arrowLeft.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                imageView.setImageResource(images[currentIndex])
                updateArrowsVisibility(currentIndex, images.size, arrowLeft, arrowRight)
            }
        }

        // Right Arrow Click
        arrowRight.setOnClickListener {
            if (currentIndex < images.size - 1) {
                currentIndex++
                imageView.setImageResource(images[currentIndex])
                updateArrowsVisibility(currentIndex, images.size, arrowLeft, arrowRight)
            }
        }

        // Close Button Click
        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun updateArrowsVisibility(currentIndex: Int, totalSize: Int, arrowLeft: ImageView, arrowRight: ImageView) {
        arrowLeft.visibility = if (currentIndex > 0) View.VISIBLE else View.GONE
        arrowRight.visibility = if (currentIndex < totalSize - 1) View.VISIBLE else View.GONE
    }
}
