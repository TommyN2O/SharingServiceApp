package com.example.sharingserviceapp.activitys

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.PeoplePlannedTasksDetailedActivity
import com.example.sharingserviceapp.adapters.GalleryAdapter
import com.example.sharingserviceapp.models.CreateChat
import com.example.sharingserviceapp.models.CreateChatBody
import com.example.sharingserviceapp.models.ServerDate
import com.example.sharingserviceapp.models.StatusUpdate
import com.example.sharingserviceapp.models.TaskResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PeopleRequestDetailedHistoryActivity : AppCompatActivity() {

    private lateinit var customerName: TextView
    private lateinit var taskCategory: TextView
    private lateinit var taskDateTime: TextView
    private lateinit var taskDuration: TextView
    private lateinit var taskLocation: TextView
    private lateinit var taskStatus: TextView
    private lateinit var taskPrice: TextView
    private lateinit var taskDescription: TextView
    private lateinit var profileImage: ImageView
    private lateinit var galleryRecyclerView: RecyclerView
    private var taskId: Int = -1
    private var taskDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_request_detailed_history)

        customerName = findViewById(R.id.customerName)
        taskCategory = findViewById(R.id.taskCategory)
        taskDateTime = findViewById(R.id.taskDateTime)
        taskDuration = findViewById(R.id.taskDuration)
        taskLocation = findViewById(R.id.taskLocation)
        taskStatus = findViewById(R.id.taskStatus)
        taskPrice = findViewById(R.id.taskPrice)
        taskDescription = findViewById(R.id.taskDescription)
        profileImage = findViewById(R.id.customerProfileImage)
        galleryRecyclerView = findViewById(R.id.galleryRecyclerView)

        taskId = intent.getIntExtra("task_id", -1)
        if (taskId == -1) {
            Toast.makeText(this, "Invalid Task ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadRequestDetailed(taskId)
        setupBackButton()
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backArrowButton).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadRequestDetailed(taskId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getPeopleRequestsById("Bearer $token", taskId)

        call.enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showRequestDetailed(response.body()!!)
                } else {
                    Toast.makeText(this@PeopleRequestDetailedHistoryActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Toast.makeText(this@PeopleRequestDetailedHistoryActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun showRequestDetailed(request: TaskResponse) {

        customerName.text = "${request.sender.name.replaceFirstChar { it.uppercase() }} ${request.sender.surname.firstOrNull()?.uppercaseChar() ?: ""}."
        taskCategory.text = "Category: ${request.categories.joinToString { it.name }}"
        val slot = request.availability.firstOrNull()
        taskDateTime.text = slot?.let {"Date & Time: ${it.date}, ${it.time.dropLast(3)}"}
        taskDate = slot?.date
        taskDuration.text = "Duration: ${request.duration}"
        taskLocation.text = "Location: ${request.city.name}"
        taskPrice.text = "Price: $${request.tasker?.hourly_rate}/h"
        taskDescription.text = request.description
        val status = request.status.replaceFirstChar { it.uppercase() }
        taskStatus.text = "Status: $status"
        when (status) {
            "Pending" -> taskStatus.setTextColor(resources.getColor(R.color.status_pending))
            "Waiting for Payment" -> taskStatus.setTextColor(resources.getColor(R.color.status_waiting_payment))
            "Declined"->taskStatus.setTextColor(resources.getColor(R.color.status_declined))
            "Canceled"->taskStatus.setTextColor(resources.getColor(R.color.status_declined))
            else -> taskStatus.setTextColor(resources.getColor(R.color.status_default))
        }
        val imageUrl = request.sender.profile_photo?.let {
            URL(URL(ApiServiceInstance.BASE_URL), it)
        } ?: R.drawable.placeholder_image_user

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image_user)
            .error(R.drawable.error)
            .circleCrop()
            .into(profileImage)


        val galleryRecyclerView: RecyclerView = findViewById(R.id.galleryRecyclerView)
        val galleryImages = request.gallery
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