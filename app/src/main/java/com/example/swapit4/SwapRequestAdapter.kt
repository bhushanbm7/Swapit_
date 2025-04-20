package com.example.swapit4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SwapRequestAdapter(
    private val swapRequests: List<SwapRequest>,
    private val onApproveClick: (SwapRequest) -> Unit,
    private val onConfirmClick: (SwapRequest) -> Unit
) : RecyclerView.Adapter<SwapRequestAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val requestInfo: TextView = itemView.findViewById(R.id.requestInfo)
        val approveBtn: Button = itemView.findViewById(R.id.approveBtn)
        val confirmBtn: Button = itemView.findViewById(R.id.confirmSwapBtn)

        fun bind(request: SwapRequest) {
            requestInfo.text = "Requester Item: ${request.requesterItemId}\nOwner Item: ${request.ownerItemId}\nStatus: ${request.status}"
            approveBtn.setOnClickListener { onApproveClick(request) }
            confirmBtn.setOnClickListener { onConfirmClick(request) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_swap_request, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = swapRequests.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(swapRequests[position])
    }
}
