package com.example.pra_project

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dojomovie.DatabaseHelper

class MovieDetailsFragment : Fragment() {
    private lateinit var ivMovieCover: ImageView
    private lateinit var tvMovieTitle: TextView
    private lateinit var tvMoviePrice: TextView
    private lateinit var etQuantity: EditText
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnBuy: Button
    private lateinit var dbHelper: DatabaseHelper

    private var movieTitle: String = ""
    private var moviePrice: Int = 0
    private var movieImage: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_movie_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivMovieCover = view.findViewById(R.id.ivMovieCover)
        tvMovieTitle = view.findViewById(R.id.tvMovieTitle)
        tvMoviePrice = view.findViewById(R.id.tvMoviePrice)
        etQuantity = view.findViewById(R.id.etQuantity)
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice)
        btnBuy = view.findViewById(R.id.btnBuy)

        dbHelper = DatabaseHelper(requireContext())

        arguments?.let {
            movieTitle = it.getString("MOVIE_TITLE", "")
            moviePrice = it.getInt("MOVIE_PRICE", 0)
            movieImage = it.getString("MOVIE_IMAGE", "")
        }

        tvMovieTitle.text = movieTitle
        tvMoviePrice.text = "Price: Rp ${moviePrice}"
        Glide.with(this)
            .load(movieImage)
            .into(ivMovieCover)

        // Add text change listener for quantity
        etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateTotalPrice()
            }
        })

        btnBuy.setOnClickListener {
            validateAndBuy()
        }
    }

    private fun updateTotalPrice() {
        val quantity = etQuantity.text.toString().toIntOrNull() ?: 0
        val total = quantity * moviePrice
        tvTotalPrice.text = "Total: Rp $total"
    }

    private fun validateAndBuy() {
        val quantityStr = etQuantity.text.toString()

        if (quantityStr.isEmpty()) {
            Toast.makeText(context, "Please enter quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityStr.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            Toast.makeText(context, "Quantity must be a number greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = activity?.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val loggedInPhone = sharedPref?.getString("loggedInPhone", "")
        val userId = loggedInPhone?.let { dbHelper.getUserId(it) }

        if (userId == null) {
            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        val success = dbHelper.addTransaction(userId, movieTitle, moviePrice, quantity)
        if (success) {
            Toast.makeText(context, "Transaction successful!", Toast.LENGTH_SHORT).show()

            activity?.supportFragmentManager?.popBackStack()
        } else {
            Toast.makeText(context, "Failed to process transaction", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(title: String, price: Int, image: String): MovieDetailsFragment {
            return MovieDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString("MOVIE_TITLE", title)
                    putInt("MOVIE_PRICE", price)
                    putString("MOVIE_IMAGE", image)
                }
            }
        }
    }
}