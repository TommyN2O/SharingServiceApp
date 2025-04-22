package com.example.sharingserviceapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.HelperListActivity
import com.example.sharingserviceapp.activitys.TaskDetailOfferActivity
import com.example.sharingserviceapp.models.CustumerHelper

class CustomerHelperAdapter(
    private val context: HelperListActivity,  // HelperListActivity context passed correctly
    private var custumerList: MutableList<CustumerHelper>,
    private val onItemClick: (CustumerHelper) -> Unit
) : RecyclerView.Adapter<CustomerHelperAdapter.TaskerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_helper_item_custumer, parent, false)
        return TaskerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskerViewHolder, position: Int) {
        val tasker = custumerList[position]
        holder.bind(tasker)
    }

    override fun getItemCount(): Int = custumerList.size

    // Method to update the list
    fun updateList(newList: MutableList<CustumerHelper>) {
        custumerList = newList
        notifyDataSetChanged()
    }

    // ViewHolder class
    inner class TaskerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        private val galleryRecyclerView: RecyclerView = itemView.findViewById(R.id.gallery)
        private val sendOfferButton: Button = itemView.findViewById(R.id.btn_send_offer)
        fun bind(tasker: CustumerHelper) {
            // Set the profile image
            profileImage.setImageResource(tasker.profileImage)

            // Set up the gallery recycler view with click listener for zooming images
            if (tasker.galleryImages.isNotEmpty()) {
                galleryRecyclerView.visibility = View.VISIBLE
                galleryRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

            } else {
                galleryRecyclerView.visibility = View.GONE
            }

            // Handle tasker item click
            itemView.setOnClickListener { onItemClick(tasker) }

            // Handle send offer button click
            sendOfferButton.setOnClickListener {
                navigateToTaskDetailOfferActivity(tasker)
            }
        }
        private fun navigateToTaskDetailOfferActivity(tasker: CustumerHelper) {
            // Create intent to navigate to TaskDetailOfferActivity and pass tasker details
            val intent = Intent(context, TaskDetailOfferActivity::class.java).apply {
                putExtra("tasker_name", tasker.name)
                putExtra("tasker_budget", tasker.budget)
                putExtra("tasker_city", tasker.city)
            }
            context.startActivity(intent)
        }
    }
}
