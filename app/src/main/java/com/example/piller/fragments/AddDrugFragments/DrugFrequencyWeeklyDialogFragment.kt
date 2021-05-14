package com.example.piller.fragments.AddDrugFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.utilities.DbConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.drug_frequency_weekly_dialog.*


class DrugFrequencyWeeklyDialogFragment(
    private val _doneCallback: (daysCheck: Array<Boolean>) -> Unit,
    private val _backCallback: () -> Unit,
    private var _daysCheck: Array<Boolean>
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.drug_frequency_weekly_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        do_freq_week_list.layoutManager = LinearLayoutManager(context)
        do_freq_week_list.adapter = DrugObjectAdapter(getDaysList())
        do_freq_back_btn.setOnClickListener {
            _backCallback()
            dismiss()
        }
        do_freq_done.setOnClickListener {
            if (_daysCheck.isNotEmpty()) {
                _doneCallback(_daysCheck)
                dismiss()
            } else {
                SnackBar.showToastBar(context, DbConstants.noDaysChosenError)
            }
        }
    }

    private fun getDaysList(): List<Pair<String, Boolean>> {
        val dayOfWeekString = resources.getStringArray(R.array.daysName)
        return dayOfWeekString.zip(_daysCheck) { a, b -> Pair(a, b) }
    }

    private inner class ViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.drug_frequency_weekly_dialog_item,
            parent,
            false
        )
    ) {
        val checkBox: CheckBox = itemView.findViewById(R.id.do_freq_week_day_name_cb)
    }

    private inner class DrugObjectAdapter(private val dataset: List<Pair<String, Boolean>>) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.checkBox.text = dataset[position].first
            holder.checkBox.isChecked = dataset[position].second
            holder.checkBox.setOnClickListener {
                _daysCheck[position] = holder.checkBox.isChecked
            }
        }

        override fun getItemCount() = dataset.size
    }

    companion object {

        const val TAG = DbConstants.drugFrequencyWeeklyDialogFragmentTag

        fun newInstance(
            doneCallback: (daysCheck: Array<Boolean>) -> Unit,
            backCallback: () -> Unit,
            daysCheck: Array<Boolean>
        ): DrugFrequencyWeeklyDialogFragment =
            DrugFrequencyWeeklyDialogFragment(doneCallback, backCallback, daysCheck)
    }
}