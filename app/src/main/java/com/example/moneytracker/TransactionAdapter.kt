package com.example.moneytracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytracker.model.Transaction

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onEditClick: (Transaction, Int) -> Unit,
    private val onDeleteClick: (Transaction, Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvTitle.text = transaction.title
        holder.tvDate.text = transaction.date
        holder.tvCategory.text = transaction.category
        holder.tvType.text = transaction.type

        holder.tvAmount.text = "Rs.${transaction.amount}"
        holder.tvAmount.setTextColor(
            if (transaction.type.equals("income", true))
                Color.parseColor("#00C853") else Color.parseColor("#FF0000")
        )

        holder.btnEdit.setOnClickListener {
            onEditClick(transaction, position)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(transaction, position)
        }
    }

    override fun getItemCount(): Int = transactions.size
}
