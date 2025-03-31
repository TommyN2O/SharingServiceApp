package com.example.sharingserviceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.ChatMessage

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView? = itemView.findViewById(R.id.img_sender_profile)
        val messageText: TextView = itemView.findViewById(R.id.txt_message)!!
        val messageTime: TextView = itemView.findViewById(R.id.txt_time)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == 1) R.layout.item_chat_sent else R.layout.item_chat_received
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        holder.messageText.text = message.text
        holder.messageTime.text = message.timestamp

        // Safe profile image handling
        if (message.isSentByUser) {
            holder.profileImage?.visibility = View.GONE // Hide profile image for sent messages
            holder.messageText.setBackgroundResource(R.drawable.bg_chat_sent)
        } else {
            holder.profileImage?.visibility = View.VISIBLE
            message.senderProfileImageResId?.let {
                holder.profileImage?.setImageResource(it) // Set profile image for received message
            }
            holder.messageText.setBackgroundResource(R.drawable.bg_chat_received)
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSentByUser) 1 else 0
    }
}
