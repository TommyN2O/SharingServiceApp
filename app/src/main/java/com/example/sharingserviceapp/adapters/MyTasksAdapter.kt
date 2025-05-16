package com.example.sharingserviceapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.TaskResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.net.URL

class MyTasksAdapter(
    private val context: Context,
    private val tasksList: List<TaskResponse>,
    private val onItemClick: (TaskResponse) -> Unit
) : RecyclerView.Adapter<MyTasksAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.img_category)
        val txtName: TextView = itemView.findViewById(R.id.txt_user_name)
        val txtCategory: TextView = itemView.findViewById(R.id.txt_category)
        val txtDateTime: TextView = itemView.findViewById(R.id.txt_day_time)
        val txtCity: TextView = itemView.findViewById(R.id.txt_city)
        val txtStatus: TextView = itemView.findViewById(R.id.txt_status)
        val txtTasker: TextView = itemView.findViewById(R.id.txt_tasker)

        fun bind(task: TaskResponse) {
            val imageUrl = task.categories.firstOrNull()?.image
            if (!imageUrl.isNullOrEmpty()) {
                try {
                    val fullUrl = URL(URL(ApiServiceInstance.BASE_URL), imageUrl)
                    Glide.with(itemView.context)
                        .load(fullUrl.toString())
                        .placeholder(R.drawable.placeholder_image_user)
                        .error(R.drawable.error)
                        .circleCrop()
                        .into(imgProfile)
                } catch (e: Exception) {
                    imgProfile.setImageResource(R.drawable.placeholder_image_user)
                }
            } else {
                imgProfile.setImageResource(R.drawable.placeholder_image_user)
            }

            txtName.text = task.categories.joinToString { it.name }

            txtCategory.text = task.categories.joinToString { it.name }

            val slot = task.availability.firstOrNull()
            txtDateTime.text = slot?.let { "${it.date}, ${it.time.dropLast(3)}" } ?: "Not set"

            txtCity.text = task.city.name

            val status = task.status.replaceFirstChar { it.uppercase() }
            txtStatus.text = "Status: $status"

            when (status) {
                "Pending" -> txtStatus.setTextColor(context.resources.getColor(R.color.status_pending))
                "Waiting for Payment" -> txtStatus.setTextColor(context.resources.getColor(R.color.status_waiting_payment))
                "Declined"->txtStatus.setTextColor(context.resources.getColor(R.color.status_declined))
                "Canceled"->txtStatus.setTextColor(context.getColor(R.color.status_declined))
                else -> txtStatus.setTextColor(context.resources.getColor(R.color.status_default)) // Default color
            }

            txtTasker.text = task.tasker?.let {
                "Tasker: ${it.name} ${it.surname?.firstOrNull()?.uppercaseChar() ?: ""}."
            } ?: "Tasker: Not chosen"

            itemView.setOnClickListener { onItemClick(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_my_requests, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasksList[position])
    }

    override fun getItemCount(): Int = tasksList.size
}
