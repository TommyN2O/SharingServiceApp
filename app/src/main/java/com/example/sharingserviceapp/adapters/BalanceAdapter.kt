package com.example.sharingserviceapp.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.models.WalletPaymentsResponse

class BalanceAdapter(private val transactions: List<WalletPaymentsResponse>) : RecyclerView.Adapter<BalanceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textFullname: TextView = view.findViewById(R.id.text_fullname)
        val textCategory: TextView = view.findViewById(R.id.text_category)
        val textDate: TextView = view.findViewById(R.id.text_date)
        val textStatus: TextView = view.findViewById(R.id.text_status)
        val textAmount: TextView = view.findViewById(R.id.text_amount)

        @SuppressLint("SetTextI18n")
        fun bind(transaction: WalletPaymentsResponse) {
            textFullname.text = "${transaction.otherParty.name} ${transaction.otherParty.surname?.firstOrNull()?.uppercaseChar() ?: ""}."
            textCategory.text = transaction.task.category
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")

            val outputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())

            val parsedDate = try {
                inputFormat.parse(transaction.paymentDate)
            } catch (e: Exception) {
                null
            }

            textDate.text = parsedDate?.let { outputFormat.format(it) } ?: transaction.paymentDate
            textStatus.text = transaction.paymentStatus
            textAmount.text = if (transaction.type == "earning") "+€${transaction.amount}" else "-€${transaction.amount}"
            textAmount.setTextColor(
                if (transaction.type == "earning") Color.parseColor("#4CAF50") // Green
                else Color.parseColor("#F44336") // Red
            )
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
