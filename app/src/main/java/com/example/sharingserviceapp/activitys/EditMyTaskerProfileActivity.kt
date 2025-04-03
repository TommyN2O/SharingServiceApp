package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import com.example.sharingserviceapp.R

class EditMyTaskerProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_my_tasker_profile)

        val backButton: ImageView = findViewById(R.id.btn_back)  // Make sure this ID is correct in the XML
        backButton.setOnClickListener {
            finish() // This will close the current activity and go back to the previous one
        }
    }
}
