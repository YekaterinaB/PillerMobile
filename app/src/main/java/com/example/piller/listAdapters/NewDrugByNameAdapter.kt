package com.example.piller.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.models.DrugObject

class NewDrugByNameAdapter(
    private var _dataSet: MutableList<DrugObject>,
    private val _clickOnItemListener: (Int) -> Unit
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

    fun setData(data: MutableList<DrugObject>) {
        _dataSet = data
    }

    override fun getItemCount(): Int = _dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val currentItem = _dataSet[position]
        holder.drugName.text = currentItem.drugName
        holder.itemView.setOnClickListener { _clickOnItemListener(currentItem.rxcui) }
    }
}