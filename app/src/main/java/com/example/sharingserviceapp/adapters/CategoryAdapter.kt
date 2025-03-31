package com.example.sharingserviceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Category


class CategoryAdapter(private val categoryList: List<Category>,
                      private val onCategoryClick: (String) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryImage: ImageView = view.findViewById(R.id.category_image)
        val categoryTitle: TextView = view.findViewById(R.id.category_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.categoryTitle.text = category.name
        holder.categoryImage.setImageResource(category.imageResId)

        // On item click, pass the category to the new activity
        holder.itemView.setOnClickListener {
            onCategoryClick(category.name)
        }
    }

    override fun getItemCount(): Int = categoryList.size
}