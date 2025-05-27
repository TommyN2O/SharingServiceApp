package com.example.sharingserviceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.ChatMessages
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val messages: List<ChatMessages>,
    private val currentUserId: Int
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView? = itemView.findViewById(R.id.img_sender_profile)
        val messageText: TextView = itemView.findViewById(R.id.txt_message)
        val messageTime: TextView = itemView.findViewById(R.id.txt_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == VIEW_TYPE_SENT) R.layout.item_chat_sent else R.layout.item_chat_received
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        val context = holder.itemView.context
        val isSentByUser = message.sender.id == currentUserId

        holder.messageText.text = message.message
        holder.messageTime.text = formatMessageTime(message.createdAt)

        if (isSentByUser) {
            holder.profileImage?.visibility = View.GONE
        } else {
            holder.profileImage?.visibility = View.VISIBLE

            val photoPath = message.sender.profile_photo
            if (!photoPath.isNullOrBlank()) {
                    val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), photoPath)
                    Glide.with(context)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.placeholder_image_user)
                        .error(R.drawable.error)
                        .circleCrop()
                        .into(holder.profileImage!!)
            } else {
                holder.profileImage?.setImageResource(R.drawable.placeholder_image_user)
            }
        }

    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 0
    }

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
