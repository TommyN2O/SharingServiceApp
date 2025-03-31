package com.example.sharingserviceapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.CustomerHelper

class CustomerHelperAdapter(
    private val context: Context,
    private var customerList: MutableList<CustomerHelper>,
    private val onItemClick: (CustomerHelper) -> Unit
) : RecyclerView.Adapter<CustomerHelperAdapter.CustomerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_helper_item_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customerList[position]
        holder.bind(customer)
    }

    override fun getItemCount(): Int = customerList.size

    fun updateList(newList: MutableList<CustomerHelper>) {
        customerList = newList
        notifyDataSetChanged()
    }

    inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        private val name: TextView = itemView.findViewById(R.id.person_name)
        private val rating: TextView = itemView.findViewById(R.id.rating_number)
        private val reviewCount: TextView = itemView.findViewById(R.id.review_count)
        private val price: TextView = itemView.findViewById(R.id.price_per_hour)
        private val shortDescription: TextView = itemView.findViewById(R.id.short_description)

        fun bind(customer: CustomerHelper) {
            profileImage.setImageResource(customer.profileImage)
            name.text = customer.name
            rating.text = customer.rating.toString()
            reviewCount.text = "(${customer.reviewCount} reviews)"
            price.text = "${customer.price}$/h"
            shortDescription.text = customer.shortDescription

            itemView.setOnClickListener { onItemClick(customer) }
        }
    }
}
