package com.example.piller.fragments

import android.app.Activity
import android.content.DialogInterface
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
    private var eventsData = mutableListOf<CalendarEvent>()
    private lateinit var dateTV: TextView
    private lateinit var eventsList: RecyclerView
    private lateinit var loggedEmail: String
    private lateinit var loggedName: String
    private val drugsToDelete = mutableListOf<String>()
    private lateinit var drugToDelete: String
    private val DRUG_INFO_DELETE_CODE = 1

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
        setEventsData()
    }

    private fun setEventsData() {
        eventsList.adapter =
            EliAdapter(eventsData.toMutableList()) { calendarEvent -> showDrugInfo(calendarEvent) }
        eventsList.adapter?.notifyDataSetChanged()
    }

    private fun showDrugInfo(calendarEvent: CalendarEvent) {
        drugToDelete = calendarEvent.drug_rxcui
        val intent = Intent(requireContext(), DrugInfoActivity::class.java)
        intent.putExtra(DbConstants.CALENDAR_EVENT, calendarEvent)
        intent.putExtra(DbConstants.LOGGED_USER_EMAIL, loggedEmail)
        intent.putExtra(DbConstants.LOGGED_USER_NAME, loggedName)
        startActivityForResult(intent, DRUG_INFO_DELETE_CODE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dateString = it.getString(ARG_DATE_STRING)
            val a =
                it.getParcelableArray(ARG_EVENTS_LIST)?.map { list -> list as CalendarEvent }
                    ?.toTypedArray()
            if (a != null) {
                eventsData = a.toMutableList()
            }

            loggedEmail = it.getString(DbConstants.LOGGED_USER_EMAIL).toString()
            loggedName = it.getString(DbConstants.LOGGED_USER_NAME).toString()
        }
    }

    override fun onStart() {
        super.onStart()
        //  set window size
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            DRUG_INFO_DELETE_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    drugsToDelete.add(drugToDelete)
                    removeDrugFromList()
                    setEventsData()
                }
            }
        }
    }

    private fun removeDrugFromList() {
        for (index in eventsData.indices) {
            if (eventsData[index].drug_rxcui == drugToDelete) {
                eventsData.removeAt(index)
                break
            }
        }
    }

    private fun SendResultToParent() {
        val intent = Intent()
        intent.putExtra(DbConstants.DRUGSLIST, drugsToDelete.toTypedArray())
        targetFragment!!.onActivityResult(
            targetRequestCode,
            DbConstants.DRUGDELETEPOPUP,
            intent
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (drugsToDelete.isNotEmpty()) {
            SendResultToParent()
        }
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