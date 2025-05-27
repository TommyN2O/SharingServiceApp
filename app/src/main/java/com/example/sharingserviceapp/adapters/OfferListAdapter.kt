package com.example.sharingserviceapp.adapters

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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.Offer
import com.example.sharingserviceapp.network.ApiServiceInstance
import java.net.URL

class OfferListAdapter(
    private val offers: List<Offer>,
    private val onAccept: (Offer) -> Unit,

) : RecyclerView.Adapter<OfferListAdapter.OfferViewHolder>() {

    inner class OfferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView = itemView.findViewById(R.id.imgPhoto)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtDateTime: TextView = itemView.findViewById(R.id.txtDateTime)
        val txtDuration: TextView = itemView.findViewById(R.id.txtDuration)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        val btnAccept: ImageView = itemView.findViewById(R.id.btnAccept)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_offer, parent, false)
        return OfferViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val offer = offers[position]
        val taskerImageUrl = offer.tasker.profile_photo.let {
            URL(URL(ApiServiceInstance.BASE_URL), it)
        }

        Glide.with(holder.itemView.context)
            .load(taskerImageUrl)
            .placeholder(R.drawable.placeholder_image_user)
            .error(R.drawable.error)
            .circleCrop()
            .into(holder.imgPhoto)
        holder.txtName.text = offer.tasker.name
        holder.txtDateTime.text = "Data ir laikas: ${offer.availability.date}, ${offer.availability.time}"
        holder.txtDuration.text = "Trukmė: ${offer.duration} val."
        val priceText = "Valandinis: ${offer.price}€/val."
        val spannablePrice = SpannableString(priceText)
        val greenColor = ContextCompat.getColor(holder.itemView.context, R.color.my_light_primary)
        val valandinisLength = "Valandinis: ".length
        spannablePrice.setSpan(
            ForegroundColorSpan(greenColor), // Use the actual color value
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
        holder.txtPrice.text = spannablePrice
        holder.btnAccept.setOnClickListener { onAccept(offer) }
    }

    override fun getItemCount(): Int = offers.size
}
