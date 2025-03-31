package com.example.sharingserviceapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R

class GalleryAdapter(
    private val images: List<Int>, // List of image resources
    private val onImageClick: (Int) -> Unit // Function to handle image clicks (position)
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        // Inflate the gallery_item layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        // Bind the image resource to the ImageView
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int = images.size

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.gallery_image)

        fun bind(imageRes: Int, position: Int) {
            imageView.setImageResource(imageRes) // Set the image resource
            itemView.setOnClickListener {
                onImageClick(position) // Pass the position to the click listener
            }
        }
    }
}
