// ChatActivity.kt
package com.example.sharingserviceapp.activitys

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.ChatAdapter
import com.example.sharingserviceapp.models.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerChat: RecyclerView
    private lateinit var editMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerChat = findViewById(R.id.recycler_chat)
        editMessage = findViewById(R.id.edit_message)
        btnSend = findViewById(R.id.btn_send)

        recyclerChat.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(messages)
        recyclerChat.adapter = chatAdapter

        // Example data: Adding some mock messages
        val profileImageResId = R.drawable.user // Replace with your image resource ID
        messages.add(ChatMessage("Hi, how are you?", false, "12:46 PM", profileImageResId))
        messages.add(ChatMessage("I'm good, thanks! How about you?", true, "12:47 PM"))

        chatAdapter.notifyDataSetChanged()

        btnSend.setOnClickListener {
            val text = editMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                messages.add(ChatMessage(text, true, timestamp))
                chatAdapter.notifyItemInserted(messages.size - 1)
                recyclerChat.scrollToPosition(messages.size - 1)
                editMessage.text.clear()
            }
        }
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }
}
