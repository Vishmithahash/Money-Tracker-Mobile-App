package com.example.moneytracker.uis.analytics

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneytracker.R
import com.example.moneytracker.uis.add.Add
import com.example.moneytracker.uis.budget.Budget
import com.example.moneytracker.uis.setting.Setting
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import org.json.JSONArray

class Analytics : AppCompatActivity() {

    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, com.example.moneytracker.uis.dashboard.Home::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.navAdd).setOnClickListener {
            val intent = Intent(this, Add::class.java)
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

        pieChart = findViewById(R.id.pieChart)
        setupPieChart()
        loadChartData()
    }

    private fun setupPieChart() {
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(14f)
        pieChart.centerText = "Income vs Expense"
        pieChart.setCenterTextSize(16f)
        pieChart.description.isEnabled = false
        val legend = pieChart.legend
        legend.isEnabled = false
        legend.textSize = 14f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
    }

    private fun loadChartData() {
        val sharedPref = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val data = sharedPref.getString("data", "[]")
        val jsonArray = JSONArray(data)

        var incomeTotal = 0.0
        var expenseTotal = 0.0

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val type = obj.getString("type")
            val amount = obj.getDouble("amount")

            if (type.equals("income", true)) {
                incomeTotal += amount
            } else if (type.equals("expense", true)) {
                expenseTotal += amount
            }
        }

        val entries = mutableListOf<PieEntry>()
        if (incomeTotal > 0) entries.add(PieEntry(incomeTotal.toFloat(), "Income"))
        if (expenseTotal > 0) entries.add(PieEntry(expenseTotal.toFloat(), "Expense"))

        val colors = listOf(Color.parseColor("#00C853"), Color.parseColor("#D32F2F"))

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.invalidate() // refresh chart
    }
}
