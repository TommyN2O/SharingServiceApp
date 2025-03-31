package com.example.sharingserviceapp.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.sharingserviceapp.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnCreateAcc();
        btnLogin()
    }
    private fun gotoLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun btnLogin() {
        val btnReg = findViewById<Button>(R.id.btn_login)
        btnReg.setOnClickListener {
            gotoLogin()
        }
    }


    private fun btnCreateAcc() {
        val btnReg = findViewById<Button>(R.id.btn_con_email)
        btnReg.setOnClickListener {
            gotoRegisterActivity()
        }
    }
    private fun gotoRegisterActivity() {
        val intent = Intent(this@MainActivity, RegisterActivity::class.java)
        startActivity(intent)
    }
}