package com.example.piller.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.DrugMap
import com.example.piller.R
import com.example.piller.models.CalendarEvent
import com.example.piller.utilities.DateUtils
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
        val takenStatus: ImageView = view.findViewById(R.id.eli_drug_taken)
        val layout: ConstraintLayout = view.findViewById(R.id.eli_layout)
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
        val drugObject = DrugMap.instance.getDrugObject(currentItem.calendarId, currentItem.drugId)
        viewHolder.drugName.text = drugObject.drugName
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentItem.intakeTime)
        viewHolder.intakeTime.text = time
        viewHolder.layout.setOnClickListener { itemClickCallback(currentItem) }
        setViewHolderBackgroundColor(viewHolder, currentItem)
    }

    private fun setViewHolderBackgroundColor(viewHolder: ViewHolder, calendarEvent: CalendarEvent) {
        //  set the background color only if the intake time passed
        if (DateUtils.isDateAfter(Date(), calendarEvent.intakeTime)) {
            if (calendarEvent.isTaken) {
                //  the medicine was taken - set green background (alpha is for opacity)
                viewHolder.takenStatus.setBackgroundResource(R.drawable.ic_check_green)
            } else {
                //  the medicine wasn't taken - set red background (alpha is for opacity)
                viewHolder.takenStatus.setBackgroundResource(R.drawable.ic_exclamation_red)
            }
        } else {
            //  the next line is in order to remove the icon, because if we delete the icon stays
            //  even though it's not supposed to be there
            viewHolder.takenStatus.setBackgroundResource(0)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}
