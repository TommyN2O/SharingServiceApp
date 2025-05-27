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

    inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        private val name: TextView = itemView.findViewById(R.id.person_name)
        private val rating: TextView = itemView.findViewById(R.id.rating_number)
        private val reviewCount: TextView = itemView.findViewById(R.id.review_count)
        private val price: TextView = itemView.findViewById(R.id.price_per_hour)
        private val city: TextView = itemView.findViewById(R.id.detail_cities)
        private val shortDescription: TextView = itemView.findViewById(R.id.short_description)

        fun bind(tasker: TaskerHelper) {
            val profilePhotoPath = tasker.profile_photo

            if (!profilePhotoPath.isNullOrEmpty()) {
                try {
                    val imageUrl = URL(URL(ApiServiceInstance.BASE_URL), profilePhotoPath).toString()
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image_user)
                        .error(R.drawable.error)
                        .circleCrop()
                        .into(profileImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Glide.with(itemView.context)
                        .load(R.drawable.placeholder_image_user)
                        .circleCrop()
                        .into(profileImage)
                }
            } else {
                Glide.with(itemView.context)
                    .load(R.drawable.placeholder_image_user)
                    .circleCrop()
                    .into(profileImage)
            }

            name.text = "${tasker.name ?: "Unknown"} ${tasker.surname?.firstOrNull()?.uppercase() ?: ""}.".trim()
            rating.text = tasker.rating.toString()
            reviewCount.text = "(${getReviewsText(tasker.review_count)})"
            price.text = "${tasker.hourly_rate}€/val."
            city.text = tasker.cities.joinToString(", ") { it.name }
            shortDescription.text = tasker.description

            itemView.setOnClickListener { onItemClick(tasker) }
        }
    }
    private fun getReviewsText(count: Int): String {
        return when {
            count == 1 -> "$count atsiliepimas"
            (count % 10 in 2..9) && !(count % 100 in 11..19) -> "$count atsiliepimai"
            else -> "$count atsiliepimų"
        }
    }
}
