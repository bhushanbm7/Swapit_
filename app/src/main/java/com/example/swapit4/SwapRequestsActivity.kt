// Update SwapRequestsActivity.kt
package com.example.swapit4

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SwapRequestsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SwapRequestAdapter
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap_requests)

        recyclerView = findViewById(R.id.swapRequestsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadSwapRequests()
    }

    private fun loadSwapRequests() {
        val currentUserId = auth.currentUser?.uid ?: return

        listenerRegistration = db.collection("swap_requests")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading requests: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SwapRequest::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                adapter = SwapRequestAdapter(requests, ::approveSwap, ::confirmSwap)
                recyclerView.adapter = adapter
            }
    }

    private fun approveSwap(request: SwapRequest) {
        val currentUserId = auth.currentUser?.uid ?: return

        val fieldToUpdate = if (currentUserId == request.requesterId) {
            "approvedByRequester"
        } else {
            "approvedByOwner"
        }

        db.collection("swap_requests").document(request.id)
            .update(fieldToUpdate, true)
            .addOnSuccessListener {
                Toast.makeText(this, "Approval recorded!", Toast.LENGTH_SHORT).show()
                checkIfBothApproved(request.id)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmSwap(request: SwapRequest) {
        // This is just an alias for approveSwap in this implementation
        approveSwap(request)
    }

    private fun checkIfBothApproved(requestId: String) {
        db.collection("swap_requests").document(requestId).get()
            .addOnSuccessListener { doc ->
                val request = doc.toObject(SwapRequest::class.java) ?: return@addOnSuccessListener

                if (request.approvedByRequester && request.approvedByOwner) {
                    completeSwap(request)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error checking approval: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun completeSwap(request: SwapRequest) {
        // Update status to completed
        db.collection("swap_requests").document(request.id)
            .update("status", "completed")
            .addOnSuccessListener {
                // Start SwapCompletedActivity to show the swapped items
                val intent = Intent(this, SwapCompletedActivity::class.java).apply {
                    putExtra("requesterItemId", request.requesterItemId)
                    putExtra("ownerItemId", request.ownerItemId)
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error completing swap: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}