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
    private lateinit var taskNr: TextView
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
        taskNr = findViewById(R.id.titleText)
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
            Toast.makeText(this, getString(R.string.error_invalid_taskId), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        ServerDate()
        loadRequestDetailed(taskId)
        setupChatActivity()
        setupListeners()
    }

    private fun setupListeners(){
        btnCompleted.setOnClickListener {
            updateTaskStatus("Completed")
        }

        btnCanceled.setOnClickListener {
            showDeclineConfirmationDialog()
        }
        findViewById<ImageView>(R.id.backArrowButton).setOnClickListener {
            val intent = Intent(this, PlannedTasksActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showDeclineConfirmationDialog() {
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
                        Toast.makeText(this@PeoplePlannedTasksDetailedActivity, getString(R.string.request_detailed_failed_create_chat), Toast.LENGTH_SHORT).show()
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
                    loadRequestDetailed(taskId)
                    if (statusUpdate.status == "Completed") {
                        val intent = Intent(this@PeoplePlannedTasksDetailedActivity, PlannedTasksActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this@PeoplePlannedTasksDetailedActivity, getString(R.string.request_detailed_failed_update_status), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadRequestDetailed(taskId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getPeopleRequestsById("Bearer $token", taskId)
        call.enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showRequestDetailed(response.body()!!)
                } else {
                    Toast.makeText(this@PeoplePlannedTasksDetailedActivity, getString(R.string.request_offer_my_task_load_failed), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showRequestDetailed(request: TaskResponse) {
        taskNr.text ="Užklausa #${request.id}"
        customerName.text = "${request.sender.name.replaceFirstChar { it.uppercase() }} ${request.sender.surname.firstOrNull()?.uppercaseChar() ?: ""}."
        taskCategory.text = "Paslauga: ${request.categories.joinToString { it.name }}"
        val slot = request.availability.firstOrNull()
        taskDateTime.text = slot?.let {"Data ir laikas: ${it.date}, ${it.time.dropLast(3)}"}
        taskDate = slot?.date
        taskDuration.text = "Trukmė: ${request.duration} val."
        taskLocation.text = "Miestas: ${request.city.name}"

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
        taskDescription.text = request.description

        receiverId=request.tasker?.id

        val rawStatus = request.status.replaceFirstChar { it.uppercase() }
        val status = translateStatus(rawStatus)
        taskStatus.text = status

        when (rawStatus) {
            "Canceled"->taskStatus.setTextColor(resources.getColor(R.color.status_declined))
            else -> taskStatus.setTextColor(resources.getColor(R.color.status_default))
        }

        checkLocalDate()
        if (rawStatus.equals("Canceled", ignoreCase = true)) {
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
        val galleryTitle: TextView = findViewById(R.id.galleryTitle)
        if (galleryImages.isNullOrEmpty()) {
            galleryRecyclerView.visibility = View.GONE
            galleryTitle.visibility = View.GONE
        } else {
            galleryRecyclerView.visibility = View.VISIBLE
            galleryTitle.visibility = View.VISIBLE
            galleryRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            galleryRecyclerView.adapter = GalleryAdapter(galleryImages, { position ->
                showZoomDialog(galleryImages, position, baseUrl)
            }, baseUrl)
        }
    }
    private val statusTranslations = mapOf(
        "Canceled" to "Atšaukta",
        "Paid" to "Apmokėta",
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

    fun dpToPx(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun ServerDate() {
        val api = ApiServiceInstance.Auth.apiServices
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
                    Toast.makeText(this@PeoplePlannedTasksDetailedActivity, getString(R.string.people_planned_task_failed_load_server_date), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ServerDate>, t: Throwable) {
                Toast.makeText(this@PeoplePlannedTasksDetailedActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun checkLocalDate() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())
        if (isSameDayLocal(currentDate, taskDate)) {     //check date
            btnCompleted.visibility = View.VISIBLE
            val paramsCanceled = btnCanceled.layoutParams
            paramsCanceled.width = dpToPx(140f)
            btnCanceled.layoutParams = paramsCanceled
        } else {
            btnCompleted.visibility = View.GONE
        }
    }

    private fun isSameDayLocal(currentDate: String, taskDate: String?): Boolean {
        if (taskDate == null) return false
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.parse(currentDate)
            val task = dateFormat.parse(taskDate)

            today != null && task != null && !today.before(task)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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
