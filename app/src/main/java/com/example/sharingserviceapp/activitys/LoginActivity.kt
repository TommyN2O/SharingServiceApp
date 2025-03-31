package com.example.sharingserviceapp.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.sharingserviceapp.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btnLogin()
    }

    private fun btnLogin() {
        val btnReg = findViewById<Button>(R.id.btn_login)
        btnReg.setOnClickListener {
            gotoHome()
        }
    }
    private fun gotoHome() {
        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
        startActivity(intent)
    }
}