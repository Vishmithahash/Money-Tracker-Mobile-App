package com.example.moneytracker.uis.setting

import java.util.Date
import java.util.Locale
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneytracker.R
import com.example.moneytracker.uis.add.Add
import com.example.moneytracker.uis.analytics.Analytics
import com.example.moneytracker.uis.budget.Budget
import com.example.moneytracker.uis.dashboard.Home
import java.io.File

class Setting : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }
        findViewById<ImageButton>(R.id.navAdd).setOnClickListener {
            startActivity(Intent(this, Add::class.java))
        }
        findViewById<ImageButton>(R.id.navBudget).setOnClickListener {
            startActivity(Intent(this, Budget::class.java))
        }
        findViewById<ImageButton>(R.id.navAnalytics).setOnClickListener {
            startActivity(Intent(this, Analytics::class.java))
        }

        val btnExport = findViewById<Button>(R.id.btnExport)
        val btnRestore = findViewById<Button>(R.id.btnRestore)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val tvExportTime = findViewById<TextView>(R.id.tvExportTime)

        //  Currency Selector
        val spinnerCurrency = findViewById<Spinner>(R.id.spinnerCurrency)
        val currencies = listOf("Select Currency", "LKR")
        spinnerCurrency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)

        val settingsPref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedCurrency = settingsPref.getString("currency", "LKR")
        val index = currencies.indexOf(savedCurrency)
        if (index >= 0) {
            spinnerCurrency.setSelection(index)
        }

        var userTouched = false

        spinnerCurrency.setOnTouchListener { _, _ ->
            userTouched = true
            false
        }

        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (userTouched) {
                    val selectedCurrency = currencies[position]
                    settingsPref.edit().putString("currency", selectedCurrency).apply()
                    Toast.makeText(this@Setting, "Currency set to $selectedCurrency", Toast.LENGTH_SHORT).show()
                    userTouched = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        //  Show Export Time
        val lastTime = settingsPref.getString("lastExportTime", null)
        tvExportTime.text = if (lastTime != null) "Last export: $lastTime" else "Last export: -"

        //  Export Data
        btnExport.setOnClickListener {
            val sharedPref = getSharedPreferences("transactions", Context.MODE_PRIVATE)
            val data = sharedPref.getString("data", "[]")
            try {
                val file = File(filesDir, "transactions_backup.json")
                file.writeText(data ?: "[]")

                val time = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
                settingsPref.edit().putString("lastExportTime", time).apply()

                tvExportTime.text = "Last export: $time"
                Toast.makeText(this, "Data exported Successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show()
            }
        }

        //  Restore Data
        btnRestore.setOnClickListener {
            val file = File(filesDir, "transactions_backup.json")
            if (!file.exists()) {
                Toast.makeText(this, "No backup found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Restore Data")
            builder.setMessage("This will overwrite current transactions. Are you sure?")
            builder.setPositiveButton("Yes") { _, _ ->
                try {
                    val restoredData = file.readText()
                    val sharedPref = getSharedPreferences("transactions", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("data", restoredData).apply()
                    Toast.makeText(this, "Data restored Successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Restore failed", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        //  Log Out
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, com.example.moneytracker.Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
