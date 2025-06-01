package com.example.pra_project

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dojomovie.DatabaseHelper

class HistoryActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoTransactions: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize views
        recyclerView = findViewById(R.id.rvTransactions)
//        tvNoTransactions = findViewById(R.id.tvNoTransactions)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Get user ID from intent
        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId != -1) {
            loadTransactions(userId)
        }
    }

    private fun loadTransactions(userId: Int) {
        val transactions = dbHelper.getUserTransactions(userId)

        if (transactions.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvNoTransactions.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvNoTransactions.visibility = View.GONE

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = TransactionAdapter(transactions)
        }
    }
}