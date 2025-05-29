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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.GalleryAdapter
import com.example.sharingserviceapp.models.CreateChat
import com.example.sharingserviceapp.models.CreateChatBody
import com.example.sharingserviceapp.models.StatusUpdate
import com.example.sharingserviceapp.models.TaskResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class MyPlannedTaskDetailedActivity : AppCompatActivity() {
    private var taskId: Int = -1
    private var receiverId: Int ?= -1
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_planned_tasks_detailed)

        btnCancel = findViewById(R.id.btnCancel)

        val intent = intent
        val action = intent.action
        val data = intent.data

        taskId = intent.getIntExtra("TASK_ID", -1)

        if (Intent.ACTION_VIEW == action && data != null) {
            if (data.scheme == "sharingapp" && data.host == "payment-success") {
                taskId = data.getQueryParameter("task_id")?.toIntOrNull()!!

                Toast.makeText(this, getString(R.string.my_planned_task_payment_successful), Toast.LENGTH_LONG).show()
            }
        }

        if (taskId == -1) {
            Toast.makeText(this, getString(R.string.error_invalid_taskId), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        btnCancel.setOnClickListener {
            showCancelConfirmationDialog()
        }
        loadTaskDetailed(taskId)
        setupBackButton()
        setupChatActivity()
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
                        val intent = Intent(this@MyPlannedTaskDetailedActivity, ChatActivity::class.java).apply {
                            putExtra("chat_id", chatId)
                            putExtra("receiver_id", receiverId)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@MyPlannedTaskDetailedActivity, getString(R.string.request_offer_my_task_failed_create_chat), Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<CreateChat>, t: Throwable) {
                    Toast.makeText(this@MyPlannedTaskDetailedActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backArrowButton).setOnClickListener {
            val intent = Intent(this, PlannedTasksActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showCancelConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmation)

        val confirmationMessage = dialog.findViewById<TextView>(R.id.confirmationMessage)
        confirmationMessage.text = getString(R.string.planned_task_dialog_cancel_text)

        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        btnYes.setOnClickListener {
            updateTaskStatus("Canceled")
            val intent = Intent(this, PlannedTasksActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)
        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateTaskStatus(status: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val api = ApiServiceInstance.Auth.apiServices
        val statusUpdate = StatusUpdate(status)
        val call = api.updateTaskStatus("Bearer $token", taskId, statusUpdate)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    loadTaskDetailed(taskId)
                } else {
                    Toast.makeText(this@MyPlannedTaskDetailedActivity, getString(R.string.request_offer_my_task_status_update), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MyPlannedTaskDetailedActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadTaskDetailed(taskId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getMyTasksById("Bearer $token", taskId)
        call.enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showRequestDetailed(response.body()!!)
                } else {
                    Toast.makeText(this@MyPlannedTaskDetailedActivity, getString(R.string.request_offer_my_task_load_failed), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Toast.makeText(this@MyPlannedTaskDetailedActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showRequestDetailed(task: TaskResponse) {
        val tasknr: TextView = findViewById(R.id.titleText)
        val taskCategory: TextView = findViewById(R.id.taskCategory)
        val taskDateTime: TextView = findViewById(R.id.taskDateTime)
        val taskDuration: TextView = findViewById(R.id.taskDuration)
        val taskLocation: TextView = findViewById(R.id.taskLocation)
        val taskStatus: TextView = findViewById(R.id.taskStatus)
        val taskPrice: TextView = findViewById(R.id.taskPrice)
        val taskDescription: TextView = findViewById(R.id.taskDescription)
        val galleryRecyclerView: RecyclerView = findViewById(R.id.galleryRecyclerView)
        val taskerProfileImage: ImageView = findViewById(R.id.taskerProfileImage)
        val taskerName: TextView = findViewById(R.id.taskerName)

        tasknr.text ="Užklausa #${task.id}"
        taskCategory.text = "Paslauga: ${task.categories.joinToString { it.name }}"
        val slot = task.availability.firstOrNull()
        taskDateTime.text = slot?.let {"Data ir laikas: ${it.date}, ${it.time.dropLast(3)}"}
        taskDuration.text = "Trukmė: ${task.duration} val."
        taskLocation.text = "Miestas: ${task.city.name}"
        val priceText = "Valandinis: ${task.tasker?.hourly_rate}€/val."
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

        taskDescription.text = task.description
        receiverId=task.tasker?.id

        val rawStatus = task.status.replaceFirstChar { it.uppercase() }
        val status = translateStatus(rawStatus)
        taskStatus.text = status

        when (rawStatus) {
            "Canceled" -> taskStatus.setTextColor(resources.getColor(R.color.status_declined))
            else -> taskStatus.setTextColor(resources.getColor(R.color.status_default))
        }

        val galleryImages = task.gallery
        val baseUrl = ApiServiceInstance.BASE_URL
        val galleryTitle: TextView = findViewById(R.id.galleryTitle)
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

        val taskerImageUrl = task.tasker?.profile_photo?.let {
            URL(URL(ApiServiceInstance.BASE_URL), it)
        } ?: R.drawable.placeholder_image_user

        Glide.with(this)
            .load(taskerImageUrl)
            .placeholder(R.drawable.placeholder_image_user)
            .error(R.drawable.error)
            .circleCrop()
            .into(taskerProfileImage)
        taskerName.text = "${task.tasker?.name?.replaceFirstChar { it.uppercase() }} ${task.tasker?.surname?.firstOrNull()?.uppercaseChar() ?: ""}."
    }
    private val statusTranslations = mapOf(
        "Paid" to "Apmokėta",
        "Canceled" to "Atšauktas",
    )

    private fun translateStatus(status: String): String {
        val key = status.replaceFirstChar { it.uppercase() }
        return statusTranslations[key] ?: status
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