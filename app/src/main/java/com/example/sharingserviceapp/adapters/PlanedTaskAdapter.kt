package com.example.sharingserviceapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.DetailedTasksActivity
import com.example.sharingserviceapp.models.Task1
import java.text.SimpleDateFormat
import java.util.*

class PlanedTaskAdapter(private val groupedTasks: List<Pair<String, List<Task1>>>, private val isTasker: Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_DATE_HEADER = 0
    private val TYPE_TASK = 1
    private val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.date_header_layout, parent, false)
                DateViewHolder(view)
            }
            TYPE_TASK -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
                TaskViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var currentItemIndex = 0

        for (group in groupedTasks) {
            val date = group.first
            val tasks = group.second
            val dateItemCount = tasks.size + 1 // +1 for the date header

            if (position == currentItemIndex) { // Date header position
                if (holder is DateViewHolder) {
                    holder.dateTextView.text = if (date == todayDate) "Today" else formatDate(date)
                }
                return
            }

            val taskIndex = position - currentItemIndex - 1
            if (taskIndex in tasks.indices && holder is TaskViewHolder) { // Task position
                val task = tasks[taskIndex]
                holder.taskNameTextView.text = task.taskName
                holder.taskCategoryTextView.text = task.taskCategory
                holder.taskTimeTextView.text = task.taskTime

                // Set click listener on the whole item
                holder.itemView.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, DetailedTasksActivity::class.java).apply {
                        putExtra("taskName", task.taskName)
                        putExtra("taskCategory", task.taskCategory)
                        putExtra("taskTime", task.taskTime)
                        putExtra("taskDate", task.taskDate)
                    }
                    context.startActivity(intent)
                }
            }

            currentItemIndex += dateItemCount
        }
    }

    override fun getItemCount(): Int {
        return groupedTasks.sumOf { it.second.size + 1 } // Count tasks + headers
    }

    override fun getItemViewType(position: Int): Int {
        var currentItemIndex = 0

        for (group in groupedTasks) {
            val dateItemCount = group.second.size + 1

            if (position == currentItemIndex) {
                return TYPE_DATE_HEADER
            }
            if (position < currentItemIndex + dateItemCount) {
                return TYPE_TASK
            }

            currentItemIndex += dateItemCount
        }
        return TYPE_TASK
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.txt_task_date_header)
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskNameTextView: TextView = itemView.findViewById(R.id.txt_task_name)
        val taskCategoryTextView: TextView = itemView.findViewById(R.id.txt_task_category)
        val taskTimeTextView: TextView = itemView.findViewById(R.id.txt_task_time)
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            date?.let { outputFormat.format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}
