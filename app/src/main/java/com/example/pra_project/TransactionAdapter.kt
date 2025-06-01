package com.example.pra_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dojomovie.Transaction
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFilmTitle: TextView = view.findViewById(R.id.tvFilmTitle)
        val tvFilmPrice: TextView = view.findViewById(R.id.tvFilmPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvTransactionDate: TextView = view.findViewById(R.id.tvTransactionDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        holder.tvFilmTitle.text = transaction.filmTitle
        holder.tvFilmPrice.text = "Price: ${formatter.format(transaction.filmPrice)}"
        holder.tvQuantity.text = "Quantity: ${transaction.quantity}"
        holder.tvTransactionDate.text = "Date: ${transaction.transactionDate}"
    }

    override fun getItemCount() = transactions.size
}