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

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var password: EditText
    private lateinit var number: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView

    private var pendingPhoneNumber: String? = null
    private var pendingMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        password = findViewById(R.id.passwordET)
        number = findViewById(R.id.numberPhone)
        loginButton = findViewById(R.id.loginButton)
        registerTextView = findViewById(R.id.registerTextView)

        registerTextView.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val phone = number.text.toString()
            val pass = password.text.toString()

            if (phone.isNotEmpty() && pass.isNotEmpty()) {
                // Contoh penggunaan sendSMS setelah login berhasil
                val message = "Login berhasil ke PRA Project"
                checkSendSMSPermission(phone, message)
            } else {
                Toast.makeText(this, "Nomor dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkSendSMSPermission(phoneNumber: String, message: String) {
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

    fun sendSMS(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        Toast.makeText(this, "SMS terkirim ke $phoneNumber", Toast.LENGTH_SHORT).show()
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
}
