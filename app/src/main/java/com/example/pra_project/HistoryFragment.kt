package com.example.pra_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dojomovie.DatabaseHelper

class HistoryFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoTransactions: View

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
//        tvNoTransactions = view.findViewById(R.id.tvNoTransactions)

        // Initialize database helper
        dbHelper = DatabaseHelper(requireContext())

        // Get user ID from arguments
        val userId = arguments?.getInt("USER_ID", -1) ?: -1
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

            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = TransactionAdapter(transactions)
        }
    }

    companion object {
        fun newInstance(userId: Int): HistoryFragment {
            return HistoryFragment().apply {
                arguments = Bundle().apply {
                    putInt("USER_ID", userId)
                }
            }
        }
    }
}