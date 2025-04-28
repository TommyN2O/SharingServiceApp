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

class MyPlannedTasksAdapter(
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
        private val imgProfile: ImageView = itemView.findViewById(R.id.img_category)
        private val txtName: TextView = itemView.findViewById(R.id.txt_user_name)
        private val txtDateTime: TextView = itemView.findViewById(R.id.txt_day_time)
        private val txtCity: TextView = itemView.findViewById(R.id.txt_city)
        private val txtTasker: TextView = itemView.findViewById(R.id.txt_tasker)

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

            val slot = task.availability.firstOrNull()
            txtDateTime.text = slot?.let { it.time.dropLast(3) } ?: "Not set"

            txtCity.text = task.city.name

            txtTasker.text = task.tasker?.let {
                "Tasker: ${it.name} ${it.surname?.firstOrNull()?.uppercaseChar() ?: ""}."
            } ?: "Tasker: Not chosen"

            itemView.setOnClickListener { onItemClick(task) }
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
            val view = inflater.inflate(R.layout.list_item_my_planned_tasks, parent, false)
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
