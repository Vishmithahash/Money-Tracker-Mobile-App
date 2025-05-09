package com.example.moneytracker.uis.dashboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytracker.R
import com.example.moneytracker.TransactionAdapter
import com.example.moneytracker.model.Transaction
import org.json.JSONArray
import org.json.JSONObject

class Home : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: TransactionAdapter
    private val transactionList = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Bottom Nav
        findViewById<ImageButton>(R.id.navAdd)?.setOnClickListener {
            startActivity(Intent(this, com.example.moneytracker.uis.add.Add::class.java))
        }
        findViewById<ImageButton>(R.id.navAnalytics)?.setOnClickListener {
            startActivity(Intent(this, com.example.moneytracker.uis.analytics.Analytics::class.java))
        }
        findViewById<ImageButton>(R.id.navSettings)?.setOnClickListener {
            startActivity(Intent(this, com.example.moneytracker.uis.setting.Setting::class.java))
        }
        findViewById<ImageButton>(R.id.navBudget)?.setOnClickListener {
            startActivity(Intent(this, com.example.moneytracker.uis.budget.Budget::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.transactionRecyclerView)
        emptyView = findViewById(R.id.emptyView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupSwipeToDelete()
        loadTransactions()
    }


    fun saveTransactions() {
        try {
            val sharedPref = getSharedPreferences("transactions", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            val jsonArray = JSONArray()
            transactionList.forEach {
                val obj = JSONObject().apply {
                    put("type", it.type)
                    put("category", it.category)
                    put("title", it.title)
                    put("amount", it.amount)
                    put("date", it.date)
                }
                jsonArray.put(obj)
            }
            editor.putString("data", jsonArray.toString())
            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save transactions", Toast.LENGTH_SHORT).show()
        }
    }




    fun loadTransactions() {
        try {
            transactionList.clear()
            val sharedPref = getSharedPreferences("transactions", Context.MODE_PRIVATE)
            val data = sharedPref.getString("data", "[]")
            val jsonArray = JSONArray(data)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                transactionList.add(
                    Transaction(
                        type = obj.getString("type"),
                        category = obj.getString("category"),
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        date = obj.getString("date")
                    )
                )
            }

            adapter = TransactionAdapter(
                transactionList,
                onEditClick = { transaction, position ->
                    val intent = Intent(this, com.example.moneytracker.uis.add.Add::class.java).apply {
                        putExtra("editMode", true)
                        putExtra("editIndex", position)
                        putExtra("type", transaction.type)
                        putExtra("category", transaction.category)
                        putExtra("title", transaction.title)
                        putExtra("amount", transaction.amount)
                        putExtra("date", transaction.date)
                    }
                    startActivity(intent)
                },
                onDeleteClick = { _, position ->
                    transactionList.removeAt(position)
                    saveTransactions()
                    adapter.notifyItemRemoved(position)
                    adapter.notifyItemRangeChanged(position, transactionList.size)
                    updateSummaryCards()
                    toggleEmptyState()
                }
            )
            recyclerView.adapter = adapter
            toggleEmptyState()
            updateSummaryCards()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load transactions", Toast.LENGTH_SHORT).show()
        }
    }


    private fun toggleEmptyState() {
        if (transactionList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                transactionList.removeAt(position)
                saveTransactions()
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, transactionList.size)
                updateSummaryCards()
                toggleEmptyState()
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun updateSummaryCards() {
        val incomeTotal = transactionList.filter { it.type.equals("income", true) }.sumOf { it.amount }
        val expenseTotal = transactionList.filter { it.type.equals("expense", true) }.sumOf { it.amount }
        val balance = incomeTotal - expenseTotal

        findViewById<TextView>(R.id.totalBalanceCard).text = "Total Balance\nLKR %.2f".format(balance)
        findViewById<TextView>(R.id.incomeCard).text = "Income\nLKR %.2f".format(incomeTotal)
        findViewById<TextView>(R.id.expenseCard).text = "Expense\nLKR %.2f".format(expenseTotal)

        findViewById<TextView>(R.id.totalBalanceCard).setTextColor(
            if (balance >= 0) Color.parseColor("#00C853") else Color.parseColor("#D32F2F")
        )
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }
}