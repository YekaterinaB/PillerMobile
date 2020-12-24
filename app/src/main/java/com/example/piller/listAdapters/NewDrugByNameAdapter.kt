package com.example.piller.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.models.Drug

class NewDrugByNameAdapter(
    private var dataSet: MutableList<Drug>,
    private val clickOnItemListener: (Int) -> Unit
) : RecyclerView.Adapter<NewDrugByNameAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val drugName: TextView = view.findViewById(R.id.nd_drug_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.drug_by_name_list_item, parent, false)

        return ViewHolder(view)
    }

    fun setData(data: MutableList<Drug>) {
        dataSet = data
    }

    override fun getItemCount(): Int = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val currentItem = dataSet[position]
        holder.drugName.text = currentItem.drug_name
        holder.itemView.setOnClickListener { clickOnItemListener(currentItem.rxcui) }
    }
}