package com.example.piller.fragments.AddDrugFragments

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.piller.R
import com.example.piller.utilities.DbConstants
import kotlinx.android.synthetic.main.drug_occurrence_picker_dialog.*

class DrugPickerDialogFragment(
    private val title: String,
    private val optionsArray: Array<String>,
    private val optionSelected: (String) -> Unit
) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.drug_occurrence_picker_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        do_number_chooser_list.layoutManager = LinearLayoutManager(context)
        do_number_chooser_list.adapter = DrugObjectAdapter(optionsArray)
        do_freq_picker_title.text = title
        do_picker_close_btn.setOnClickListener { dismiss() }
    }

    private inner class ViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.drug_occurrence_picker_dialog_item,
            parent,
            false
        )
    ) {
        val text: TextView = itemView.findViewById(R.id.do_number_chooser_item)
    }

    private inner class DrugObjectAdapter(private val dataset: Array<String>) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataset[position]
            holder.text.text = item
            holder.text.setOnClickListener {
                optionSelected(item)
                dismiss()
            }
        }

        override fun getItemCount(): Int = dataset.size
    }

    companion object {
        const val TAG = DbConstants.drugPickerDialogFragmentTag

        fun newInstance(
            title: String,
            optionsArray: Array<String>,
            optionSelected: (String) -> Unit
        ): DrugPickerDialogFragment =
            DrugPickerDialogFragment(title, optionsArray, optionSelected)
    }
}