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
import com.example.piller.utilities.DbConstants
import java.text.SimpleDateFormat
import java.util.*

class EliAdapter(
    private var _dataSet: MutableList<CalendarEvent>,
    private val _itemClickCallback: (CalendarEvent) -> Unit
) : RecyclerView.Adapter<EliAdapter.ViewHolder>() {
    private val _noBackground = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val drugName: TextView = view.findViewById(R.id.wdi_drug_name)
        val intakeTime: TextView = view.findViewById(R.id.wdi_intake_time)
        val takenStatus: ImageView = view.findViewById(R.id.wdi_taken_status)
        val layout: ConstraintLayout = view.findViewById(R.id.wdi_layout)
    }

    fun setData(data: MutableList<CalendarEvent>) {
        _dataSet = data
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.weekly_drug_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val currentItem = _dataSet[position]
        val drugObject = DrugMap.instance.getDrugObject(currentItem.calendarId, currentItem.drugId)
        viewHolder.drugName.text = drugObject.drugName
        val time = SimpleDateFormat(
            DbConstants.timeFormat,
            Locale.getDefault()
        ).format(currentItem.intakeTime)
        viewHolder.intakeTime.text = time
        viewHolder.layout.setOnClickListener { _itemClickCallback(currentItem) }
        setViewHolderBackgroundColor(viewHolder, currentItem)
    }

    private fun setViewHolderBackgroundColor(viewHolder: ViewHolder, calendarEvent: CalendarEvent) {
        //  set the background color only if the intake time passed
        //  the next line is in order to remove the icon, because if we delete the icon stays
        //  even though it's not supposed to be there
        viewHolder.takenStatus.setBackgroundResource(_noBackground)
        when {
            calendarEvent.isTaken -> {
                //  the medicine was taken - set green background (alpha is for opacity)
                viewHolder.takenStatus.setBackgroundResource(R.drawable.ic_check_green)
            }
            DateUtils.isDateAfter(Date(), calendarEvent.intakeTime) -> {
                //  the medicine wasn't taken - set red background (alpha is for opacity)
                viewHolder.takenStatus.setBackgroundResource(R.drawable.ic_exclamation_red)
            }
            else -> {
                //  the next line is in order to remove the icon, because if we delete the icon stays
                //  even though it's not supposed to be there
                viewHolder.takenStatus.setBackgroundResource(R.drawable.ic_basic_alarm)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = _dataSet.size
}
