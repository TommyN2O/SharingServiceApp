package com.example.sharingserviceapp.activitys

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
    private var taskId: Int = -1
    private var taskDate: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_request_detailed_history)

        taskId = intent.getIntExtra("task_id", -1)
        if (taskId == -1) {
            Toast.makeText(this, getString(R.string.error_invalid_taskId), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadRequestDetailed()
        setupBackButton()
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backArrowButton).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadRequestDetailed() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getPeopleRequestsById("Bearer $token", taskId)

        call.enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showRequestDetailed(response.body()!!)
                } else {
                    Toast.makeText(this@PeopleRequestDetailedHistoryActivity, getString(R.string.history_detailed_my_tasks_error_load), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Toast.makeText(this@PeopleRequestDetailedHistoryActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showRequestDetailed(request: TaskResponse) {
        val tasknr: TextView = findViewById(R.id.titleText)
        val profileImage: ImageView = findViewById(R.id.customerProfileImage)
        val customerName: TextView = findViewById(R.id.customerName)
        val taskCategory: TextView = findViewById(R.id.taskCategory)
        val taskDateTime: TextView = findViewById(R.id.taskDateTime)
        val taskDuration: TextView = findViewById(R.id.taskDuration)
        val taskLocation: TextView = findViewById(R.id.taskLocation)
        val taskStatus: TextView = findViewById(R.id.taskStatus)
        val taskPrice: TextView = findViewById(R.id.taskPrice)
        val taskDescription: TextView = findViewById(R.id.taskDescription)
        val galleryRecyclerView: RecyclerView = findViewById(R.id.galleryRecyclerView)

        tasknr.text ="Užklausa #${request.id}"
        customerName.text = "${request.sender.name.replaceFirstChar { it.uppercase() }} ${request.sender.surname.firstOrNull()?.uppercaseChar() ?: ""}."
        taskCategory.text = "Paslauga: ${request.categories.joinToString { it.name }}"
        val slot = request.availability.firstOrNull()
        taskDateTime.text = slot?.let {"Date & Time: ${it.date}, ${it.time.dropLast(3)}"}
        taskDate = slot?.date
        taskDuration.text = "Trukmė: ${request.duration} val."
        taskLocation.text = "Miestas: ${request.city.name}"
        taskDescription.text = request.description
        val priceText = "Valandinis: ${request.tasker?.hourly_rate}€/val."
        val spannablePrice = SpannableString(priceText)
        val greenColor = resources.getColor(R.color.my_light_primary)

        val valandinisLength = "Valandinis: ".length
        spannablePrice.setSpan(
            ForegroundColorSpan(greenColor),
            valandinisLength,
            priceText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannablePrice.setSpan(
            StyleSpan(Typeface.BOLD),
            valandinisLength,
            priceText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        taskPrice.text = spannablePrice

        val statusOriginal = request.status.replaceFirstChar { it.uppercase() }
        taskStatus.text = statusTranslations[statusOriginal] ?: statusOriginal
        taskStatus.setTextColor(resources.getColor(R.color.status_default))

        val imageUrl = request.sender.profile_photo?.let {
            URL(URL(ApiServiceInstance.BASE_URL), it)
        } ?: R.drawable.placeholder_image_user

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image_user)
            .error(R.drawable.error)
            .circleCrop()
            .into(profileImage)

        val galleryTitle: TextView = findViewById(R.id.galleryTitle)
        val galleryImages = request.gallery

        if (galleryImages.isNullOrEmpty()) {
            galleryRecyclerView.visibility = View.GONE
            galleryTitle?.visibility = View.GONE
        } else {
            galleryRecyclerView.visibility = View.VISIBLE
            galleryTitle?.visibility = View.VISIBLE

            val baseUrl = ApiServiceInstance.BASE_URL
            galleryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            galleryRecyclerView.adapter = GalleryAdapter(galleryImages, { position ->
                showZoomDialog(galleryImages, position, baseUrl)
            }, baseUrl)
        }
    }

    private val statusTranslations = mapOf(
        "Completed" to "Užbaigtas"
    )

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