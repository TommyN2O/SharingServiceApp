package com.example.sharingserviceapp.adapters

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.activitys.CreateMyTaskerProfileActivity
import com.example.sharingserviceapp.activitys.HelperListActivity
import com.example.sharingserviceapp.activitys.MoreActivity
import com.example.sharingserviceapp.activitys.TaskDetailOfferActivity
import com.example.sharingserviceapp.models.OpenedTasksHelper
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.net.URL

class OpenTasksHelperAdapter(
    private val context: HelperListActivity,
    private var custumerList: MutableList<OpenedTasksHelper>,
) : RecyclerView.Adapter<OpenTasksHelperAdapter.TaskerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskerViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.list_helper_item_custumer, parent, false)
        return TaskerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskerViewHolder, position: Int) {
        val tasker = custumerList[position]
        holder.bind(tasker)
    }

    override fun getItemCount(): Int = custumerList.size

    inner class TaskerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        private val nameText: TextView = itemView.findViewById(R.id.person_name)
        private val cityText: TextView = itemView.findViewById(R.id.city)
        private val categoryText: TextView = itemView.findViewById(R.id.category)
        private val durationText: TextView = itemView.findViewById(R.id.duration)
        private val budgetText: TextView = itemView.findViewById(R.id.budget)
        private val dueDateText: TextView = itemView.findViewById(R.id.due_date)
        private val descriptionText: TextView = itemView.findViewById(R.id.short_description)
        private val galleryRecyclerView: RecyclerView = itemView.findViewById(R.id.gallery)
        private val sendOfferButton: Button = itemView.findViewById(R.id.btn_send_offer)

        fun bind(tasker: OpenedTasksHelper) {
            val profilePhotoPath = tasker.creator?.profile_photo
            if (!profilePhotoPath.isNullOrEmpty()) {
                try {
                    val imageUrl =
                        URL(URL(ApiServiceInstance.BASE_URL), profilePhotoPath).toString()
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

            nameText.text = "${tasker.creator.name} ${tasker.creator.surname?.firstOrNull()?.uppercase() ?: ""}.".trim()
            cityText.text = tasker.city?.name
            categoryText.text = tasker.category?.name
            durationText.text = "Trukmė: ${tasker.duration ?: "-"} val."
            val formattedBudget = tasker.budget?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: "-"
            val priceText = "Biudžetas: ${formattedBudget}€"
            val spannablePrice = SpannableString(priceText)
            val greenColor = context.getColor(R.color.my_light_primary)
            val valandinisLength = "Biudžetas: ".length
            spannablePrice.setSpan(
                ForegroundColorSpan(greenColor),
                valandinisLength,
                priceText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannablePrice.setSpan(
                StyleSpan(Typeface.BOLD),
                valandinisLength,
                priceText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            budgetText.text = spannablePrice

            val lastAvailabilityDate = tasker.availability?.lastOrNull()?.date ?: "-"
            dueDateText.text = "Terminas: $lastAvailabilityDate"

            descriptionText.text = tasker.description

            val galleryImages = tasker.gallery ?: emptyList()
            if (galleryImages.isNotEmpty()) {
                galleryRecyclerView.visibility = View.VISIBLE
                galleryRecyclerView.layoutManager =
                    LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

                val baseUrl = ApiServiceInstance.BASE_URL
                galleryRecyclerView.adapter = GalleryAdapter(galleryImages, { position ->
                    context.showZoomDialog(galleryImages, position, baseUrl)
                }, baseUrl)
            } else {
                galleryRecyclerView.visibility = View.GONE
            }

            sendOfferButton.setOnClickListener {
                context.hasTaskerProfile { isTasker ->
                    if (!isTasker) {
                        showTaskerProfileAlertDialog()
                    } else {
                        navigateToTaskDetailOfferActivity(tasker)
                    }
                }
            }
        }

        private fun showTaskerProfileAlertDialog() {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.open_task_dialog_tasker_profile_tile))
                .setMessage(context.getString(R.string.open_task_dialog_tasker_profile_text))
                .setPositiveButton("Taip") { dialog, _ ->
                    context.startActivity(Intent(context, CreateMyTaskerProfileActivity::class.java))
                    dialog.dismiss()
                }
                .setNegativeButton("Ne") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun navigateToTaskDetailOfferActivity(task: OpenedTasksHelper) {
            val intent = Intent(context, TaskDetailOfferActivity::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("duration", task.duration)
            }
            context.startActivity(intent)
        }
    }
}