package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
import java.util.Locale

class MessagesActivity : AppCompatActivity() {
    private lateinit var recyclerMessages: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()
    private val fullMessageList = mutableListOf<Message>()
    private val filteredMessageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerMessages = findViewById(R.id.recycler_messages)
        recyclerMessages.layoutManager = LinearLayoutManager(this)

        messageAdapter = MessageAdapter(filteredMessageList) { message ->
            navigateToChat(message)
        }
        recyclerMessages.adapter = messageAdapter

        fetchMessages()
        setupBottomNavigation()
        setupListeners()
    }

    private fun fetchMessages() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_user_auth), Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiServiceInstance.Auth.apiServices.getConversations("Bearer $token")
            .enqueue(object : Callback<List<Message>> {
                override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                    if (response.isSuccessful) {
                        val messages = response.body() ?: emptyList()
                        messageList.clear()
                        messageList.addAll(messages)
                        fullMessageList.clear()
                        fullMessageList.addAll(messages)
                        filteredMessageList.clear()
                        filteredMessageList.addAll(messages)
                        messageAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@MessagesActivity, getString(R.string.failed_load_messages), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                    Toast.makeText(this@MessagesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun filterMessages(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        val filtered = fullMessageList.filter { message ->
            val name = message.otherUser?.name ?: ""
            val surname = message.otherUser?.surname ?: ""
            (name + " " + surname).lowercase(Locale.getDefault()).contains(lowerCaseQuery)
        }
        filteredMessageList.clear()
        filteredMessageList.addAll(filtered)
        messageAdapter.notifyDataSetChanged()
    }

    private fun navigateToChat(message: Message) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chat_id", message.chatId)
            putExtra("receiver_id", message.otherUser.id)
        }
        startActivity(intent)
    }

    private fun setupListeners() {
        val searchButton: ImageView = findViewById(R.id.search_button)
        val searchView: SearchView = findViewById(R.id.search_view)
        val title: TextView = findViewById(R.id.messages_title)

        searchButton.setOnClickListener {
            searchButton.visibility = View.GONE
            title.visibility=View.GONE
            searchView.visibility = View.VISIBLE
            searchView.requestFocus()
        }

        searchView.setOnCloseListener {
            searchView.visibility = View.GONE
            title.visibility=View.VISIBLE
            searchButton.visibility = View.VISIBLE
            false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMessages(newText ?: "")
                return true
            }
        })
    }

    private fun setupBottomNavigation() {
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
}
