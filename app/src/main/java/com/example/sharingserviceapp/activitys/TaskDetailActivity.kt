package com.example.sharingserviceapp.activitys

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.CreateMyTaskerProfileActivity.Companion.REQUEST_CODE_SELECT_DAYS_AND_TIME
import com.example.sharingserviceapp.activitys.LoginActivity
import com.example.sharingserviceapp.activitys.RequestDetailActivity
import com.example.sharingserviceapp.adapters.GalleryAdapter
import com.example.sharingserviceapp.adapters.OfferListAdapter
import com.example.sharingserviceapp.models.AvailabilitySlot
import com.example.sharingserviceapp.models.CreateChat
import com.example.sharingserviceapp.models.CreateChatBody
import com.example.sharingserviceapp.models.Offer
import com.example.sharingserviceapp.models.OpenTaskResponse
import com.example.sharingserviceapp.models.OpenedTasksHelper
import com.example.sharingserviceapp.models.SetAsOpenTask
import com.example.sharingserviceapp.models.StatusUpdate
import com.example.sharingserviceapp.models.TaskResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class TaskDetailActivity : AppCompatActivity() {

    private var taskId: Int = -1
    private var receiverId: Int ?= -1
    private lateinit var btnPayment: Button
    private lateinit var btnCancel: Button
    private lateinit var btnOffers: ImageView
    private lateinit var btnMessage: ImageView
    private lateinit var btnSetAsOpenTask:Button

    private var availabilityList: List<String> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        btnPayment = findViewById(R.id.btnPayment)
        btnCancel = findViewById(R.id.btnCancel)
        btnOffers = findViewById(R.id.btn_offers)
        btnMessage = findViewById(R.id.messageButton)
        btnSetAsOpenTask = findViewById(R.id.btn_SetAsOpenTask)

        taskId = intent.getIntExtra("TASK_ID", -1)
        if (taskId == -1) {
            Toast.makeText(this, "Invalid Task ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupPaymentButton()

        btnOffers.setOnClickListener {
            showOfferListDialog(taskId)
        }

        btnCancel.setOnClickListener {
            showCancelConfirmationDialog()
        }

        btnSetAsOpenTask.setOnClickListener {
                showSetAsOpTaskDialog(taskId)
        }


        loadTaskDetailed(taskId)
        setupBackButton()
        setupChatActivity()
    }
    private fun showSetAsOpTaskDialog(taskId: Int) {
        val dialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_as_open_task, null)


        dialogView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        dialog.setContentView(dialogView)
        dialog.setCancelable(false)

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.requestLayout()

                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }



        val editTextBudget = dialogView.findViewById<EditText>(R.id.edit_budget)
        val btnSelectDaysTime = dialogView.findViewById<Button>(R.id.btn_select_days_time)
        btnSelectDaysTime.setOnClickListener {
            val availabilitySlotList = availabilityList.map {
                val parts = it.split(" ")
                AvailabilitySlot(parts[0], parts[1])
            }

            val intent = Intent(this, DaysAndTimeActivity::class.java)
            intent.putParcelableArrayListExtra("PREVIOUS_AVAILABILITY", ArrayList(availabilitySlotList))
            startActivityForResult(intent, REQUEST_CODE_SELECT_DAYS_AND_TIME)
        }

        dialogView.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_create_open_task).setOnClickListener {
            val budgetText = editTextBudget.text.toString()
            val budget = budgetText.toDoubleOrNull()

            if (budget == null) {
                Toast.makeText(this, "Please enter a valid budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val availability = availabilityList.map {
                val parts = it.split(" ")
                AvailabilitySlot(parts[0], parts[1])
            }

            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val token = sharedPreferences.getString("auth_token", null)

            if (token.isNullOrEmpty()) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return@setOnClickListener
            }

            val body = SetAsOpenTask(
                budget = budget,
                availability = availability,
            )

            val apiService = ApiServiceInstance.Auth.apiServices
            val call = apiService.convertToOpenTask("Bearer $token", body, taskId)

            call.enqueue(object : Callback<OpenTaskResponse> {
                override fun onResponse(call: Call<OpenTaskResponse>, response: Response<OpenTaskResponse>) {
                    if (response.isSuccessful) {
                        val intent = Intent(this@TaskDetailActivity, RequestsOffersActivity::class.java)
                        startActivity(intent)
                        finish()
                        Toast.makeText(this@TaskDetailActivity, "Chat created successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@TaskDetailActivity, "Failed to create Open Task", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OpenTaskResponse>, t: Throwable) {
                    Toast.makeText(this@TaskDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })

            dialog.dismiss()
        }

        dialog.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_DAYS_AND_TIME && resultCode == RESULT_OK) {
            val selectedAvailability = data?.getParcelableArrayListExtra<AvailabilitySlot>("SELECTED_AVAILABILITY")
            if (selectedAvailability != null) {
                availabilityList = selectedAvailability.map { "${it.date} ${it.time}" }
                Toast.makeText(this, "Availability: ${availabilityList.joinToString("\n")}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showOfferListDialog(taskId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_offer_list, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.offerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val apiService = ApiServiceInstance.Auth.apiServices

        apiService.getOffersForTask("Bearer $token", taskId)
            .enqueue(object : Callback<List<Offer>> {
                override fun onResponse(call: Call<List<Offer>>, response: Response<List<Offer>>) {
                    if (response.isSuccessful) {
                        val offers = response.body() ?: emptyList()
                        val adapter = OfferListAdapter(offers,
                            onAccept = { offer ->
                                acceptOffer(token, offer.id)
                            },
                        )
                        recyclerView.adapter = adapter

                        AlertDialog.Builder(this@TaskDetailActivity)
                            .setTitle("Offers")
                            .setView(dialogView)
                            .setNegativeButton("Close", null)
                            .show()
                    } else {
                        Toast.makeText(this@TaskDetailActivity, "Failed to load offers", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Offer>>, t: Throwable) {
                    Toast.makeText(this@TaskDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun acceptOffer(token: String, offerId: Int) {

        val apiService = ApiServiceInstance.Auth.apiServices

        apiService.acceptOffer("Bearer $token", offerId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@TaskDetailActivity, "Offer accepted", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@TaskDetailActivity, RequestsOffersActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@TaskDetailActivity, "Failed to accept offer", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@TaskDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun setupChatActivity() {
        btnMessage.setOnClickListener{

            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val token = sharedPreferences.getString("auth_token", null)
            val api = ApiServiceInstance.Auth.apiServices
            val body = CreateChatBody(receiverId)
            val call = api.createChat("Bearer $token", body)
            call.enqueue(object : Callback<CreateChat> {
                override fun onResponse(call: Call<CreateChat>, response: Response<CreateChat>) {
                    if (response.isSuccessful) {
                        val chatId = response.body()!!.chatId
                        val intent = Intent(this@TaskDetailActivity, ChatActivity::class.java).apply {
                            putExtra("chat_id", chatId)
                            putExtra("receiver_id", receiverId)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@TaskDetailActivity, "Failed to create chat", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CreateChat>, t: Throwable) {
                    Toast.makeText(this@TaskDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backArrowButton).setOnClickListener {
            val intent = Intent(this, RequestsOffersActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun setupPaymentButton(){
        btnPayment.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java).apply{
                putExtra("TASK_ID",taskId)
            }
            startActivity(intent)
            finish()
        }

    }

    private fun showCancelConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmation)

        val confirmationMessage = dialog.findViewById<TextView>(R.id.confirmationMessage)
        confirmationMessage.text = "Are you sure you want to cancel this request?"

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
                        Toast.makeText(this@TaskDetailActivity, "$status successfully", Toast.LENGTH_SHORT).show()
                        loadTaskDetailed(taskId)

                    } else {
                        Toast.makeText(this@TaskDetailActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@TaskDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }


    private fun loadTaskDetailed(taskId: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)


        val api = ApiServiceInstance.Auth.apiServices
        val call = api.getMyTasksById("Bearer $token", taskId)

        call.enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showRequestDetailed(response.body()!!)
                } else {
                    Toast.makeText(this@TaskDetailActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Toast.makeText(this@TaskDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
    //#TODO FIX UP INFO SHOWN
    private fun showRequestDetailed(task: TaskResponse) {
        val tasknr: TextView = findViewById(R.id.titleText)
        val customerName: TextView = findViewById(R.id.customerName)
        val taskCategory: TextView = findViewById(R.id.taskCategory)
        val taskDateTime: TextView = findViewById(R.id.taskDateTime)
        val taskDuration: TextView = findViewById(R.id.taskDuration)
        val taskLocation: TextView = findViewById(R.id.taskLocation)
        val taskStatus: TextView = findViewById(R.id.taskStatus)
        val taskPrice: TextView = findViewById(R.id.taskPrice)
        val taskDescription: TextView = findViewById(R.id.taskDescription)
//        val profileImage: ImageView = findViewById(R.id.customerProfileImage)
        val galleryRecyclerView: RecyclerView = findViewById(R.id.galleryRecyclerView)
        val taskerProfileImage: ImageView = findViewById(R.id.taskerProfileImage)
        val taskerName: TextView = findViewById(R.id.taskerName)
        val titleTasker: TextView = findViewById(R.id.titleTasker)

        customerName.text = "${task.sender.name.replaceFirstChar { it.uppercase() }} ${task.sender.surname.firstOrNull()?.uppercaseChar() ?: ""}."
        taskCategory.text = "Category: ${task.categories.joinToString { it.name }}"
        taskDuration.text = "Duration: ${task.duration}h"
        taskLocation.text = "Location: ${task.city.name}"
        taskDescription.text = task.description
        receiverId=task.tasker?.id

        val status = task.status.replaceFirstChar { it.uppercase() }
        taskStatus.text = "Status: $status"

        when (status) {
            "Pending" -> taskStatus.setTextColor(resources.getColor(R.color.status_pending))
            "Waiting for Payment" -> taskStatus.setTextColor(resources.getColor(R.color.status_waiting_payment))
            "Declined"->taskStatus.setTextColor(resources.getColor(R.color.status_declined))
            else -> taskStatus.setTextColor(resources.getColor(R.color.status_default))
        }

        // Hide the Accept button if status is "Waiting for Payment"
        if (status.equals("Waiting for Payment", ignoreCase = true)) {
            btnPayment.visibility = View.VISIBLE

            val paramsDecline = btnCancel.layoutParams
            paramsDecline.width = dpToPx(140f) // Set width to 320dp
            btnCancel.layoutParams = paramsDecline

        } else {
            btnPayment.visibility = View.GONE
        }

        if(status=="Declined" || status=="Canceled")
        {
            btnSetAsOpenTask.visibility = View.VISIBLE
            val paramsDecline = btnCancel.layoutParams
            paramsDecline.width = dpToPx(140f) // Set width to 320dp
            btnCancel.layoutParams = paramsDecline
        }
        else{
            btnSetAsOpenTask.visibility = View.GONE
        }

        val galleryImages = task.gallery
        val baseUrl = ApiServiceInstance.BASE_URL

        galleryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        galleryRecyclerView.adapter = GalleryAdapter(galleryImages, { position ->
            showZoomDialog(galleryImages, position, baseUrl)
        }, baseUrl)

        val slot = task.availability.firstOrNull()
        taskDateTime.text = slot?.let {"Date & Time: ${it.date}, ${it.time.dropLast(3)}"}

        if (task.tasker == null || status == "Open") {
            titleTasker.visibility = View.GONE
            taskerProfileImage.visibility = View.GONE
            taskerName.visibility = View.GONE
            btnMessage.visibility = View.GONE
            btnOffers.visibility = View.VISIBLE
            taskPrice.text = "Budget: $${task.budget}"
            taskDateTime.text = slot?.let {"Due Date: ${it.date}"}
            tasknr.text ="Open Task #${task.id}"
        }else {

            val taskerImageUrl = task.tasker?.profile_photo?.let {
            URL(URL(ApiServiceInstance.BASE_URL), it)
            } ?: R.drawable.placeholder_image_user

            Glide.with(this)
                .load(taskerImageUrl)
                .placeholder(R.drawable.placeholder_image_user)
                .error(R.drawable.error)
                .circleCrop()
                .into(taskerProfileImage)

            taskerName.text = "${task.tasker?.name?.replaceFirstChar { it.uppercase() }} ${
                task.tasker?.surname?.firstOrNull()?.uppercaseChar() ?: ""
            }."
            taskerProfileImage.visibility = View.VISIBLE
            taskerName.visibility = View.VISIBLE
            titleTasker.visibility = View.VISIBLE
            btnMessage.visibility = View.VISIBLE
            btnOffers.visibility = View.GONE
            taskPrice.text = "Price: $${task.tasker?.hourly_rate}/h"
            taskDateTime.text = slot?.let {"Date & Time: ${it.date}, ${it.time.dropLast(3)}"}
            tasknr.text ="Task #${task.id}"
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
    fun dpToPx(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

}
