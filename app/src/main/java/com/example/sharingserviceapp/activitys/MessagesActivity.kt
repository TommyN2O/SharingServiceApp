package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.MessageAdapter
import com.example.sharingserviceapp.models.Message
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessagesActivity : AppCompatActivity() {

    private lateinit var recyclerMessages: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerMessages = findViewById(R.id.recycler_messages)
        recyclerMessages.layoutManager = LinearLayoutManager(this)

        messageAdapter = MessageAdapter(messageList) { message ->
            navigateToChat(message)
        }
        recyclerMessages.adapter = messageAdapter

        fetchMessages()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menu_messages
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_tasks -> {
                    startActivity(Intent(this, PlannedTasksActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_messages -> {
                    true
                }
                R.id.menu_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchMessages() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) return

        ApiServiceInstance.Auth.apiServices.getConversations("Bearer $token")
            .enqueue(object : Callback<List<Message>> {
                override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                    if (response.isSuccessful) {
                        val messages = response.body() ?: emptyList()
                        messageList.clear()
                        messageList.addAll(messages)
                        messageAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@MessagesActivity, "Failed to load messages", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                    Toast.makeText(this@MessagesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun navigateToChat(message: Message) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chat_id", message.chatId)
            putExtra("receiver_id", message.otherUser.id)
        }
        startActivity(intent)
    }
}
