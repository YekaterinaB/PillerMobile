package com.example.piller.listAdapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R

class DosageAdapter(
    private var dataSet: Array<String>,
    private var measurementType: String,
    private val itemClickCallback: (String) -> Unit
) :
    RecyclerView.Adapter<DosageAdapter.ViewHolder>() {
    private var selectedPos = 0
    private var isInInit: Boolean = true

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.dosage_item_name)
        var layout: ConstraintLayout = view.findViewById(R.id.dosage_item_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dosage_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataSet[position]
        //  set first item as selected in first run
        if (!isInInit) {
            holder.itemView.setBackgroundColor(if (selectedPos == position) Color.GREEN else Color.TRANSPARENT)
        } else {
            if (currentItem == measurementType) {
                itemClickCallback(currentItem)
                isInInit = false
                holder.itemView.setBackgroundColor(Color.GREEN)
                selectedPos = position
            }
        }

        holder.name.text = currentItem
        holder.layout.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(selectedPos)
                selectedPos = holder.adapterPosition
                notifyItemChanged(selectedPos)
            }
            itemClickCallback(currentItem)
        }
    }

    override fun getItemCount() = dataSet.size
}