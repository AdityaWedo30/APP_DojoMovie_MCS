package com.example.dojomovie

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

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

        // Transaction table
        private const val TABLE_TRANSACTION = "transactions"
        private const val COLUMN_TRANSACTION_ID = "transaction_id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_FILM_TITLE = "film_title"
        private const val COLUMN_FILM_PRICE = "film_price"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_TRANSACTION_DATE = "transaction_date"
    }

    // Fungsi ini dipanggil saat database pertama kali dibuat
    override fun onCreate(db: SQLiteDatabase) {
        // Create User table
        val createUserTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PHONE TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT UNIQUE
            )
        """.trimIndent()

        // Create Transaction table
        val createTransactionTable = """
            CREATE TABLE $TABLE_TRANSACTION (
                $COLUMN_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER,
                $COLUMN_FILM_TITLE TEXT,
                $COLUMN_FILM_PRICE INTEGER,
                $COLUMN_QUANTITY INTEGER,
                $COLUMN_TRANSACTION_DATE TEXT,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USER($COLUMN_ID)
            )
        """.trimIndent()

        db.execSQL(createUserTable) // Membuat tabel user
        db.execSQL(createTransactionTable) // Membuat tabel transaction
    }

    // Fungsi ini dipanggil saat terjadi upgrade versi database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTION") // Hapus tabel transaction terlebih dahulu
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER") // Hapus tabel user
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

    // Function to add a new transaction
    fun addTransaction(userId: Int, filmTitle: String, filmPrice: Int, quantity: Int): Boolean {
        val db = writableDatabase

        // Get current date and time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_FILM_TITLE, filmTitle)
            put(COLUMN_FILM_PRICE, filmPrice)
            put(COLUMN_QUANTITY, quantity)
            put(COLUMN_TRANSACTION_DATE, currentDate)
        }

        return try {
            val result = db.insertOrThrow(TABLE_TRANSACTION, null, values)
            val success = result != -1L

            if (success) {
                android.util.Log.d("DatabaseHelper", "Transaction added: ID=$result")
            } else {
                android.util.Log.e("DatabaseHelper", "Failed to insert transaction")
            }

            success
        } catch (e: Exception) {
            android.util.Log.e("DatabaseHelper", "Exception adding transaction", e)
            Toast.makeText(ctx, "Database error: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // Function to get user ID by phone number
    fun getUserId(phone: String): Int? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COLUMN_ID),
            "$COLUMN_PHONE = ?",
            arrayOf(phone),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            cursor.close()
            userId
        } else {
            cursor.close()
            null
        }
    }

    // Function to get all transactions for a specific user
    fun getUserTransactions(userId: Int): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTION,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_TRANSACTION_DATE DESC"
        )

        while (cursor.moveToNext()) {
            val transaction = Transaction(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                filmTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILM_TITLE)),
                filmPrice = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FILM_PRICE)),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                transactionDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE))
            )
            transactions.add(transaction)
        }
        cursor.close()
        return transactions
    }

    // Function to get total spent by user
    fun getTotalSpentByUser(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_FILM_PRICE * $COLUMN_QUANTITY) as total FROM $TABLE_TRANSACTION WHERE $COLUMN_USER_ID = ?",
            arrayOf(userId.toString())
        )

        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }
}

// Transaction data class
data class Transaction(
    val id: Int,
    val userId: Int,
    val filmTitle: String,
    val filmPrice: Int,
    val quantity: Int,
    val transactionDate: String
) {
    fun getTotalPrice(): Int {
        return filmPrice * quantity
    }
}