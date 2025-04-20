package com.example.swapit4

data class WasteListing(
    var id: String = "",             // Unique identifier for the listing
    val title: String = "",          // Title of the item (e.g., "Used Plastic Bottles")
    val wasteType: String = "",      // Type of waste (e.g., "Plastic", "Paper", "Metal")
    val quantity: Int = 0,           // Quantity of waste (e.g., in kg or pieces)
    val location: String = "",       // Location where the waste is available
    val pricePerKg: Double = 0.0,    // Price per kg of the waste item
    val status: String = " ", // Status of the item: "Available" or "Pending"
    val userId: String = "",         // User ID of the person who listed the waste item
    val timestamp: Long = 0L,        // Timestamp of when the listing was created
    val description: String? = null,  // Description of the waste item, optional
    val region: String = "",         // Region or city where the waste is located
    val ownerId: String = ""         // Owner's ID (can be the same as userId)
)
