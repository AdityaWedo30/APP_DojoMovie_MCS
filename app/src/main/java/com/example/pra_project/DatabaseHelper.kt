package com.example.dojomovie

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.pra_project.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.example.pra_project.Transaction

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
        private const val COLUMN_FILM_ID = "films_id"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_TRANSACTION_DATE = "transaction_date"

        // films table
        private const val TABLE_FILMS = "films"
        private const val COLUMN_FILMS_ID = "id"
        private const val COLUMN_FILMS_IMAGE = "image"
        private const val COLUMN_FILMS_TITLE = "title"
        private const val COLUMN_FILMS_PRICE = "price"

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
                $COLUMN_FILM_ID TEXT,
                $COLUMN_QUANTITY INTEGER,
                $COLUMN_TRANSACTION_DATE TEXT,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USER($COLUMN_ID),
                FOREIGN KEY($COLUMN_FILM_ID) REFERENCES $TABLE_FILMS($COLUMN_FILMS_ID)
            )
        """.trimIndent()

        // Create Films table
        val createFilmsTable = """
            CREATE TABLE $TABLE_FILMS (
                $COLUMN_FILMS_ID TEXT PRIMARY KEY,
                $COLUMN_FILMS_IMAGE TEXT,
                $COLUMN_FILMS_TITLE TEXT,
                $COLUMN_FILMS_PRICE INT
            )
        """.trimIndent()

        db.execSQL(createUserTable) // Membuat tabel user
        db.execSQL(createTransactionTable) // Membuat tabel transaction
        db.execSQL(createFilmsTable)     // Membuat tabel films
    }

    // Fungsi ini dipanggil saat terjadi upgrade versi database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTION") // Hapus tabel transaction terlebih dahulu
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER") // Hapus tabel user
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FILMS") // Hapus tabel films
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
    fun addTransaction(userId: Int, filmId: String, quantity: Int): Boolean {
        val db = writableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_FILM_ID, filmId)
            put(COLUMN_QUANTITY, quantity)
            put(COLUMN_TRANSACTION_DATE, currentDate)
        }

        return try {
            val result = db.insertOrThrow(TABLE_TRANSACTION, null, values)
            result != -1L
        } catch (e: Exception) {
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
//    fun getUserTransactions(userId: Int): List<Transaction> {
//        val transactions = mutableListOf<Transaction>()
//        val db = readableDatabase
//
//        val query = """
//        SELECT t.$COLUMN_TRANSACTION_ID, t.$COLUMN_USER_ID,
//               f.$COLUMN_FILMS_TITLE, f.$COLUMN_FILMS_PRICE,
//               t.$COLUMN_QUANTITY, t.$COLUMN_TRANSACTION_DATE
//        FROM $TABLE_TRANSACTION t
//        JOIN $TABLE_FILMS f ON t.$COLUMN_FILM_ID = f.$COLUMN_FILM_ID
//        WHERE t.$COLUMN_USER_ID = ?
//        ORDER BY t.$COLUMN_TRANSACTION_DATE DESC
//        """
//
//        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
//
//        while (cursor.moveToNext()) {
//            val transaction = Transaction(
//                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
//                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
//                filmTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILMS_TITLE)),
//                filmPrice = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FILMS_PRICE)),
//                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
//                transactionDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE))
//            )
//            transactions.add(transaction)
//        }
//        cursor.close()
//        return transactions
//    }
//    fun getUserTransactions(userId: Int): List<Transaction> {
//        val transactions = mutableListOf<Transaction>()
//        val db = readableDatabase
//
//        val cursor = db.query(
//            TABLE_TRANSACTION,
//            null,
//            "$COLUMN_USER_ID = ?",
//            arrayOf(userId.toString()),
//            null,
//            null,
//            "$COLUMN_TRANSACTION_DATE DESC"
//        )
//
//        cursor.use {
//            while (it.moveToNext()) {
//                val transaction = com.example.pra_project.Transaction(
//                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
//                    userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID)),
//                    filmId = it.getString(it.getColumnIndexOrThrow(COLUMN_FILM_ID)),
//                    quantity = it.getInt(it.getColumnIndexOrThrow(COLUMN_QUANTITY)),
//                    transactionDate = it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE))
//                )
//                transactions.add(transaction)
//            }
//        }
//
//        return transactions
//    }

    fun getUserTransactions(userId: Int): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT t.quantity, t.transaction_date, f.title, f.price
        FROM transactions t
        JOIN films f ON t.films_id = f.id
        WHERE t.user_id = ?
        """, arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                val transactionDate = cursor.getString(cursor.getColumnIndexOrThrow("transaction_date"))
                val filmTitle = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val filmPrice = cursor.getInt(cursor.getColumnIndexOrThrow("price"))

                transactions.add(Transaction(filmTitle, filmPrice, quantity, transactionDate))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }



    // Function to get total spent by user
    fun getTotalSpentByUser(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_FILMS_PRICE * $COLUMN_QUANTITY) as total FROM $TABLE_TRANSACTION WHERE $COLUMN_USER_ID = ?",
            arrayOf(userId.toString())
        )

        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }
    // Menambahkan film baru ke tabel films
    fun addFilm(film: Film): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FILMS_ID, film.id)
            put(COLUMN_FILMS_IMAGE, film.image)
            put(COLUMN_FILMS_TITLE, film.title)
            put(COLUMN_FILMS_PRICE, film.price)
        }

        return try {
            val result = db.insertOrThrow(TABLE_FILMS, null, values)
            result != -1L
        } catch (e: Exception) {
            Toast.makeText(ctx, "Gagal menambahkan film: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // Mengambil semua film
    fun getAllFilms(): List<Film> {
        val filmList = mutableListOf<Film>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM films", null)
        if (cursor.moveToFirst()) {
            do {
                val film = Film(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    image = cursor.getString(cursor.getColumnIndexOrThrow("image")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    price = cursor.getInt(cursor.getColumnIndexOrThrow("price"))
                )
                filmList.add(film)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return filmList
    }

    fun updateFilmImage(filmId: String, newImage: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FILMS_IMAGE, newImage)
        }

        val rowsUpdated = db.update(
            TABLE_FILMS,
            values,
            "$COLUMN_FILMS_ID = ?",
            arrayOf(filmId)
        )

        return rowsUpdated > 0 // true jika ada yang ter-update
    }

    fun getFilmById(id: String): Film? {
        val db = this.readableDatabase
        val cursor = db.query(
            "films",
            arrayOf("id", "title", "price", "image"),
            "id = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        var film: Film? = null
        if (cursor.moveToFirst()) {
            val filmId = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val price = cursor.getInt(cursor.getColumnIndexOrThrow("price"))
            val image = cursor.getString(cursor.getColumnIndexOrThrow("image"))
            film = Film(filmId, image, title, price)
        }

        cursor.close()
        db.close()
        return film
    }


}

fun fetchAndStoreFilms(context: Context) {
    val dbHelper = DatabaseHelper(context)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val films = ApiClient.filmService.getFilms()
            for (film in films) {
                dbHelper.addFilm(film)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Berhasil menambahkan film dari API", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Gagal memuat data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}





// Transaction data class
data class Transaction(
    val filmTitle: String,
    val filmPrice: Int,
    val quantity: Int,
    val transactionDate: String
){
    fun getTotalPrice(): Int {
        return filmPrice * quantity
    }
}

data class Film(
    val id: String,
    val image: String,
    val title: String,
    val price: Int
)
