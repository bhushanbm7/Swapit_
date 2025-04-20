package com.example.swapit4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WasteListingAdapter(
    private val listings: MutableList<WasteListing>,
    private val onItemClick: (WasteListing) -> Unit,
    private val onDeleteClick: (WasteListing, Int) -> Unit
) : RecyclerView.Adapter<WasteListingAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        private val regionTextView: TextView = itemView.findViewById(R.id.regionTextView)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        private val viewDetailsButton: Button = itemView.findViewById(R.id.viewDetailsButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(listing: WasteListing, position: Int) {
            typeTextView.text = "Type: ${listing.wasteType}"
            regionTextView.text = "Region: ${listing.region}"
            quantityTextView.text = "Qty: ${listing.quantity}"

            viewDetailsButton.setOnClickListener {
                onItemClick(listing)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(listing, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_waste_listing, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listings.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listings[position], position)
    }

    fun removeItem(position: Int) {
        listings.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listings.size)
    }
}
