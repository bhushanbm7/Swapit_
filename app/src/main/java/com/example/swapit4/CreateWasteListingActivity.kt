package com.example.swapit4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit4.databinding.ActivityCreateWasteListingBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class CreateWasteListingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateWasteListingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateWasteListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Setup dropdown with waste types
        setupWasteTypeDropdown()

        // Submit listing
        binding.submitButton.setOnClickListener {
            submitWasteListing()
        }

        // View Marketplace button
        binding.viewMarketplaceButton.setOnClickListener {
            val intent = Intent(this, MarketplaceActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupWasteTypeDropdown() {
        val wasteTypes = resources.getStringArray(R.array.waste_types)
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, wasteTypes)

        (binding.wasteTypeAutoComplete as MaterialAutoCompleteTextView).apply {
            setAdapter(adapter)
            setOnClickListener { showDropDown() }
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDropDown() }
        }
    }

    private fun submitWasteListing() {
        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val wasteType = binding.wasteTypeAutoComplete.text.toString().trim()
        val quantityStr = binding.quantityEditText.text.toString().trim()
        val priceStr = binding.priceEditText.text.toString().trim()

        val currentUser = auth.currentUser
        if (title.isEmpty() || description.isEmpty() || wasteType.isEmpty() ||
            quantityStr.isEmpty() || priceStr.isEmpty() || currentUser == null
        ) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = try {
            quantityStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val price = try {
            priceStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
            return
        }

        val totalValue = quantity * price
        val df = DecimalFormat("#.##")
        val formattedTotalValue = df.format(totalValue)

        val listing = hashMapOf(
            "title" to title,
            "description" to description,
            "wasteType" to wasteType,
            "quantity" to quantity,
            "pricePerKg" to price,
            "totalValue" to formattedTotalValue,
            "userId" to currentUser.uid,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("waste_listings")
            .add(listing)
            .addOnSuccessListener {
                val intent = Intent(this, WasteDetailsActivity::class.java).apply {
                    putExtra("listingData", listing)
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error adding document", e)
                Toast.makeText(this, "Failed to create listing: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
