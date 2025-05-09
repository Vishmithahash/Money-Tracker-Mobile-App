package com.example.moneytracker.model


data class Transaction(
    val type: String,
    val category: String,
    val title: String,
    val amount: Double,
    val date: String
)
