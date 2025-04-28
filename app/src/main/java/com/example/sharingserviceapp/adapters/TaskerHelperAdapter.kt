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
import com.example.sharingserviceapp.models.TaskerHelper
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.net.URL

class TaskerHelperAdapter(
    private val context: Context,
    private var taskerList: MutableList<TaskerHelper>,
    private val onItemClick: (TaskerHelper) -> Unit
) : RecyclerView.Adapter<TaskerHelperAdapter.CustomerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_helper_item_tasker, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = taskerList[position]
        holder.bind(customer)
    }

    override fun getItemCount(): Int = taskerList.size

    fun updateList(newList: MutableList<TaskerHelper>) {
        taskerList = newList
        notifyDataSetChanged()
    }

    inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        private val name: TextView = itemView.findViewById(R.id.person_name)
        private val rating: TextView = itemView.findViewById(R.id.rating_number)
        private val reviewCount: TextView = itemView.findViewById(R.id.review_count)
        private val price: TextView = itemView.findViewById(R.id.price_per_hour)
        private val city: TextView = itemView.findViewById(R.id.detail_cities)
        private val shortDescription: TextView = itemView.findViewById(R.id.short_description)

        fun bind(customer: TaskerHelper) {
            val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), customer.profile_photo)
            Glide.with(itemView.context)
                .load(fullImageUrl.toString())
                .placeholder(R.drawable.placeholder_image_user)
                .error(R.drawable.error)
                .circleCrop()
                .into(profileImage)

            name.text = customer.name
            rating.text = customer.rating.toString()
            reviewCount.text = "(${customer.reviewCount} reviews)"
            price.text = "${customer.hourly_rate}$/h"
            city.text = customer.cities.joinToString(", ") { it.name }
            shortDescription.text = customer.description

            itemView.setOnClickListener { onItemClick(customer) }
        }
    }
}
