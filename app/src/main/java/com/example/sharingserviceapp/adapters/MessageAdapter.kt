package com.example.sharingserviceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Message

class MessageAdapter(private val messages: List<Message>, private val onClick: (Message) -> Unit) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.img_profile)
        val txtSenderName: TextView = itemView.findViewById(R.id.txt_sender_name)
        val txtLastMessage: TextView = itemView.findViewById(R.id.txt_last_message)
        val txtTime: TextView = itemView.findViewById(R.id.txt_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_list_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.txtSenderName.text = message.senderName
        holder.txtLastMessage.text = message.lastMessage
        holder.txtTime.text = message.time

        holder.itemView.setOnClickListener { onClick(message) }
    }

    override fun getItemCount(): Int = messages.size
}
