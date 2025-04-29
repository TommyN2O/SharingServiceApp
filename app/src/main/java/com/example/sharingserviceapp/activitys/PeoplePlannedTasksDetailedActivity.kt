package com.example.sharingserviceapp.activitys

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
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

class PeoplePlannedTasksDetailedActivity : AppCompatActivity() {
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
    private var receiverId: Int ?= -1
    private lateinit var btnCompleted: Button
    private lateinit var btnCanceled: Button
    private var taskDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_planned_tasks_detailed)

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
        btnCompleted = findViewById(R.id.btnComplete)
        btnCanceled = findViewById(R.id.btnCancel)

        taskId = intent.getIntExtra("task_id", -1)
        if (taskId == -1) {
            Toast.makeText(this, "Invalid Task ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadRequestDetailed(taskId)
        btnCompleted.setOnClickListener {
            updateTaskStatus("Completed")
        }

        btnCanceled.setOnClickListener {
            showDeclineConfirmationDialog()
        }
        setupBackButton()
        setupChatActivity()
    }

    private fun showDeclineConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmation)

        val confirmationMessage = dialog.findViewById<TextView>(R.id.confirmationMessage)
        confirmationMessage.text = "Are you sure you want to decline this request?"


        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        btnYes.setOnClickListener {
            updateTaskStatus("Canceled")
            dialog.dismiss()
        }

        val btnNo = dialog.findViewById<Button>(R.id.btnNo)
        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backArrowButton).setOnClickListener {
            val intent = Intent(this, PlannedTasksActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupChatActivity() {
        findViewById<ImageView>(R.id.messageButton).setOnClickListener{

            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val token = sharedPreferences.getString("auth_token", null)

            val api = ApiServiceInstance.Auth.apiServices
            val body = CreateChatBody(receiverId)
            val call = api.createChat("Bearer $token", body)

            call.enqueue(object : Callback<CreateChat> {
                override fun onResponse(call: Call<CreateChat>, response: Response<CreateChat>) {
                    if (response.isSuccessful) {
                        val chatId = response.body()!!.chatId
                        val intent = Intent(this@PeoplePlannedTasksDetailedActivity, ChatActivity::class.java).apply {
                            putExtra("chat_id", chatId)
                            putExtra("receiver_id", receiverId)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Failed to create chat", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CreateChat>, t: Throwable) {
                    Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun updateTaskStatus(status: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            val api = ApiServiceInstance.Auth.apiServices

            val statusUpdate = StatusUpdate(status)

            val call = api.updateTaskStatus("Bearer $token", taskId, statusUpdate)

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "$status successfully", Toast.LENGTH_SHORT).show()
                        loadRequestDetailed(taskId)

                        if (statusUpdate.status == "Completed") {
                            val intent = Intent(this@PeoplePlannedTasksDetailedActivity, PlannedTasksActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
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
                    Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
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

        receiverId=request.tasker?.id

        val status = request.status.replaceFirstChar { it.uppercase() }
        taskStatus.text = "Status: $status"

        when (status) {
            "Pending" -> taskStatus.setTextColor(resources.getColor(R.color.status_pending))
            "Waiting for Payment" -> taskStatus.setTextColor(resources.getColor(R.color.status_waiting_payment))
            "Declined"->taskStatus.setTextColor(resources.getColor(R.color.status_declined))
            "Canceled"->taskStatus.setTextColor(resources.getColor(R.color.status_declined))
            else -> taskStatus.setTextColor(resources.getColor(R.color.status_default))
        }
        ServerDate()
        if (status.equals("Canceled", ignoreCase = true)) {
            btnCompleted.visibility = View.GONE
            btnCanceled.visibility = View.GONE
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
    fun dpToPx(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
    private fun ServerDate()
    { val api = ApiServiceInstance.Auth.apiServices
        val call = api.serverDate()

        call.enqueue(object : Callback<ServerDate> {
            override fun onResponse(call: Call<ServerDate>, response: Response<ServerDate>) {
                if (response.isSuccessful && response.body() != null) {
                    val serverDate = response.body()?.date
                    if (serverDate != null) {

                        if (isSameDay(serverDate, taskDate)) {
                            btnCompleted.visibility = View.VISIBLE
                            val paramsDecline = btnCanceled.layoutParams
                            paramsDecline.width = dpToPx(140f)
                            btnCanceled.layoutParams = paramsDecline

                        } else {
                            btnCompleted.visibility = View.GONE
                        }
                        }
                } else {
                    Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Failed to load server Date", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ServerDate>, t: Throwable) {
                Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun isSameDay(serverDateTime: String, taskDate: String?): Boolean {
        if (taskDate == null) return false
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val serverDate = isoFormat.parse(serverDateTime)
            val formattedServerDate = simpleDateFormat.format(serverDate!!)

            val task = simpleDateFormat.parse(taskDate)

            formattedServerDate >= simpleDateFormat.format(task!!)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
