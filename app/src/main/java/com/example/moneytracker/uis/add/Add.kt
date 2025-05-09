package com.example.moneytracker.uis.add

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneytracker.R
import com.example.moneytracker.uis.analytics.Analytics
import com.example.moneytracker.uis.budget.Budget
import com.example.moneytracker.uis.dashboard.Home
import com.example.moneytracker.uis.setting.Setting
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Add : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.navAnalytics).setOnClickListener {
            val intent = Intent(this, com.example.moneytracker.uis.analytics.Analytics::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.navBudget).setOnClickListener {
            val intent = Intent(this, Budget::class.java)
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

        val spinnerType = findViewById<Spinner>(R.id.spinnerType)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val btnPickDate = findViewById<ImageButton>(R.id.btnPickDate)
        val btnAdd = findViewById<Button>(R.id.btnAddTransaction)

        val types = listOf("Select type", "Income", "Expense")
        val incomeCats = listOf("Select Category", "Salary", "Investments", "Part-Time", "Bonus", "Others")
        val expenseCats = listOf("Select Category", "Shopping", "Food", "Transport", "Gifts", "Sports", "Education", "Utilities")

        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        val editMode = intent.getBooleanExtra("editMode", false)
        val editIndex = intent.getIntExtra("editIndex", -1)

        val editType = intent.getStringExtra("type") ?: ""
        val editCategory = intent.getStringExtra("category") ?: ""
        val editTitle = intent.getStringExtra("title") ?: ""
        val editAmount = intent.getDoubleExtra("amount", 0.0)
        val editDate = intent.getStringExtra("date") ?: ""

        if (editMode) {
            etTitle.setText(editTitle)
            etAmount.setText(editAmount.toString())
            tvDate.text = editDate
            spinnerType.setSelection(types.indexOfFirst { it.equals(editType, true) })
        }

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = types[position]
                val categoryList = when (selected) {
                    "Income" -> incomeCats
                    "Expense" -> expenseCats
                    else -> listOf("Select Category")
                }
                spinnerCategory.adapter = ArrayAdapter(this@Add, android.R.layout.simple_spinner_dropdown_item, categoryList)

                if (editMode) {
                    spinnerCategory.post {
                        spinnerCategory.setSelection(categoryList.indexOfFirst { it.equals(editCategory, true) })
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val dp = DatePickerDialog(this, { _, y, m, d ->
                val formatted = "${String.format("%02d", d)}/${String.format("%02d", m + 1)}/$y"
                tvDate.text = formatted
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dp.show()
        }

        btnAdd.setOnClickListener {
            val type = spinnerType.selectedItem.toString()
            val category = spinnerCategory.selectedItem.toString()
            val title = etTitle.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val selectedDateText = tvDate.text.toString()

            if (type == "Select type") {
                Toast.makeText(this, "Please select transaction type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (category == "Select Category") {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (title.isEmpty()) {
                etTitle.error = "Title cannot be empty"
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount < 0) {
                etAmount.error = "Enter a valid positive amount"
                return@setOnClickListener
            }

            val cal = Calendar.getInstance()
            val today = cal.time
            val dateToUse = if (selectedDateText == "Add Date") {
                "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
            } else {
                selectedDateText
            }

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = sdf.parse(dateToUse)
            if (parsedDate != null && parsedDate.after(today)) {
                Toast.makeText(this, "Future dates are not allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPref = getSharedPreferences("transactions", Context.MODE_PRIVATE)
            val data = sharedPref.getString("data", "[]")
            val jsonArray = JSONArray(data)

            var incomeTotal = 0.0
            var expenseTotal = 0.0

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val t = obj.getString("type")
                val amt = obj.getDouble("amount")
                if (t.equals("income", true)) incomeTotal += amt
                if (t.equals("expense", true)) expenseTotal += amt
            }

            if (type.equals("expense", true) && incomeTotal == 0.0) {
                Toast.makeText(this, "Please add at least one income before adding expenses", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (type.equals("expense", true) && (expenseTotal + amount > incomeTotal)) {
                Toast.makeText(this, "This transaction exceeds your income limit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val obj = JSONObject().apply {
                put("type", type)
                put("category", category)
                put("title", title)
                put("amount", amount)
                put("date", dateToUse)
            }

            if (editMode && editIndex in 0 until jsonArray.length()) {
                jsonArray.put(editIndex, obj)
            } else {
                jsonArray.put(obj)
            }

            sharedPref.edit().putString("data", jsonArray.toString()).apply()
            Toast.makeText(this, if (editMode) "Transaction updated" else "Transaction added", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, com.example.moneytracker.uis.dashboard.Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()

        }
    }
}
