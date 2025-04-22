package com.example.sharingserviceapp.activitys

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.GalleryAdapter
import com.example.sharingserviceapp.models.DetailedTasks
import com.google.android.material.bottomsheet.BottomSheetDialog

class DetailedTasksActivity : AppCompatActivity() {

    // Example task data with image resource IDs
    private val exampleTask = DetailedTasks(
        customerName = "John Doe",
        taskDescription = "Fix the leaky faucet in the kitchen.",
        taskLocation = "123 Elm St, Springfield",
        taskDate = "March 30, 2025",
        taskTime = "2:00 PM",
        taskImages = listOf(R.drawable.clean_category, R.drawable.clean_category), // Replace with actual drawable images
        taskerName = "Alice Smith",
        customerProfileImage = R.drawable.user,  // Placeholder image
        taskerProfileImage = R.drawable.user     // Placeholder image
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_tasks)

        // Initialize UI elements
        val backButton = findViewById<ImageButton>(R.id.backArrowButton)
        val messageButton = findViewById<ImageView>(R.id.messageButton)
        val completeTaskButton = findViewById<Button>(R.id.btnCompleteTask)
        val supportButton = findViewById<Button>(R.id.btnSupport)

        // Customer Info UI elements
        val customerName = findViewById<TextView>(R.id.customerName)
        val customerProfileImage = findViewById<ImageView>(R.id.customerProfileImage)

        // Task Details UI elements
        val taskDescription = findViewById<TextView>(R.id.taskDescription)
        val taskLocation = findViewById<TextView>(R.id.taskLocation)
        val taskDate = findViewById<TextView>(R.id.taskDate)
        val taskTime = findViewById<TextView>(R.id.taskTime)

        // Tasker Info UI elements
        val taskerName = findViewById<TextView>(R.id.taskerName)
        val taskerProfileImage = findViewById<ImageView>(R.id.taskerProfileImage)

        // RecyclerView for images
        val galleryRecyclerView = findViewById<RecyclerView>(R.id.galleryRecyclerView)

        // Set Example Data to UI
        customerName.text = exampleTask.customerName
        customerProfileImage.setImageResource(exampleTask.customerProfileImage)

        taskDescription.text = exampleTask.taskDescription
        taskLocation.text = "Location: ${exampleTask.taskLocation}"
        taskDate.text = "Date: ${exampleTask.taskDate}"
        taskTime.text = "Time: ${exampleTask.taskTime}"

        taskerName.text = exampleTask.taskerName
        taskerProfileImage.setImageResource(exampleTask.taskerProfileImage)

        // Set up RecyclerView with adapter for task images
       // val galleryAdapter = GalleryAdapter(
            //exampleTask.taskImages
//        ) { position ->
//            showZoomDialog(exampleTask.taskImages, position) // Handle image click to show zoom dialog
//        }

//        galleryRecyclerView.adapter = galleryAdapter

        // Back Button Click - Navigate Back
        backButton.setOnClickListener {
            finish() // Closes this activity and goes back
        }

        // Message Button Click - Open Chat (Replace with actual messaging activity)
        messageButton.setOnClickListener {
            // Open the chat screen
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // Complete Task Button Click - Handle Task Completion
        completeTaskButton.setOnClickListener {
            // TODO: Implement logic to mark task as completed (e.g., update in database)
        }

        // Support Button Click - Open Support Page
        supportButton.setOnClickListener {
            // Open the support screen
            val intent = Intent(this, SupportActivity::class.java)
            startActivity(intent)
        }
    }

    // Zoom Dialog to view image in full-screen with left/right navigation
    private fun showZoomDialog(images: List<Int>, position: Int) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = layoutInflater.inflate(R.layout.dialog_zoom_image, null)
        dialog.setContentView(view)

        val imageView: ImageView = view.findViewById(R.id.zoomedImageView)
        val arrowLeft: ImageView = view.findViewById(R.id.arrow_left)
        val arrowRight: ImageView = view.findViewById(R.id.arrow_right)
        val closeButton: ImageView = view.findViewById(R.id.close_button)

        var currentPosition = position
        val totalImages = images.size

        // Set the initial image
        imageView.setImageResource(images[currentPosition])

        // Update arrow visibility
        updateArrowsVisibility(currentPosition, totalImages, arrowLeft, arrowRight)

        // Left Arrow Click
        arrowLeft.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                imageView.setImageResource(images[currentPosition])
                updateArrowsVisibility(currentPosition, totalImages, arrowLeft, arrowRight)
            }
        }

        // Right Arrow Click
        arrowRight.setOnClickListener {
            if (currentPosition < totalImages - 1) {
                currentPosition++
                imageView.setImageResource(images[currentPosition])
                updateArrowsVisibility(currentPosition, totalImages, arrowLeft, arrowRight)
            }
        }

        // Close Button Click
        closeButton.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog to close it
        }

        dialog.show()
    }

    // Update visibility of the navigation arrows (left & right)
    private fun updateArrowsVisibility(
        currentIndex: Int,
        totalSize: Int,
        arrowLeft: ImageView,
        arrowRight: ImageView
    ) {
        arrowLeft.visibility = if (currentIndex > 0) android.view.View.VISIBLE else android.view.View.GONE
        arrowRight.visibility = if (currentIndex < totalSize - 1) android.view.View.VISIBLE else android.view.View.GONE
    }
}
