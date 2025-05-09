package com.example.moneytracker.uis.budget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneytracker.R
import com.example.moneytracker.uis.add.Add
import com.example.moneytracker.uis.analytics.Analytics
import com.example.moneytracker.uis.setting.Setting
import org.json.JSONArray

class Budget : AppCompatActivity() {

    private lateinit var tvBudgetAmount: TextView
    private lateinit var tvBudgetSummary: TextView
    private var currentBudget = 0.0
    private var totalExpense = 0.0
    private val channelId = "budget_alert_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, com.example.moneytracker.uis.dashboard.Home::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.navAdd).setOnClickListener {
            val intent = Intent(this, Add::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.navAnalytics).setOnClickListener {
            val intent = Intent(this, Analytics::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.navSettings).setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvBudgetAmount = findViewById(R.id.tvBudgetAmount)
        tvBudgetSummary = findViewById(R.id.tvBudgetSummary)

        val btnAddBudget = findViewById<Button>(R.id.btnAddBudget)
        val btnSaveBudget = findViewById<Button>(R.id.btnSaveBudget)

        btnAddBudget.setOnClickListener { showAddBudgetDialog() }
        btnSaveBudget.setOnClickListener { saveBudget() }

        createNotificationChannel()
        loadBudget()
        calculateTotalExpenses()
        updateSummary()
    }

    override fun onResume() {
        super.onResume()
        calculateTotalExpenses()
        updateSummary()
    }

    private fun showAddBudgetDialog() {
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Enter budget amount"
        }

        AlertDialog.Builder(this)
            .setTitle("Set Monthly Budget")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val value = input.text.toString().toDoubleOrNull()
                if (value != null && value >= 0) {
                    currentBudget = value
                    tvBudgetAmount.text = "LKR %.2f".format(currentBudget)
                } else {
                    Toast.makeText(this, "Invalid budget amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveBudget() {
        val prefs = getSharedPreferences("budget", Context.MODE_PRIVATE)
        prefs.edit().putFloat("monthlyBudget", currentBudget.toFloat()).apply()
        Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show()
        updateSummary()
    }

    private fun loadBudget() {
        val prefs = getSharedPreferences("budget", Context.MODE_PRIVATE)
        currentBudget = prefs.getFloat("monthlyBudget", 0.0f).toDouble()
        tvBudgetAmount.text = "LKR %.2f".format(currentBudget)
    }

    private fun calculateTotalExpenses() {
        val prefs = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val data = prefs.getString("data", "[]")
        val jsonArray = JSONArray(data)

        totalExpense = 0.0
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getString("type").equals("expense", ignoreCase = true)) {
                totalExpense += obj.getDouble("amount")
            }
        }
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Alerts"
            val description = "Notifications when expenses exceed budget"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun updateSummary() {
        tvBudgetSummary.text = "Monthly Budget : LKR %.0f\nTotal Expenses    : LKR %.0f"
            .format(currentBudget, totalExpense)

        if (totalExpense > currentBudget) {
            sendBudgetExceededNotification()
        }
    }

    private fun sendBudgetExceededNotification() {
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Budget Alert 🚨")
            .setContentText("You have exceeded your monthly budget!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(this).notify(101, builder.build())
        } catch (e: SecurityException) {
            Toast.makeText(this, "Notification permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

}