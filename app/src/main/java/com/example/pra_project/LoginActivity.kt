package com.example.pra_project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dojomovie.DatabaseHelper
import com.example.dojomovie.fetchAndStoreFilms

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var passwordEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var rememberCheckBox: CheckBox // Jika tidak ada di layout, boleh dihapus

    private var pendingPhoneNumber: String? = null
    private var pendingMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)
        fetchAndStoreFilms(this)

        phoneEditText = findViewById(R.id.numberPhone)
        passwordEditText = findViewById(R.id.passwordET)
        loginButton = findViewById(R.id.loginButton)
        registerTextView = findViewById(R.id.registerTextView)
        rememberCheckBox = findViewById(R.id.rememberCheckBox) // Jika tidak ada di layout, hapus baris ini

        loginButton.setOnClickListener {
            val phone = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi nomor dan password", Toast.LENGTH_SHORT).show()
            } else {
                val success = dbHelper.loginUser(phone, password)
                if (success) {
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()

                    // Simpan nomor telepon ke SharedPreferences
                    val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                    sharedPref.edit().putString("loggedInPhone", phone).apply()

                    // Kirim OTP via SMS
                    val otp = generateOTP()
                    val message = "Kode OTP Anda adalah: $otp"
                    checkSendSMSPermission(phone, message)

                    // Pindah ke halaman OTP
                    val intent = Intent(this, OtpActivity::class.java)
                    intent.putExtra("OTP", otp)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Nomor atau password salah", Toast.LENGTH_SHORT).show()
                }
            }
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkSendSMSPermission(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pendingPhoneNumber = phoneNumber
            pendingMessage = message

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                100
            )
        } else {
            sendSMS(phoneNumber, message)
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "SMS terkirim ke $phoneNumber", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mengirim SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            pendingPhoneNumber?.let { phone ->
                pendingMessage?.let { msg ->
                    sendSMS(phone, msg)
                }
            }
        } else {
            Toast.makeText(this, "Izin untuk mengirim SMS ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateOTP(length: Int = 6): String {
        val chars = ('0'..'9')
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}
