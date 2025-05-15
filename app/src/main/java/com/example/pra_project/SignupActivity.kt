package com.example.pra_project

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dojomovie.DatabaseHelper // Ganti sesuai nama package DatabaseHelper Anda

class SignupActivity : AppCompatActivity() {

    // Deklarasi variabel untuk komponen UI dan database
    private lateinit var editPhone: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Inisialisasi view dari XML
        editPhone = findViewById(R.id.numberPhone)
        editPassword = findViewById(R.id.passwordET)
        btnRegister = findViewById(R.id.registerButton)

        // Inisialisasi database helper
        dbHelper = DatabaseHelper(this)

        // Event klik tombol "Daftar"
        btnRegister.setOnClickListener {
            val phone = editPhone.text.toString().trim()
            val password = editPassword.text.toString().trim()

            // Validasi input
            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            } else {
                // Coba daftarkan user ke database
                val success = dbHelper.registerUser(phone, password)
                if (success) {
                    Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke layar sebelumnya
                }
            }
        }
    }
}
