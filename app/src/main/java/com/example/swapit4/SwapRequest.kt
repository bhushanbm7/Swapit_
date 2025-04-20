// SwapRequest.kt
package com.example.swapit4

data class SwapRequest(
    val id: String = "",
    val requesterId: String = "",
    val requesterItemId: String = "",
    val ownerId: String = "",
    val ownerItemId: String = "",
    val approvedByRequester: Boolean = false,
    val approvedByOwner: Boolean = false,
    val status: String = "pending",
    val participants: List<String> = emptyList() // Add this field
)