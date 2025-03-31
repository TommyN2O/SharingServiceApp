package com.example.sharingserviceapp.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Review

class ReviewAdapter(private val reviewList: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reviewerName: TextView = view.findViewById(R.id.reviewer_name)
        val reviewerImage: ImageView = view.findViewById(R.id.reviewer_profile)
        val reviewRating: TextView = view.findViewById(R.id.review_rating)
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
        holder.reviewerName.text = review.reviewerName
        holder.reviewRating.text = String.format("%.1f", review.rating)
        holder.reviewDate.text = review.date
        holder.reviewText.text = review.reviewText
        holder.reviewerImage.setImageResource(review.reviewerImage)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
}