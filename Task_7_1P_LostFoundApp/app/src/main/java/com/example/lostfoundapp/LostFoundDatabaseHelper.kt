package com.example.lostfoundapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Database Helper class managing local SQLite operations.
 * It handles creating the database, creating the adverts table,
 * and performing CRUD (Create, Read, Update, Delete) operations.
 *
 * Beginner-friendly explanation:
 * SQLiteOpenHelper is a built-in Android utility that manages database creation
 * and version management. We implement onCreate to define our table schema
 * and write standard Kotlin functions to interact with the database.
 */
class LostFoundDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val TAG = "LostFoundDbHelper"
        private const val DATABASE_NAME = "LostFoundDatabase.db"
        private const val DATABASE_VERSION = 1

        // Table Name
        const val TABLE_NAME = "adverts"

        // Column Names
        const val COLUMN_ID = "id"
        const val COLUMN_POST_TYPE = "postType"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_DATE = "date"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IMAGE_URI = "imageUri"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    /**
     * Called when the database is created for the first time.
     * We run an SQL statement here to create our 'adverts' table.
     */
    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_POST_TYPE TEXT NOT NULL,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_LOCATION TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_IMAGE_URI TEXT NOT NULL,
                $COLUMN_TIMESTAMP TEXT NOT NULL
            )
        """.trimIndent()

        try {
            db.execSQL(createTableQuery)
            Log.d(TAG, "Table '$TABLE_NAME' created successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating table '$TABLE_NAME'", e)
        }
    }

    /**
     * Called when the database needs to be upgraded (e.g., version number changes).
     * For academic task simplicity, we drop the existing table and recreate it.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
        Log.d(TAG, "Database upgraded from version $oldVersion to $newVersion.")
    }

    /**
     * Inserts a new Lost/Found advert into the SQLite database.
     * @param item The LostFoundItem data object to insert.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    fun insertAdvert(item: LostFoundItem): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_POST_TYPE, item.postType)
            put(COLUMN_NAME, item.name)
            put(COLUMN_PHONE, item.phone)
            put(COLUMN_DESCRIPTION, item.description)
            put(COLUMN_DATE, item.date)
            put(COLUMN_LOCATION, item.location)
            put(COLUMN_CATEGORY, item.category)
            put(COLUMN_IMAGE_URI, item.imageUri)
            put(COLUMN_TIMESTAMP, item.timestamp)
        }

        val result = db.insert(TABLE_NAME, null, values)
        db.close() // Good practice to close database connections
        return result
    }

    /**
     * Retrieves all saved adverts from the database, ordered by latest timestamp first.
     */
    fun getAllAdverts(): List<LostFoundItem> {
        val list = mutableListOf<LostFoundItem>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val item = LostFoundItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    postType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_TYPE)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)),
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                )
                list.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    /**
     * Retrieves a single advert from the database by its unique database ID.
     */
    fun getAdvertById(id: Int): LostFoundItem? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        var item: LostFoundItem? = null
        if (cursor.moveToFirst()) {
            item = LostFoundItem(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                postType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_TYPE)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)),
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            )
        }
        cursor.close()
        db.close()
        return item
    }

    /**
     * Deletes a specific advert from the database by its ID.
     * @return true if deletion was successful (deleted 1 or more rows), false otherwise.
     */
    fun deleteAdvert(id: Int): Boolean {
        val db = this.writableDatabase
        val deletedRows = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return deletedRows > 0
    }
}
