package com.example.pra_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dojomovie.DatabaseHelper
import com.example.pra_project.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.dojomovie.fetchAndStoreFilms


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = DatabaseHelper(this)

        dbHelper.updateFilmImage("MV001", "https://www.hollywoodreporter.com/wp-content/uploads/2014/02/new_godzilla_poster.jpg")
        dbHelper.updateFilmImage("MV002", "https://www.movieposters.com/cdn/shop/files/final-destination-bloodlines_rakfqp6l.jpg?v=1746032700")
        dbHelper.updateFilmImage("MV003", "https://images-cdn.ubuy.co.id/636b04c3114c9d4a86576c33-no-time-to-die-james-bond-007-movie.jpg")

        replaceFragment(HomeFragment())
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_history -> {
                    replaceFragment(HistoryFragment())
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

//        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
//        val navController = findNavController(R.id.nav_host_fragment)
//        navView.setupWithNavController(navController)
    }

    private fun replaceFragment(fragment : Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }
}