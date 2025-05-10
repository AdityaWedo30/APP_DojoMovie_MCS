package com.example.pra_project

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Pastikan menggunakan versi terbaru dari androidx.activity
        setContentView(R.layout.activity_main)

        // Pastikan ID "main" ada di activity_main.xml
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Menggunakan Handler untuk menunggu 2 detik sebelum berpindah ke LoginActivity
        Handler(mainLooper).postDelayed({
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Menutup MainActivity agar tidak bisa kembali ke halaman ini
        }, 2000)  // 2000 milidetik = 2 detik
    }
}
