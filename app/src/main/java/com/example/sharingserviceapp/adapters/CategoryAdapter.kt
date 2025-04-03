package com.example.sharingserviceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Category
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.net.URL



class CategoryAdapter(
    private val categoryList: List<Category>,
    private val onItemClick: ((Category) -> Unit)? = null
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    companion object {
        private const val BASE_IMAGE_URL = ApiServiceInstance.BASE_URL //"https://your.api.url" // Update with actual base URL
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.category_title)
        val image: ImageView = view.findViewById(R.id.category_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        holder.name.text = category.name

        // Ensure full image URL
        val fullImageUrl = URL(URL(BASE_IMAGE_URL), category.image)
        //val fullImageUrl = if (category.image.startsWith("/")) BASE_IMAGE_URL + category.image else category.image
        Glide.with(holder.itemView.context).load(fullImageUrl).into(holder.image)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(category)
        }
    }

    override fun getItemCount() = categoryList.size
}
