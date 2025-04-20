package com.example.swapit4

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapit4.databinding.ActivityMarketplaceBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MarketplaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMarketplaceBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: WasteListingAdapter
    private val listings = mutableListOf<WasteListing>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarketplaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        setupFilterControls()
        setupRecyclerView()
        loadListings()
    }

    private fun setupFilterControls() {
        val wasteTypes = resources.getStringArray(R.array.waste_types)
        val wasteTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, wasteTypes)
        wasteTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.wasteTypeSpinner.adapter = wasteTypeAdapter

        val regions = resources.getStringArray(R.array.regions)
        val regionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, regions)
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.regionSpinner.adapter = regionAdapter

        binding.filterButton.setOnClickListener {
            applyFilters()
        }
    }

    private fun setupRecyclerView() {
        adapter = WasteListingAdapter(
            listings,
            onItemClick = { listing ->
                val intent = Intent(this, ListingDetailsActivity::class.java).apply {
                    putExtra("listingId", listing.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { listing, position ->
                firestore.collection("waste_listings")
                    .document(listing.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Listing deleted successfully", Toast.LENGTH_SHORT).show()
                        adapter.removeItem(position)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to delete listing: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        )

        binding.marketplaceRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.marketplaceRecyclerView.adapter = adapter
    }

    private fun loadListings() {
        firestore.collection("waste_listings")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                listings.clear()
                for (document in documents) {
                    val listing = document.toObject(WasteListing::class.java).copy(id = document.id)
                    listings.add(listing)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading listings: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyFilters() {
        val selectedType = binding.wasteTypeSpinner.selectedItem.toString()
        val selectedRegion = binding.regionSpinner.selectedItem.toString()
        val minQuantity = binding.minQuantityEditText.text.toString().toIntOrNull() ?: 0
        val maxQuantity = binding.maxQuantityEditText.text.toString().toIntOrNull() ?: Int.MAX_VALUE

        firestore.collection("waste_listings")
            .whereEqualTo("wasteType", selectedType)
            .whereEqualTo("region", selectedRegion)
            .whereGreaterThanOrEqualTo("quantity", minQuantity)
            .whereLessThanOrEqualTo("quantity", maxQuantity)
            .get()
            .addOnSuccessListener { documents ->
                listings.clear()
                for (document in documents) {
                    val listing = document.toObject(WasteListing::class.java).copy(id = document.id)
                    listings.add(listing)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error filtering listings: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
