package com.example.swapit4

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit4.databinding.ActivityListingDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore

class ListingDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListingDetailsBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Get the listing ID passed from the previous activity
        val listingId = intent.getStringExtra("listingId")

        if (listingId != null) {
            loadListingDetails(listingId)
        } else {
            Toast.makeText(this, "No listing ID found", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set up the button to navigate to the next activity
        binding.swapItButton.setOnClickListener {
            Toast.makeText(this, "Swap Request Sent!", Toast.LENGTH_SHORT).show()
            // Passing necessary data for the swap request
            val intent = Intent(this, SelectMyItemActivity::class.java).apply {
                putExtra("listingId", listingId) // Pass listing ID or any necessary data for the swap
            }
            startActivity(intent)
        }
    }

    private fun loadListingDetails(listingId: String) {
        firestore.collection("waste_listings").document(listingId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val listing = document.toObject(WasteListing::class.java)
                    listing?.let {
                        binding.typeTextView.text = "Type: ${it.wasteType}"
                        binding.regionTextView.text = "Region: ${it.region}"
                        binding.quantityTextView.text = "Quantity: ${it.quantity}"
                        binding.descriptionTextView.text = "Description: ${it.description ?: "No Description"}"
                    }
                } else {
                    Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading listing", Toast.LENGTH_SHORT).show()
            }
    }
}
