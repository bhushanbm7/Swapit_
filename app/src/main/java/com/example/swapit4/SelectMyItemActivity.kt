package com.example.swapit4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit4.databinding.ActivitySelectMyItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SelectMyItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectMyItemBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var targetListing: WasteListing
    private var selectedMyItemId: String? = null
    private var listingId: String? = null
    private var myItems: MutableList<WasteListing> = mutableListOf()
    private var loadingAttempts = 0
    private val MAX_LOADING_ATTEMPTS = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectMyItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        listingId = intent.getStringExtra("listingId") ?: run {
            Toast.makeText(this, "No listing ID found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadListingForSwap(listingId!!)
        loadMyAvailableItems()

        binding.swapConfirmButton.setOnClickListener {
            if (selectedMyItemId != null) {
                sendSwapRequest()
            } else {
                Toast.makeText(this, "Please select an item to swap", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddNewItem.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadMyAvailableItems()
    }

    private fun loadListingForSwap(listingId: String) {
        firestore.collection("waste_listings").document(listingId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    targetListing = document.toObject(WasteListing::class.java)?.apply {
                        id = document.id
                    } ?: run {
                        Toast.makeText(this, "Invalid listing data", Toast.LENGTH_SHORT).show()
                        finish()
                        return@addOnSuccessListener
                    }

                    binding.itemNameTextView.text = "Item: ${targetListing.wasteType}"
                    binding.itemDescriptionTextView.text = "Description: ${targetListing.description ?: "No Description"}"
                } else {
                    Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading listing", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun loadMyAvailableItems() {
        val currentUserId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (loadingAttempts >= MAX_LOADING_ATTEMPTS) {
            Toast.makeText(this, "Failed to load items after multiple attempts", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadingAttempts++

        firestore.collection("waste_listings")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("status", "available")
            .get()
            .addOnSuccessListener { documents ->
                myItems.clear()
                myItems.addAll(documents.mapNotNull { doc ->
                    doc.toObject(WasteListing::class.java)?.apply {
                        id = doc.id
                    }
                })

                if (myItems.isEmpty()) {
                    Toast.makeText(this, "You have no available items to swap", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                Log.d("SelectMyItem", "Loaded ${myItems.size} items")
                setupSpinnerAdapter()
            }
            .addOnFailureListener { e ->
                Log.e("SelectMyItemActivity", "Error loading items: ${e.message}")
                if (loadingAttempts < MAX_LOADING_ATTEMPTS) {
                    loadMyAvailableItems() // Retry
                } else {
                    Toast.makeText(this, "Error loading your items", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    private fun setupSpinnerAdapter() {
        val itemDisplayStrings = myItems.map {
            "${it.wasteType} (${it.quantity} kg)"
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            itemDisplayStrings
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.myItemsSpinner.adapter = adapter

        binding.myItemsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedMyItemId = myItems[position].id
                binding.selectedItemTextView.text = "Selected: ${myItems[position].wasteType}"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedMyItemId = null
                binding.selectedItemTextView.text = "No item selected"
            }
        }

        if (myItems.isNotEmpty()) {
            binding.myItemsSpinner.setSelection(0)
        }
    }

    private fun sendSwapRequest() {
        val currentUserId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val ownerId = targetListing.userId ?: run {
            Toast.makeText(this, "Owner ID not found for target item", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedItem = myItems.find { it.id == selectedMyItemId } ?: run {
            Toast.makeText(this, "Invalid item selection", Toast.LENGTH_SHORT).show()
            return
        }

        val swapRequest = hashMapOf(
            "requesterId" to currentUserId,
            "requesterItemId" to selectedMyItemId,
            "ownerId" to ownerId,
            "ownerItemId" to listingId,
            "status" to "pending",
            "timestamp" to Date(),
            "requesterItemName" to selectedItem.wasteType,
            "ownerItemName" to targetListing.wasteType,
            "requesterItemQuantity" to selectedItem.quantity,
            "ownerItemQuantity" to targetListing.quantity
        )

        firestore.collection("swap_requests")
            .add(swapRequest)
            .addOnSuccessListener { documentReference ->
                updateItemStatus(selectedMyItemId!!, "pending_swap") { success1 ->
                    updateItemStatus(listingId!!, "pending_swap") { success2 ->
                        if (success1 && success2) {
                            // Navigate to SwapCompletedActivity with both item IDs
                            Intent(this@SelectMyItemActivity, SwapCompletedActivity::class.java).apply {
                                putExtra("requesterItemId", selectedMyItemId)
                                putExtra("ownerItemId", listingId)
                                putExtra("swapRequestId", documentReference.id)
                                startActivity(this)
                                finish()
                            }
                            Toast.makeText(this@SelectMyItemActivity, "Swap request sent!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                this@SelectMyItemActivity,
                                "Swap sent but status update failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateItemStatus(itemId: String, status: String, callback: (Boolean) -> Unit) {
        firestore.collection("waste_listings").document(itemId)
            .update("status", status)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                Log.e("SelectMyItemActivity", "Failed to update status for item $itemId")
                callback(false)
            }
    }
}