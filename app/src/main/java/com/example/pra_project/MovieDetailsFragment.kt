package com.example.pra_project

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_movie_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupDatabase()
        loadMovieData()
        setupListeners()
    }

    private fun initializeViews(view: View) {
        ivMovieCover = view.findViewById(R.id.ivMovieCover)
        tvMovieTitle = view.findViewById(R.id.tvMovieTitle)
        tvMoviePrice = view.findViewById(R.id.tvMoviePrice)
        etQuantity = view.findViewById(R.id.etQuantity)
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice)
        btnBuy = view.findViewById(R.id.btnBuy)
    }

    private fun setupDatabase() {
        dbHelper = DatabaseHelper(requireContext())
    }

//    private fun loadMovieData() {
//        arguments?.let {
//            movieTitle = it.getString("MOVIE_TITLE", "")
//            moviePrice = it.getInt("MOVIE_PRICE", 0)
//            movieImage = it.getString("MOVIE_IMAGE", "")
//        }
//
//        tvMovieTitle.text = movieTitle
//        tvMoviePrice.text = "Price: Rp $moviePrice"
//        Glide.with(this).load(movieImage).into(ivMovieCover)
//    }
        private fun loadMovieData() {
            val filmId = arguments?.getString("FILM_ID") ?: return

            val film = dbHelper.getFilmById(filmId)
            if (film != null) {
                movieImage = film.image
                movieTitle = film.title
                moviePrice = film.price


                Glide.with(this)
                    .load(movieImage)
                    .into(ivMovieCover)
                tvMovieTitle.text = film.title
                tvMoviePrice.text = "Price: Rp ${film.price}"


            } else {
                Toast.makeText(context, "Film not found", Toast.LENGTH_SHORT).show()
            }
        }

    private fun setupListeners() {
        etQuantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateTotalPrice()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnBuy.setOnClickListener { validateAndBuy() }
    }

    private fun updateTotalPrice() {
        val quantity = etQuantity.text.toString().toIntOrNull() ?: 0
        val total = quantity * moviePrice
        tvTotalPrice.text = "Total: Rp $total"
    }

    private fun validateAndBuy() {
        val filmId = arguments?.getString("FILM_ID") ?: return
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

        if (loggedInPhone.isNullOrEmpty()) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = dbHelper.getUserId(loggedInPhone)
        if (userId == null) {
            Toast.makeText(context, "User not found in database", Toast.LENGTH_SHORT).show()
            return
        }

        val success = dbHelper.addTransaction(userId, filmId, quantity)
        if (success) {
            Toast.makeText(context, "Transaction successful! Movie purchased.", Toast.LENGTH_LONG).show()
            etQuantity.setText("")
            updateTotalPrice()
            activity?.supportFragmentManager?.popBackStack()
        } else {
            Toast.makeText(context, "Failed to process transaction. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(filmId: String): MovieDetailsFragment {
            val fragment = MovieDetailsFragment()
            val args = Bundle()
            args.putString("FILM_ID", filmId)
            fragment.arguments = args
            return fragment
        }
    }
}
