package com.example.swapit4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class SwapCompletedActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap_completed)

        val requesterItemId = intent.getStringExtra("requesterItemId") ?: ""
        val ownerItemId = intent.getStringExtra("ownerItemId") ?: ""

        val requesterItemView = findViewById<TextView>(R.id.requesterItemView)
        val ownerItemView = findViewById<TextView>(R.id.ownerItemView)
        val marketplaceButton = findViewById<Button>(R.id.marketplaceButton)

        // Load and display requester's item
        db.collection("waste_listings").document(requesterItemId).get()
            .addOnSuccessListener { doc ->
                val item = doc.toObject(WasteListing::class.java)
                requesterItemView.text = "${item?.wasteType}\n${item?.description ?: "No Description"}"
            }

        // Load and display owner's item
        db.collection("waste_listings").document(ownerItemId).get()
            .addOnSuccessListener { doc ->
                val item = doc.toObject(WasteListing::class.java)
                ownerItemView.text = "${item?.wasteType}\n${item?.description ?: "No Description"}"
            }

        // Handle marketplace button click
        marketplaceButton.setOnClickListener {
            val intent = Intent(this, MarketplaceActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }
}