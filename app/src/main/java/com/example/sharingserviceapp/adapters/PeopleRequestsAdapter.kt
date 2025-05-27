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
import kotlin.text.firstOrNull

class PeopleRequestsAdapter(
    private val context: Context,
    private var requestList: List<TaskResponse>,
    private val onItemClick: (TaskResponse) -> Unit
) : RecyclerView.Adapter<PeopleRequestsAdapter.PeopleRequestViewHolder>() {

    private val statusTranslations = mapOf(
        "Pending" to "Laukiama patvirtinimo",
        "Waiting for Payment" to "Laukiama apmokėjimo",
        "Declined" to "Atmestas",
        "Canceled" to "Atšauktas",
        "Completed" to "Užbaigtas",
    )
    inner class PeopleRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.img_category)
        val txtName: TextView = itemView.findViewById(R.id.txt_user_name)
        val txtCategory: TextView = itemView.findViewById(R.id.txt_category)
        val txtDateTime: TextView = itemView.findViewById(R.id.txt_day_time)
        val txtCity: TextView = itemView.findViewById(R.id.txt_city)
        val txtStatus: TextView = itemView.findViewById(R.id.txt_status)

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

            txtCategory.text = "${request.categories.joinToString { it.name }}"

            val slot = request.availability.firstOrNull()
            txtDateTime.text = slot?.let { "${it.date}, ${it.time.dropLast(3)}" } ?: "Nenurodyta"

            txtCity.text = request.city.name

            val statusOriginal = request.status.replaceFirstChar { it.uppercase() }
            val translatedStatus = statusTranslations[statusOriginal] ?: statusOriginal
            txtStatus.text = translatedStatus

            when (statusOriginal) {
                "Pending" -> txtStatus.setTextColor(context.resources.getColor(R.color.status_pending))
                "Waiting for Payment" -> txtStatus.setTextColor(context.resources.getColor(R.color.status_waiting_payment))
                "Declined"->txtStatus.setTextColor(context.resources.getColor(R.color.status_declined))
                "Canceled"->txtStatus.setTextColor(context.getColor(R.color.status_declined))
                else -> txtStatus.setTextColor(context.resources.getColor(R.color.status_default))
            }

            itemView.setOnClickListener { onItemClick(request) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleRequestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_people_requests, parent, false)
        return PeopleRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeopleRequestViewHolder, position: Int) {
        holder.bind(requestList[position])
    }

    override fun getItemCount(): Int = requestList.size
}
