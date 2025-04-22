package com.example.sharingserviceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import java.net.URL

class GalleryAdapter(
    private val images: List<String>, // List of relative image URLs
    private val onImageClick: (Int) -> Unit, // Function to handle image clicks (position)
    private val baseUrl: String // The base URL where the images are hosted
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        // Inflate the gallery_item layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        // Build the full image URL using the base URL
        val fullImageUrl = URL(URL(baseUrl), images[position])
        // Load the image into the ImageView using Glide
        holder.bind(fullImageUrl.toString(), position)
    }

    override fun getItemCount(): Int = images.size

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.gallery_image)

        fun bind(imageUrl: String, position: Int) {
            // Use Glide to load the image from the full URL
            Glide.with(itemView.context)
                .load(imageUrl)
                .override(300, 300)
                .placeholder(R.drawable.placeholder_image) // Placeholder image
                .error(R.drawable.error) // Error image in case the URL fails
                .into(imageView)

            // Set up the click listener for the image
            itemView.setOnClickListener {
                onImageClick(position) // Pass the position to the click listener
            }
        }
    }
}
