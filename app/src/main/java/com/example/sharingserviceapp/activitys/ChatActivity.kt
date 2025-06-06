package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.ChatAdapter
import com.example.sharingserviceapp.models.ChatMessages
import com.example.sharingserviceapp.models.SendMessage
import com.example.sharingserviceapp.network.ApiServiceInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerChat: RecyclerView
    private lateinit var editMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessages>()
    private var chatId: Int = -1
    private var receiverId: Int = -1
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerChat = findViewById(R.id.recycler_chat)
        editMessage = findViewById(R.id.edit_message)
        btnSend = findViewById(R.id.btn_send)

        recyclerChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("user_id", -1)
        if (currentUserId == -1) {
            Toast.makeText(this, getString(R.string.error_invalid_userID), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        chatAdapter = ChatAdapter(messages,currentUserId)
        recyclerChat.adapter = chatAdapter

        chatId = intent.getIntExtra("chat_id", -1)
        if (chatId == -1) {
            Toast.makeText(this, getString(R.string.error_invalid_chatID), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        receiverId = intent.getIntExtra("receiver_id", -1)
        if (receiverId == -1) {
            Toast.makeText(this, getString(R.string.error_invalid_receiverID), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchMessages(chatId)
        setupListeners()
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, MessagesActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnSend.setOnClickListener {
            val text = editMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                editMessage.text.clear()
                sendMessage(text)
            }
        }
    }

    private fun fetchMessages(chatId: Int) {
        val token = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        ApiServiceInstance.Auth.apiServices.getConversationsById("Bearer $token", chatId)
            .enqueue(object : Callback<List<ChatMessages>> {
                override fun onResponse(call: Call<List<ChatMessages>>, response: Response<List<ChatMessages>>) {
                    if (response.isSuccessful) {
                        val sortedMessages = (response.body() ?: emptyList()).sortedBy { it.createdAt }
                        messages.clear()
                        messages.addAll(sortedMessages)
                        chatAdapter.notifyDataSetChanged()
                        recyclerChat.scrollToPosition(messages.size - 1)

                        val other = sortedMessages.firstOrNull()?.let { message ->
                            if (message.sender.id == currentUserId) {
                                message.receiver  // If current user is sender, other user is receiver
                            } else {
                                message.sender    // If current user is receiver, other user is sender
                            }
                        }

                        val userNameView = findViewById<TextView>(R.id.chat_user_name)
                        val userImageView = findViewById<ImageView>(R.id.chat_profile_image)
                        if(other == null)
                        {
                            userNameView.visibility=View.GONE
                            userImageView.visibility=View.GONE
                        }
                        else{
                            userNameView.visibility=View.VISIBLE
                            userImageView.visibility=View.VISIBLE
                        }
                        userNameView.text ="${other?.name?.replaceFirstChar { it.uppercase() }} ${other?.surname?.firstOrNull()?.uppercaseChar() ?: ""}."
                        if (!other?.profile_photo.isNullOrBlank()) {

                                val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), other?.profile_photo)
                                Glide.with(this@ChatActivity)
                                    .load(fullImageUrl)
                                    .placeholder(R.drawable.placeholder_image_user)
                                    .error(R.drawable.error)
                                    .circleCrop()
                                    .into(userImageView)
                        } else {
                            userImageView.setImageResource(R.drawable.placeholder_image_user)
                        }
                    } else {
                        Toast.makeText(this@ChatActivity, getString(R.string.chat_failed_load_messages), Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<ChatMessages>>, t: Throwable) {
                    Toast.makeText(this@ChatActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun sendMessage(messageContent: String) {
        val token = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val request = SendMessage(
            chatId = chatId,
            receiverId = receiverId,
            message = messageContent
        )
        ApiServiceInstance.Auth.apiServices.sendMessage("Bearer $token", request)
            .enqueue(object : Callback<SendMessage> {
                override fun onResponse(call: Call<SendMessage>, response: Response<SendMessage>) {
                    if (response.isSuccessful) {
                        fetchMessages(chatId)
                    }
                    else {
                        Toast.makeText(this@ChatActivity, getString(R.string.chat_failed_send_message), Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<SendMessage>, t: Throwable) {
                    Toast.makeText(this@ChatActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

