package com.example.sharingserviceapp.adapters

import android.content.Context.MODE_PRIVATE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Message
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private var messages: MutableList<Message>,
    private val onClick: (Message) -> Unit) :

    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.img_profile)
        val txtSenderName: TextView = itemView.findViewById(R.id.txt_sender_name)
        val txtLastMessage: TextView = itemView.findViewById(R.id.txt_last_message)
        val txtTime: TextView = itemView.findViewById(R.id.txt_time)
        val lastSender: TextView = itemView.findViewById(R.id.txt_last_message_sender)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_list_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        val context = holder.itemView.context

        val profileImageUrl = message.otherUser?.profile_photo
        if (!profileImageUrl.isNullOrEmpty()) {
            try {
                val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), profileImageUrl)
                Glide.with(context)
                    .load(fullImageUrl.toString())
                    .placeholder(R.drawable.placeholder_image_user)
                    .error(R.drawable.error)
                    .circleCrop()
                    .into(holder.imgProfile)
            } catch (e: Exception) {
                holder.imgProfile.setImageResource(R.drawable.placeholder_image_user)
            }
        } else {
            holder.imgProfile.setImageResource(R.drawable.placeholder_image_user)
        }

        holder.txtSenderName.text = "${message.otherUser?.name?.replaceFirstChar { it.uppercase() }} ${message.otherUser?.surname?.firstOrNull()?.uppercaseChar() ?: ""}."
        val sharedPreferences = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
        val user = sharedPreferences.getInt("user_id", -1)
        if(user == message.lastMessageUserId) {
            holder.lastSender.visibility= View.VISIBLE
        }
        else{
            holder.lastSender.visibility= View.GONE
        }
        holder.txtLastMessage.text = message.lastMessage
        val formattedTime = formatMessageTime(message.lastMessageTime)
        holder.txtTime.text = formattedTime

        holder.itemView.setOnClickListener { onClick(message) }
    }

    override fun getItemCount(): Int = messages.size

    private fun formatMessageTime(timeString: String?): String {
        if (timeString.isNullOrEmpty()) return ""

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val messageDate: Date = try {
            inputFormat.parse(timeString) ?: return ""
        } catch (e: Exception) {
            return ""
        }

        val now = Calendar.getInstance()
        val messageCal = Calendar.getInstance()
        messageCal.time = messageDate

        val lithuanianLocale = Locale("lt")

        val result = when {
            isSameDay(now, messageCal) -> {
                SimpleDateFormat("HH:mm", lithuanianLocale).format(messageDate)
            }
            isSameWeek(now, messageCal) -> {
                SimpleDateFormat("EEE", lithuanianLocale).format(messageDate)
            }
            isSameYear(now, messageCal) -> {
                SimpleDateFormat("MMM dd", lithuanianLocale).format(messageDate)
            }
            else -> {
                SimpleDateFormat("d MMM yyyy", lithuanianLocale).format(messageDate)
            }
        }

        return result.replaceFirstChar { if (it.isLowerCase()) it.titlecase(lithuanianLocale) else it.toString() }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

}
