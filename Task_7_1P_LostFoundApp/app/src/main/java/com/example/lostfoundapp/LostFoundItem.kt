package com.example.lostfoundapp

/**
 * Data class representing a Lost or Found advert item.
 * This class maps directly to the database records and is used to pass
 * data between the UI screens and the SQLite Database Helper.
 */
data class LostFoundItem(
    val id: Int = 0,               // Auto-incremented ID, defaults to 0 for new items
    val postType: String,          // "LOST" or "FOUND"
    val name: String,              // Name of the reporter
    val phone: String,             // Contact phone number
    val description: String,       // Details/description of the item
    val date: String,              // Date the item was lost/found (entered by user)
    val location: String,          // Location where it was lost/found
    val category: String,          // Category (e.g., Electronics, Pets, Wallets, Keys, etc.)
    val imageUri: String,          // Local Uri string pointing to the selected image
    val timestamp: String          // Automatic system date/time when it was created
)
