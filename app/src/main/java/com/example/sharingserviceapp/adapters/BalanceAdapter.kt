package com.example.sharingserviceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Transaction
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.net.URL

class BalanceAdapter(private val transactions: List<Transaction>) : RecyclerView.Adapter<BalanceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageProfile: ImageView = view.findViewById(R.id.image_profile)
        val textFullname: TextView = view.findViewById(R.id.text_fullname)
        val textCategory: TextView = view.findViewById(R.id.text_category)
        val textDate: TextView = view.findViewById(R.id.text_date)
        val textStatus: TextView = view.findViewById(R.id.text_status)
        val textAmount: TextView = view.findViewById(R.id.text_amount)

        fun bind(transaction: Transaction) {
            textFullname.text = "${transaction.sender.name} ${transaction.sender.surname?.firstOrNull()?.uppercaseChar() ?: ""}."
            textCategory.text = transaction.category
            textDate.text = transaction.date
            textStatus.text = transaction.status
           textAmount.text = if (transaction.amount >= 0) "+$${transaction.amount}" else "-$${transaction.amount}"


            transaction.sender.profile_photo?.let {
                try {
                    val fullImageUrl = URL(URL(ApiServiceInstance.BASE_URL), it)
                    Glide.with(itemView.context)
                        .load(fullImageUrl.toString())
                        .placeholder(R.drawable.placeholder_image_user)
                        .error(R.drawable.error)
                        .circleCrop()
                        .into(imageProfile)
                } catch (e: Exception) {
                    imageProfile.setImageResource(R.drawable.placeholder_image_user)
                }
            } ?: imageProfile.setImageResource(R.drawable.placeholder_image_user)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.balance_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size
}
