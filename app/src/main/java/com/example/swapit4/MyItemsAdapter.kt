package com.example.swapit4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyItemsAdapter(
    private val items: List<WasteListing>,
    private val onItemSelected: (WasteListing) -> Unit
) : RecyclerView.Adapter<MyItemsAdapter.ViewHolder>() {

    private var selectedPosition = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemNameTextView)
        val itemDescription: TextView = itemView.findViewById(R.id.itemDescriptionTextView)

        init {
            itemView.setOnClickListener {
                selectedPosition = adapterPosition
                onItemSelected(items[selectedPosition])
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_listing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.wasteType
        holder.itemDescription.text = item.description ?: "No description"

        // Highlight the selected item
        holder.itemView.setBackgroundColor(
            if (selectedPosition == position)
                holder.itemView.context.getColor(android.R.color.darker_gray)
            else
                holder.itemView.context.getColor(android.R.color.transparent)
        )
    }

    override fun getItemCount() = items.size

    // ✅ Return selected item
    fun getSelectedItem(): WasteListing? {
        return if (selectedPosition != -1) items[selectedPosition] else null
    }

    // Optional: Get only name
    fun getSelectedItemName(): String? {
        return getSelectedItem()?.wasteType
    }

    // Optional: Reset selection
    fun clearSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }
}
