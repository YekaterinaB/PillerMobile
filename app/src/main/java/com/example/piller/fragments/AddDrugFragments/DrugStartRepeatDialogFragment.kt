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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.drug_occurrence_start_repeat_dialog.*
import java.text.SimpleDateFormat
import java.util.*

class DrugStartRepeatDialogFragment(
    private val pickedDatesList: MutableList<Long>,
    private val doneCallback: (MutableList<Long>) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var calendarsAdapter: DrugObjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.drug_occurrence_start_repeat_dialog, container, false)
    }

    private fun initListIfEmpty() {
        if (pickedDatesList.size == 0) {
            pickedDatesList.add(Calendar.getInstance().timeInMillis)
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
            doneCallback(pickedDatesList)
        }

        do_repeat_start_close_btn.setOnClickListener { dismiss() }

        do_repeat_start_add.setOnClickListener {
            addNewRepeatStart()
        }
    }

    private fun addNewRepeatStart() {
        pickedDatesList.add(Calendar.getInstance().timeInMillis)
        calendarsAdapter.notifyDataSetChanged()
        if (pickedDatesList.size >= 5) {
            setAddBtnVisibility(false)
        }
    }

    private fun setAddBtnVisibility(visible: Boolean) {
        if (visible) {
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

        private val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.dateTV.text = parseDateToString(pickedDatesList[position])

            holder.dateTV.setOnClickListener {
                showTimePickerDialog(pickedDatesList[position]) { hourOfDay, minute ->
                    pickedDatesList[position] = getUpdatedTime(hourOfDay, minute)
                    notifyDataSetChanged()
                }
            }

            if (position == 0) {
                //  hide delete button for first repeat start
                holder.deleteItem.visibility = View.INVISIBLE
            } else {
                holder.deleteItem.setOnClickListener {
                    pickedDatesList.removeAt(position)
                    notifyDataSetChanged()
                    setAddBtnVisibility(true)
                }
            }
        }

        private fun getUpdatedTime(hourOfDay: Int, minute: Int): Long {
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = hourOfDay
            calendar[Calendar.MINUTE] = minute
            return calendar.timeInMillis
        }

        override fun getItemCount(): Int = pickedDatesList.size

        private fun parseDateToString(timeInMillis: Long): String = formatter.format(timeInMillis)


        private fun showTimePickerDialog(timeInMillis: Long, callback: (Int, Int) -> Unit) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
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
        const val TAG = "DrugStartRepeatDialogFragment"

        fun newInstance(
            pickedDatesList: MutableList<Long>,
            doneCallback: (MutableList<Long>) -> Unit
        ): DrugStartRepeatDialogFragment =
            DrugStartRepeatDialogFragment(pickedDatesList, doneCallback)
    }
}