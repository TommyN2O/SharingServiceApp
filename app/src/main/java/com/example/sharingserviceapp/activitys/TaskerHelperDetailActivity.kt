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
import com.example.sharingserviceapp.models.ReviewList
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class TaskerHelperDetailActivity : AppCompatActivity() {
    private lateinit var detailProfileImage: ImageView
    private lateinit var detailName: TextView
    private lateinit var detailRating: TextView
    private lateinit var detailReviews: TextView
    private lateinit var detailCategories: TextView
    private lateinit var detailCities: TextView
    private lateinit var detailHourlyRate: TextView
    private lateinit var detailDescription: TextView
    private lateinit var readMore: TextView
    private lateinit var reviewHeader: TextView
    private lateinit var reviewRecyclerView: RecyclerView
    private var isExpanded = false
    private var userId: Int = -1
    private var categoryId: Int = -1
    private var selectedCityIds: List<String> = emptyList()
    private var selectedCategoryIds: List<String> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasker_helper_detail)
        detailProfileImage = findViewById(R.id.detail_profile_image)
        detailName = findViewById(R.id.detail_name)
        detailRating = findViewById(R.id.detail_rating)
        detailReviews = findViewById(R.id.detail_reviews)
        detailCategories = findViewById(R.id.detail_categories)
        detailCities = findViewById(R.id.detail_cities)
        detailHourlyRate = findViewById(R.id.helper_price)
        detailDescription = findViewById(R.id.detail_description)
        readMore = findViewById(R.id.read_more)
        reviewHeader = findViewById(R.id.reviewHeader)
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)

        userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            Toast.makeText(this, getString(R.string.error_invalid_taskerID), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadTaskerProfile(userId)
        fetchReviews(userId)
        categoryId = intent.getIntExtra("category_id", -1)
        if (categoryId == -1) {
            Toast.makeText(this, getString(R.string.error_invalid_categoryID), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setupListeners()
    }

    private fun setupListeners() {

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, HelperListActivity::class.java).apply {
                putExtra("category_id", intent.getIntExtra("category_id", -1))
            }
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.select_button).setOnClickListener {
            val intent = Intent(this, RequestTaskActivity::class.java).apply{
                putExtra("user_id", userId)
                putStringArrayListExtra("allowed_city_ids", ArrayList(selectedCityIds))
                putStringArrayListExtra("allowed_category_ids", ArrayList(selectedCategoryIds))
                putExtra("category_id", categoryId)
            }
            startActivity(intent)
            finish()
        }
    }

    fun fetchReviews(userId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        val api = ApiServiceInstance.Auth.apiServices
        val taskerId = userId
        val call = api.TaskersReviews("Bearer $token", taskerId )
        call.enqueue(object : Callback<List<ReviewList>> {
            override fun onResponse(call: Call<List<ReviewList>>, response: Response<List<ReviewList>>) {
                if (response.isSuccessful) {
                    val reviews = response.body() ?: emptyList()
                    if (reviews.isEmpty()) {
                        reviewRecyclerView.visibility = View.GONE
                        reviewHeader.visibility = View.GONE
                    } else {
                        val reviewAdapter = ReviewAdapter(reviews)
                        reviewRecyclerView.adapter = reviewAdapter
                        reviewRecyclerView.layoutManager = LinearLayoutManager(this@TaskerHelperDetailActivity)
                        reviewRecyclerView.visibility = View.VISIBLE
                        reviewHeader.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@TaskerHelperDetailActivity, getString(R.string.payments_error_fetch), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<ReviewList>>, t: Throwable) {
                Toast.makeText(this@TaskerHelperDetailActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadTaskerProfile(userId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getTaskerProfileById("Bearer $token", userId)
        call.enqueue(object : Callback<TaskerHelper> {
            override fun onResponse(call: Call<TaskerHelper>, response: Response<TaskerHelper>) {
                if (response.isSuccessful && response.body() != null) {
                    showTaskerProfile(response.body()!!)
                } else {
                    Toast.makeText(this@TaskerHelperDetailActivity, getString(R.string.my_tasker_profile_failed_load_profile), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TaskerHelper>, t: Throwable) {
                Toast.makeText(this@TaskerHelperDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun showTaskerProfile(profileResponse: TaskerHelper) {
        detailName.text = "${profileResponse.name ?: "Nežinoma"} ${profileResponse.surname?.firstOrNull()?.uppercase() ?: ""}.".trim()
        detailRating.text = "Įvertinimas: ${profileResponse.rating}"
        detailReviews.text = "(${getReviewsText(profileResponse.review_count)})"
        detailCategories.text = profileResponse.categories.joinToString(", "){ it.name }
        detailCities.text = profileResponse.cities.joinToString(", "){ it.name }
        detailHourlyRate.text = "${profileResponse.hourly_rate}€/val."
        selectedCityIds = profileResponse.cities.map { it.id.toString() }
        selectedCategoryIds = profileResponse.categories.map { it.id.toString() }

        val shortDescription = profileResponse.description.take(100) + "..."
        detailDescription.text = shortDescription
        readMore.setOnClickListener {
            if (isExpanded) {
                detailDescription.text = shortDescription
                readMore.text = getString(R.string.my_tasker_profile_read_more_btn)
            } else {
                detailDescription.text = profileResponse.description
                readMore.text = getString(R.string.my_tasker_profile_read_less_btn)
            }
            isExpanded = !isExpanded
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
        val galleryTitle: TextView = findViewById(R.id.galleryHeader)
        if (galleryImages.isNullOrEmpty()) {
            galleryRecyclerView.visibility = View.GONE
            galleryTitle.visibility = View.GONE
        } else {
            galleryRecyclerView.visibility = View.VISIBLE
            galleryTitle.visibility = View.VISIBLE

            galleryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            galleryRecyclerView.adapter = GalleryAdapter(galleryImages, { position ->
                showZoomDialog(galleryImages, position, baseUrl)
            }, baseUrl)
        }
    }

    private fun getReviewsText(count: Int): String {
        return when {
            count == 1 -> "$count atsiliepimas"
            (count % 10 in 2..9) && !(count % 100 in 11..19) -> "$count atsiliepimai"
            else -> "$count atsiliepimų"
        }
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