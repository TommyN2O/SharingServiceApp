package com.example.sharingserviceapp.adapters

import android.annotation.SuppressLint
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
            textDate.text = transaction.paymentDate
            textStatus.text = transaction.paymentStatus
            textAmount.text = if (transaction.type == "earning") "+$${transaction.amount}" else "-$${transaction.amount}"
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
