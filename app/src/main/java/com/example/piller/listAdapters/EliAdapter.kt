package com.example.piller.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.models.CalendarEvent
import java.text.SimpleDateFormat
import java.util.*

class EliAdapter(
    private var dataSet: MutableList<CalendarEvent>,
    private val itemClickCallback: (CalendarEvent) -> Unit
) :
    RecyclerView.Adapter<EliAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val drugName: TextView = view.findViewById(R.id.eli_drug_name)
        val intakeTime: TextView = view.findViewById(R.id.eli_time_intake)
    }

    fun setData(data: MutableList<CalendarEvent>) {
        dataSet = data
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.event_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val currentItem = dataSet[position]
        viewHolder.drugName.text = currentItem.drug_name
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentItem.intake_time)
        viewHolder.intakeTime.text = time
        viewHolder.drugName.setOnClickListener { itemClickCallback(currentItem) }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
