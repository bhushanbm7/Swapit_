package com.example.swapit4

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit4.databinding.ActivityAddItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmitItem.setOnClickListener {
            val wasteType = binding.etWasteType.text.toString().trim()
            val quantityStr = binding.etQuantity.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (wasteType.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityStr.toIntOrNull()
            if (quantity == null || quantity <= 0) {
                Toast.makeText(this, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val itemData = hashMapOf(
                "wasteType" to wasteType,
                "quantity" to quantity,
                "description" to description,
                "status" to "available",  // ✅ Status for swapping
                "userId" to userId
            )

            firestore.collection("waste_listings")
                .add(itemData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Item added for swap!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
