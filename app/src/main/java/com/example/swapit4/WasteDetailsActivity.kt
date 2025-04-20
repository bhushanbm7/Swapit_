package com.example.swapit4

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit4.databinding.ActivityWasteDetailsBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.firestore.FirebaseFirestore

class WasteDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWasteDetailsBinding
    private lateinit var firestore: FirebaseFirestore
    private var listingData: HashMap<String, Any> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWasteDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        listingData = intent.getSerializableExtra("listingData") as? HashMap<String, Any> ?: HashMap()


        setupDropdowns()
        setupButtons()
    }



    private fun setupDropdowns() {
        val regions = resources.getStringArray(R.array.regions)
        val regionAdapter = ArrayAdapter(this, R.layout.dropdown_item, regions)
        (binding.regionAutoComplete as MaterialAutoCompleteTextView).apply {
            setAdapter(regionAdapter)
            setOnClickListener { showDropDown() }
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDropDown() }
        }

        val availabilityOptions = resources.getStringArray(R.array.availability_options)
        val availabilityAdapter = ArrayAdapter(this, R.layout.dropdown_item, availabilityOptions)
        (binding.availabilityAutoComplete as MaterialAutoCompleteTextView).apply {
            setAdapter(availabilityAdapter)
            setOnClickListener { showDropDown() }
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDropDown() }
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.submitDetailsButton.setOnClickListener {
            submitCompleteListing()
        }
    }

    private fun submitCompleteListing() {
        val detailedDescription = binding.detailedDescriptionEditText.text.toString().trim()
        val region = binding.regionAutoComplete.text.toString().trim()
        val availability = binding.availabilityAutoComplete.text.toString().trim()
        val potentialUses = binding.potentialUsesEditText.text.toString().trim()

        if (detailedDescription.isEmpty() || region.isEmpty() || availability.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Add new fields
        listingData["detailedDescription"] = detailedDescription
        listingData["region"] = region
        listingData["availability"] = availability
        listingData["potentialUses"] = potentialUses

        // Save or update the listing (you can use .set if updating)
        firestore.collection("waste_listings")
            .add(listingData)
            .addOnSuccessListener {
                val intent = Intent(this, SubmissionSuccessActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error submitting listing: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
