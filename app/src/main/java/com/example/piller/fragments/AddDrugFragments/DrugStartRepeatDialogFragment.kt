package com.example.piller.fragments.AddDrugFragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.utilities.DbConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.drug_occurrence_start_repeat_dialog.*
import java.text.SimpleDateFormat
import java.util.*

class DrugStartRepeatDialogFragment(
    private val _pickedDatesList: MutableList<Calendar>,
    private val _doneCallback: (MutableList<Calendar>) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var calendarsAdapter: DrugObjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.drug_occurrence_start_repeat_dialog, container, false)
    }

    private fun initListIfEmpty() {
        if (_pickedDatesList.size == 0) {
            _pickedDatesList.add(Calendar.getInstance())
            calendarsAdapter.notifyDataSetChanged()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        calendarsAdapter = DrugObjectAdapter()
        initListIfEmpty()
        do_repeat_start_list.layoutManager = LinearLayoutManager(context)
        do_repeat_start_list.adapter = calendarsAdapter
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        do_repeat_start_done.setOnClickListener {
            dismiss()
            _doneCallback(_pickedDatesList)
        }

        do_repeat_start_close_btn.setOnClickListener { dismiss() }

        do_repeat_start_add.setOnClickListener {
            addNewRepeatStart()
        }
    }

    private fun addNewRepeatStart() {
        _pickedDatesList.add(Calendar.getInstance())
        calendarsAdapter.notifyDataSetChanged()
        updateAddBtnVisibility()
    }

    private fun updateAddBtnVisibility() {
        if (_pickedDatesList.size <= DbConstants.maxStartRepeats) {
            do_repeat_start_add.visibility = View.VISIBLE
        } else {
            do_repeat_start_add.visibility = View.GONE
        }
    }

    private inner class ViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.drug_occurrence_start_repeat_dialog_item,
            parent,
            false
        )
    ) {
        val dateTV: TextView = itemView.findViewById(R.id.do_repeat_start_item)
        val deleteItem: ImageButton = itemView.findViewById(R.id.do_repeat_start_delete)
    }

    private inner class DrugObjectAdapter : RecyclerView.Adapter<ViewHolder>() {

        private val formatter =
            SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault())

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.dateTV.text = parseDateToString(_pickedDatesList[position])

            holder.dateTV.setOnClickListener {
                showTimePickerDialog(_pickedDatesList[position]) { hourOfDay, minute ->
                    _pickedDatesList[position][Calendar.HOUR_OF_DAY] = hourOfDay
                    _pickedDatesList[position][Calendar.MINUTE] = minute
                    notifyDataSetChanged()
                }
            }

            if (position == 0) {
                //  hide delete button for first repeat start
                holder.deleteItem.visibility = View.INVISIBLE
            } else {
                holder.deleteItem.setOnClickListener {
                    _pickedDatesList.removeAt(position)
                    notifyDataSetChanged()
                    updateAddBtnVisibility()
                }
            }
        }

        override fun getItemCount(): Int = _pickedDatesList.size

        private fun parseDateToString(calendar: Calendar): String {
            val date = calendar.time
            return formatter.format(date)
        }

        private fun showTimePickerDialog(calendar: Calendar, callback: (Int, Int) -> Unit) {
            val initialHour = calendar[Calendar.HOUR_OF_DAY]
            val initialMinute = calendar[Calendar.MINUTE]
            val tpd =
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        callback(hourOfDay, minute)
                    },
                    initialHour,
                    initialMinute,
                    true
                )

            tpd.show()
        }
    }

    companion object {
        const val TAG = DbConstants.drugStartRepeatDialogFragmentTag

        fun newInstance(
            pickedDatesList: MutableList<Calendar>,
            doneCallback: (MutableList<Calendar>) -> Unit
        ): DrugStartRepeatDialogFragment =
            DrugStartRepeatDialogFragment(pickedDatesList, doneCallback)
    }
}