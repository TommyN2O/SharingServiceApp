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
import com.example.sharingserviceapp.models.PlannedTaskListItem
import com.example.sharingserviceapp.models.TaskResponse
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.net.URL
import kotlin.text.firstOrNull

class PeoplePlanedTasksAdapter(
    private val context: Context,
    private val items: List<PlannedTaskListItem>,
    private val onItemClick: (TaskResponse) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_DATE_HEADER = 0
    private val VIEW_TYPE_TASK_ITEM = 1


    inner class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtDateHeader: TextView = itemView.findViewById(R.id.txt_date_header)

        fun bind(date: String) {
            txtDateHeader.text = date
        }
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.img_category)
        val txtName: TextView = itemView.findViewById(R.id.txt_user_name)
        val txtCategory: TextView = itemView.findViewById(R.id.txt_category)
        val txtDateTime: TextView = itemView.findViewById(R.id.txt_day_time)
        val txtCity: TextView = itemView.findViewById(R.id.txt_city)

        fun bind(request: TaskResponse) {
            request.sender.profile_photo?.let {
                try {
                    val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), it)
                    Glide.with(context)
                        .load(fullImageUrl.toString())
                        .placeholder(R.drawable.placeholder_image_user)
                        .error(R.drawable.error)
                        .circleCrop()
                        .into(imgProfile)
                } catch (e: Exception) {
                    imgProfile.setImageResource(R.drawable.placeholder_image_user)
                }
            } ?: imgProfile.setImageResource(R.drawable.placeholder_image_user)

            txtName.text = "${request.sender.name.replaceFirstChar { it.uppercase() }} ${request.sender.surname.firstOrNull()?.uppercaseChar() ?: ""}."

            txtCategory.text = "Category: ${request.categories.joinToString { it.name }}"

            val slot = request.availability.firstOrNull()
            txtDateTime.text = slot?.let { it.time.dropLast(3) } ?: "Not set"

            txtCity.text = request.city.name

            itemView.setOnClickListener { onItemClick(request) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is PlannedTaskListItem.DateHeader -> VIEW_TYPE_DATE_HEADER
            is PlannedTaskListItem.TaskItem -> VIEW_TYPE_TASK_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return if (viewType == VIEW_TYPE_DATE_HEADER) {
            val view = inflater.inflate(R.layout.item_date_header, parent, false)
            DateHeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.list_item_people_planed_tasks, parent, false)
            TaskViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is PlannedTaskListItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item.date)
            is PlannedTaskListItem.TaskItem -> (holder as TaskViewHolder).bind(item.task)
        }
    }

    override fun getItemCount(): Int = items.size
}
