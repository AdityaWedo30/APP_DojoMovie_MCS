package com.example.pra_project

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class OtpActivity : AppCompatActivity() {

    private lateinit var otpInput: EditText
    private lateinit var verifyButton: Button

    private var sentOtp: String? = null
    private val CHANNEL_ID = "sms_otp_channel"
    private val NOTIFICATION_ID = 100

    // Launcher untuk permission SMS
    private val requestPermissionLauncherSms = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission SMS granted, langsung request permission notifikasi
            requestPermissionLauncherNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            Toast.makeText(this, "Izin SMS diperlukan untuk menerima kode OTP", Toast.LENGTH_LONG).show()
            // Bisa fallback ke tampilkan OTP via Toast jika ada
            sentOtp?.let {
                Toast.makeText(this, "Kode OTP Anda: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Launcher untuk permission notifikasi
    private val requestPermissionLauncherNotification = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission notifikasi granted, langsung buat channel & tampilkan notifikasi
            createSMSNotificationChannel()
            sentOtp?.let {
                showSMSOTPNotification(it)
            }
        } else {
            Toast.makeText(this, "Izin notifikasi diperlukan untuk menampilkan kode OTP", Toast.LENGTH_LONG).show()
            sentOtp?.let {
                Toast.makeText(this, "Kode OTP Anda: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        otpInput = findViewById(R.id.otpInput)
        verifyButton = findViewById(R.id.verifyButton)

        sentOtp = intent.getStringExtra("OTP")

        checkSmsPermission()

        verifyButton.setOnClickListener {
            val enteredOtp = otpInput.text.toString().trim()

            if (enteredOtp.isEmpty()) {
                Toast.makeText(this, "Masukkan kode OTP terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else if (enteredOtp == sentOtp) {
                Toast.makeText(this, "Verifikasi OTP berhasil", Toast.LENGTH_SHORT).show()
                clearOTPNotification()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Kode OTP salah", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkSmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED -> {
                    // SMS permission sudah granted, langsung cek permission notifikasi
                    checkNotificationPermission()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS) -> {
                    showSmsPermissionRationaleDialog()
                }
                else -> {
                    requestPermissionLauncherSms.launch(Manifest.permission.RECEIVE_SMS)
                }
            }
        } else {
            // Android versi lama, langsung cek permission notifikasi
            checkNotificationPermission()
        }
    }

    private fun showSmsPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Izin SMS Diperlukan")
            .setMessage("Aplikasi memerlukan izin SMS untuk menerima kode OTP secara otomatis.")
            .setPositiveButton("Berikan Izin") { _, _ ->
                requestPermissionLauncherSms.launch(Manifest.permission.RECEIVE_SMS)
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
                sentOtp?.let {
                    Toast.makeText(this, "Kode OTP Anda: $it", Toast.LENGTH_LONG).show()
                }
            }
            .show()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    createSMSNotificationChannel()
                    sentOtp?.let {
                        showSMSOTPNotification(it)
                    }
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showNotificationPermissionRationaleDialog()
                }
                else -> {
                    requestPermissionLauncherNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            createSMSNotificationChannel()
            sentOtp?.let {
                showSMSOTPNotification(it)
            }
        }
    }

    private fun showNotificationPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Izin Notifikasi Diperlukan")
            .setMessage("Aplikasi memerlukan izin notifikasi untuk menampilkan kode OTP dengan jelas.")
            .setPositiveButton("Berikan Izin") { _, _ ->
                requestPermissionLauncherNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
                sentOtp?.let {
                    Toast.makeText(this, "Kode OTP Anda: $it", Toast.LENGTH_LONG).show()
                }
            }
            .show()
    }

    private fun createSMSNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SMS OTP"
            val descriptionText = "Notifikasi untuk kode OTP via SMS"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSMSOTPNotification(otpCode: String) {
        val intent = Intent(this, OtpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OTP", otpCode)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle("Kode OTP - DoJoMovie")
            .setContentText("Kode verifikasi Anda: $otpCode")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Kode verifikasi Anda adalah: $otpCode\n\n" +
                            "Jangan bagikan kode ini kepada siapa pun. Kode berlaku selama 5 menit.\n\n" +
                            "Terima kasih telah menggunakan DoJoMovie."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .addAction(
                android.R.drawable.ic_menu_send,
                "Buka Aplikasi",
                pendingIntent
            )
            .build()

        with(NotificationManagerCompat.from(this)) {
            try {
                notify(NOTIFICATION_ID, notification)
                Toast.makeText(this@OtpActivity, "SMS OTP telah dikirim", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                Toast.makeText(this@OtpActivity, "Kode OTP: $otpCode", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun clearOTPNotification() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(NOTIFICATION_ID)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearOTPNotification()
    }
}
