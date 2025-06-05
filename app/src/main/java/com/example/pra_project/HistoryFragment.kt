package com.example.pra_project

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dojomovie.DatabaseHelper

class HistoryFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoTransactions: TextView
    private lateinit var tvTotalSpent: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.rvTransactions)
        tvNoTransactions = view.findViewById(R.id.tvNoTransactions)
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent)

        // Initialize database helper
        dbHelper = DatabaseHelper(requireContext())

        // Get user ID from SharedPreferences or arguments
        val userId = getUserId()
        if (userId != -1) {
            loadTransactions(userId)
        }
    }

    private fun getUserId(): Int {
        val userIdFromArgs = arguments?.getInt("USER_ID", -1) ?: -1
        if (userIdFromArgs != -1) {
            return userIdFromArgs
        }

        val sharedPref = activity?.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val loggedInPhone = sharedPref?.getString("loggedInPhone", "")

        return if (!loggedInPhone.isNullOrEmpty()) {
            dbHelper.getUserId(loggedInPhone) ?: -1
        } else {
            -1
        }
    }

    private fun loadTransactions(userId: Int) {
        val transactions = dbHelper.getUserTransactions(userId)

        if (transactions.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvNoTransactions.visibility = View.VISIBLE
            tvTotalSpent.text = "Total Spent: Rp 0"
        } else {
            recyclerView.visibility = View.VISIBLE
            tvNoTransactions.visibility = View.GONE

            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = TransactionAdapter(transactions)

            // Hitung total spent
            val total = transactions.sumOf { it.filmPrice * it.quantity}
            tvTotalSpent.text = "Total Spent: Rp ${formatRupiah(total)}"
        }
    }

    private fun formatRupiah(amount: Int): String {
        val localeID = java.util.Locale("in", "ID")
        val format = java.text.NumberFormat.getCurrencyInstance(localeID)
        return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
    }

    companion object {
        fun newInstance(userId: Int): HistoryFragment {
            return HistoryFragment().apply {
                arguments = Bundle().apply {
                    putInt("USER_ID", userId)
                }
            }
        }

        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }
}
