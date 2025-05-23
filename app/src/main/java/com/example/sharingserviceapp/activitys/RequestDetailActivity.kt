package com.example.sharingserviceapp.activitys
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.TaskerHelperDetailActivity
import com.example.sharingserviceapp.adapters.GalleryAdapter
import com.example.sharingserviceapp.models.CreateChat
import com.example.sharingserviceapp.models.CreateChatBody
import com.example.sharingserviceapp.models.Message
import com.example.sharingserviceapp.models.StatusUpdate
import com.example.sharingserviceapp.models.TaskResponse
import com.example.sharingserviceapp.models.TaskerHelper
import com.example.sharingserviceapp.network.ApiServiceInstance
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import kotlin.text.firstOrNull

class RequestDetailActivity : AppCompatActivity() {

    private lateinit var  tasksname: TextView
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
    private lateinit var btnAccept: Button
    private lateinit var btnDecline: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_detail)

        tasksname=findViewById(R.id.titleText)
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
        btnAccept = findViewById(R.id.btnAccept)
        btnDecline = findViewById(R.id.btnDecline)
        btnCancel = findViewById(R.id.btnCancel)

        taskId = intent.getIntExtra("task_id", -1)
        if (taskId == -1) {
            Toast.makeText(this, "Invalid Task ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadRequestDetailed(taskId)

        btnAccept.setOnClickListener {
            updateTaskStatus("Accepted")
        }

        btnCancel.setOnClickListener {
            showConfirmationDialog("Canceled")
        }
        btnDecline.setOnClickListener {
            showConfirmationDialog("Declined")
        }

        setupBackButton()
        setupChatActivity()
    }

    private fun showConfirmationDialog(taskstatus: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmation)

        val confirmationMessage = dialog.findViewById<TextView>(R.id.confirmationMessage)
        if(taskstatus == "Declined" ) {
            confirmationMessage.text = "Are you sure you want to decline this request?"
        }
        else{
            confirmationMessage.text = "Are you sure you want to cancel this request?"
        }

        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        btnYes.setOnClickListener {
            updateTaskStatus(taskstatus)
            val intent = Intent(this, RequestsOffersActivity::class.java)
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

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backArrowButton).setOnClickListener {
            val intent = Intent(this, RequestsOffersActivity::class.java)
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
                            val intent = Intent(this@RequestDetailActivity, ChatActivity::class.java).apply {
                                putExtra("chat_id", chatId)
                                putExtra("receiver_id", receiverId)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@RequestDetailActivity, "Failed to create chat", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CreateChat>, t: Throwable) {
                        Toast.makeText(this@RequestDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(this@RequestDetailActivity, "$status successfully", Toast.LENGTH_SHORT).show()
                        loadRequestDetailed(taskId)

                    } else {
                        Toast.makeText(this@RequestDetailActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@RequestDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }


    private fun loadRequestDetailed(taskId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getPeopleRequestsById("Bearer $token", taskId) // Make sure this function exists in your API interface

        call.enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showRequestDetailed(response.body()!!)
                } else {
                    Toast.makeText(this@RequestDetailActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                    Toast.makeText(this@RequestDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun showRequestDetailed(request: TaskResponse) {
        tasksname.text ="Task #${request.id}"
        customerName.text = "${request.sender.name.replaceFirstChar { it.uppercase() }} ${request.sender.surname.firstOrNull()?.uppercaseChar() ?: ""}."
        taskCategory.text = "Category: ${request.categories.joinToString { it.name }}"
        val slot = request.availability.firstOrNull()
        taskDateTime.text = slot?.let {"Date & Time: ${it.date}, ${it.time.dropLast(3)}"}
        taskDuration.text = "Duration: ${request.duration}h"
        taskLocation.text = "Location: ${request.city.name}"
        taskPrice.text = "Price: $${request.tasker?.hourly_rate}/h"
        taskDescription.text = request.description

        receiverId=request.sender?.id

        val status = request.status.replaceFirstChar { it.uppercase() }
        taskStatus.text = "Status: $status"

        when (status) {
            "Pending" -> taskStatus.setTextColor(resources.getColor(R.color.status_pending))
            "Waiting for Payment" -> taskStatus.setTextColor(resources.getColor(R.color.status_waiting_payment))
            "Declined"->taskStatus.setTextColor(resources.getColor(R.color.status_declined))
            "Canceled"->taskStatus.setTextColor(resources.getColor(R.color.status_declined))
            else -> taskStatus.setTextColor(resources.getColor(R.color.status_default))
        }

        if (status.equals("Waiting for Payment", ignoreCase = true)) {
            btnAccept.visibility = View.GONE
            btnDecline.visibility = View.GONE
            btnCancel.visibility = View.VISIBLE
        } else {
            btnAccept.visibility = View.VISIBLE
            btnDecline.visibility = View.VISIBLE
            btnCancel.visibility = View.GONE
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
}
