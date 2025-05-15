package com.example.dojomovie

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

// Kelas DatabaseHelper digunakan untuk mengelola database SQLite pada aplikasi
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val ctx = context

    companion object {
        // Nama database
        private const val DATABASE_NAME = "DoJoMovie.db"

        // Versi database
        private const val DATABASE_VERSION = 1

        // Nama tabel dan kolom-kolom
        private const val TABLE_USER = "user"
        private const val COLUMN_ID = "id"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PASSWORD = "password"
    }

    // Fungsi ini dipanggil saat database pertama kali dibuat
    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PHONE TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT UNIQUE
            )
        """.trimIndent()
        db.execSQL(createUserTable) // Membuat tabel user
    }

    // Fungsi ini dipanggil saat terjadi upgrade versi database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER") // Hapus tabel jika sudah ada
        onCreate(db) // Buat ulang tabel
    }

    // Fungsi untuk mendaftarkan user baru ke database
    fun registerUser(phone: String, password: String): Boolean {
        // Cek apakah nomor telepon sudah terdaftar
        if (checkUserExists(phone)) {
            Toast.makeText(ctx, "Nomor telepon sudah terdaftar", Toast.LENGTH_SHORT).show()
            return false
        }

        // Cek apakah password sudah digunakan
        if (checkPasswordExists(password)) {
            Toast.makeText(ctx, "Password sudah digunakan", Toast.LENGTH_SHORT).show()
            return false
        }

        // Menambahkan data user baru ke database
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PHONE, phone)
            put(COLUMN_PASSWORD, password)
        }

        return try {
            val result = db.insertOrThrow(TABLE_USER, null, values)
            result != -1L // Berhasil jika result bukan -1
        } catch (e: Exception) {
            Toast.makeText(ctx, "Registrasi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // Fungsi untuk mengecek apakah nomor telepon sudah terdaftar
    fun checkUserExists(phone: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COLUMN_ID),
            "$COLUMN_PHONE = ?",
            arrayOf(phone),
            null, null, null
        )
        val exists = cursor.moveToFirst() // Jika ada data, berarti sudah terdaftar
        cursor.close()
        return exists
    }

    // Fungsi untuk mengecek apakah password sudah digunakan user lain
    fun checkPasswordExists(password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COLUMN_ID),
            "$COLUMN_PASSWORD = ?",
            arrayOf(password),
            null, null, null
        )
        val exists = cursor.moveToFirst() // Jika ada data, berarti password sudah digunakan
        cursor.close()
        return exists
    }

    // untuk pengecekan login
    // Fungsi untuk login user berdasarkan phone dan password
    fun loginUser(phone: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COLUMN_ID),
            "$COLUMN_PHONE = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(phone, password),
            null, null, null
        )
        val loggedIn = cursor.moveToFirst()
        cursor.close()
        return loggedIn
    }

}
