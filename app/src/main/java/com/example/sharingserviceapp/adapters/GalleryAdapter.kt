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
    private val images: List<String>,
    private val onImageClick: (Int) -> Unit,
    private val baseUrl: String
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val fullImageUrl = URL(URL(baseUrl), images[position])
        holder.bind(fullImageUrl.toString(), position)
    }

    override fun getItemCount(): Int = images.size

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.gallery_image)

        fun bind(imageUrl: String, position: Int) {
            Glide.with(itemView.context)
                .load(imageUrl)
                .override(300, 300)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error)
                .into(imageView)

            itemView.setOnClickListener {
                onImageClick(position)
            }
        }
    }
}
