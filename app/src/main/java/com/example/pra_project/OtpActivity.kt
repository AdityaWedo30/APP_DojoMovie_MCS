package com.example.pra_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OtpActivity : AppCompatActivity() {

    private lateinit var otpInput: EditText
    private lateinit var verifyButton: Button

    private var sentOtp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        otpInput = findViewById(R.id.otpInput)
        verifyButton = findViewById(R.id.verifyButton)

        // Ambil OTP dari Intent
        sentOtp = intent.getStringExtra("OTP")

        verifyButton.setOnClickListener {
            val enteredOtp = otpInput.text.toString().trim()

            if (enteredOtp.isEmpty()) {
                Toast.makeText(this, "Masukkan kode OTP terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else if (enteredOtp == sentOtp) {
                Toast.makeText(this, "Verifikasi OTP berhasil", Toast.LENGTH_SHORT).show()

                // Pindah ke halaman utama (ganti dengan activity tujuanmu)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Kode OTP salah", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
