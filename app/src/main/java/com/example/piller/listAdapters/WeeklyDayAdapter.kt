package com.example.piller.listAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.models.CalendarEvent
import com.example.piller.models.WeeklyDay

class WeeklyDayAdapter(
    private var dataSet: MutableList<WeeklyDay>,
    private var CalendarEventDataSet: Array<MutableList<CalendarEvent>>,
    private val itemClickCallback: (CalendarEvent) -> Unit
) : RecyclerView.Adapter<WeeklyDayAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayName: TextView = view.findViewById(R.id.wdc_day_name)
        val dayNumber: TextView = view.findViewById(R.id.wdc_day_number)
        val calendarEventsList: RecyclerView = view.findViewById(R.id.wdc_drugs_list)
    }

    fun setCalendarEventDataSet(data: Array<MutableList<CalendarEvent>>) {
        CalendarEventDataSet = data
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.weekly_daily_container, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataSet[position]
        holder.dayName.text = currentItem.dayName
        holder.dayNumber.text = currentItem.dayNumber.toString()
        holder.calendarEventsList.layoutManager = LinearLayoutManager(holder.dayName.context)
        val adapter = EliAdapter(CalendarEventDataSet[position], itemClickCallback)
        holder.calendarEventsList.adapter = adapter
    }

    override fun getItemCount() = dataSet.size

}