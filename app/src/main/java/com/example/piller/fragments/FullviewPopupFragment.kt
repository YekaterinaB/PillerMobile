package com.example.piller.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.activities.DrugInfoActivity
import com.example.piller.listAdapters.EliAdapter
import com.example.piller.models.CalendarEvent
import com.example.piller.utilities.DbConstants

class FullviewPopupFragment : DialogFragment() {
    private var dateString: String? = null
    private var eventsData: Array<CalendarEvent>? = null
    private lateinit var dateTV: TextView
    private lateinit var eventsList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //  set round corners
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner)
        val fragmentView = inflater.inflate(R.layout.fullview_day_popup, container, false)
        initViews(fragmentView)
        return fragmentView
    }

    private fun initViews(fragment: View) {
        dateTV = fragment.findViewById(R.id.fvp_date_tv)
        dateTV.text = dateString

        eventsList = fragment.findViewById(R.id.fvp_events_list)
        eventsList.layoutManager = LinearLayoutManager(fragment.context)
        eventsList.adapter = eventsData?.toMutableList()
            ?.let { EliAdapter(it) { calendarEvent -> showDrugInfo(calendarEvent) } }
    }

    private fun showDrugInfo(calendarEvent: CalendarEvent) {
        val intent = Intent(
            requireContext(),
            DrugInfoActivity::class.java
        )
        intent.putExtra(
            DbConstants.CALENDAR_EVENT,
            calendarEvent
        )
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dateString = it.getString(ARG_DATE_STRING)
            eventsData =
                it.getParcelableArray(ARG_EVENTS_LIST)?.map { list -> list as CalendarEvent }
                    ?.toTypedArray()
        }
    }

    override fun onStart() {
        super.onStart()
        //  set size
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        val ARG_DATE_STRING = "dateString"
        val ARG_EVENTS_LIST = "eventsData"

        @JvmStatic
        fun newInstance() =
            FullviewPopupFragment().apply {
                arguments = Bundle().apply {
//                    putString(ARG_DATE_STRING, dateString)
//                    eventsData =
//                        this.getParcelableArray(ARG_EVENTS_LIST)
//                            ?.map { list -> list as CalendarEvent }
//                            ?.toTypedArray()
                }
            }
    }
}