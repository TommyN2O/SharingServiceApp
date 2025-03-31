package com.example.sharingserviceapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Helper

class HelperAdapter(
    private val context: Context,
    private var helperList: List<Helper>, // Single list for helpers
    private val itemClickListener: (Helper) -> Unit,
    private val isTaskerMode: Boolean // Determines which layout to use
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_CUSTOMER = 1
        private const val TYPE_TASKER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (isTaskerMode) TYPE_TASKER else TYPE_CUSTOMER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TASKER) {
            val view = LayoutInflater.from(context).inflate(R.layout.list_helper_item_tasker, parent, false)
            TaskerViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.list_helper_item_customer, parent, false)
            CustomerViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val helper = helperList[position]

        if (holder is TaskerViewHolder) {
            holder.bind(helper)
        } else if (holder is CustomerViewHolder) {
            holder.bind(helper)
        }
    }

    override fun getItemCount(): Int = helperList.size

    // This method updates the list and refreshes the RecyclerView
    fun updateList(newList: List<Helper>) {
        this.helperList = newList
        notifyDataSetChanged() // Refreshes the RecyclerView
    }

    // ViewHolder for Customers
    inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.person_name)
        private val rating: TextView = itemView.findViewById(R.id.rating_number)
        private val reviewCount: TextView = itemView.findViewById(R.id.review_count)
        private val description: TextView = itemView.findViewById(R.id.short_description)
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)

        fun bind(helper: Helper) {
            name.text = helper.name
            rating.text = helper.rating.toString()
            reviewCount.text = "(${helper.reviews} reviews)"
            description.text = helper.shortDescription
            profileImage.setImageResource(helper.profileImageResId)

            itemView.setOnClickListener { itemClickListener(helper) }
        }
    }

    // ViewHolder for Taskers
    inner class TaskerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.person_name)
        private val city: TextView = itemView.findViewById(R.id.city)
        private val description: TextView = itemView.findViewById(R.id.short_description)
        private val budget: TextView = itemView.findViewById(R.id.budget)
        private val category: TextView = itemView.findViewById(R.id.category)
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)

        fun bind(helper: Helper) {
            name.text = helper.name
            city.text = helper.city
            description.text = helper.shortDescription
            budget.text = "Budget: $${helper.price}"

            // Convert list of category images to a text format
            category.text = "Categories: ${helper.categories.joinToString(", ")}"

            profileImage.setImageResource(helper.profileImageResId)

            itemView.setOnClickListener { itemClickListener(helper) }
        }
    }
}
