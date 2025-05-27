package com.example.sharingserviceapp.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Review
import com.example.sharingserviceapp.models.ReviewList
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(private val reviewList: List<ReviewList>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reviewerName: TextView = view.findViewById(R.id.reviewer_name)
        val reviewerImage: ImageView = view.findViewById(R.id.reviewer_profile)
        val reviewRatingBar: RatingBar  = view.findViewById(R.id.review_rating_bar)
        val reviewDate: TextView = view.findViewById(R.id.review_date)
        val reviewText: TextView = view.findViewById(R.id.review_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.reviewerName.text = "${review.reviewer.name} ${review.reviewer.surname.firstOrNull()?.uppercase() ?: ""}.".trim()
        holder.reviewRatingBar.rating = review.rating.toFloat()
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM yyyy", Locale("lt"))

        try {
            val parsedDate = inputFormat.parse(review.created_at)
            var formattedDate = outputFormat.format(parsedDate!!)
            formattedDate = formattedDate.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale("lt")) else it.toString()
            }
            holder.reviewDate.text = formattedDate
        } catch (e: Exception) {
            holder.reviewDate.text = review.created_at
        }

        holder.reviewText.text = review.review
        val profilePhotoPath = review.reviewer.profile_photo
        if (!profilePhotoPath.isNullOrEmpty()) {
            try {
                val imageUrl = URL(URL(ApiServiceInstance.BASE_URL), profilePhotoPath).toString()
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image_user)
                    .error(R.drawable.error)
                    .circleCrop()
                    .into(holder.reviewerImage)
            } catch (e: Exception) {
                e.printStackTrace()
                Glide.with(holder.itemView.context)
                    .load(R.drawable.placeholder_image_user)
                    .circleCrop()
                    .into(holder.reviewerImage)
            }
        } else {
            Glide.with(holder.itemView.context)
                .load(R.drawable.placeholder_image_user)
                .circleCrop()
                .into(holder.reviewerImage)
        }
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
}