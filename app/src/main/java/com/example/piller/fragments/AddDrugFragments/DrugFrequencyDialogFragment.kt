package com.example.piller.fragments.AddDrugFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.piller.R
import com.example.piller.utilities.DbConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.drug_occurrence_frequency_dialog.*

class DrugFrequencyDialogFragment(
    private val _setNoRepeat: () -> Unit,
    private val _chooseRepeatFrequency: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.drug_occurrence_frequency_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        do_freq_close.setOnClickListener { dismiss() }
        do_freq_no_repeat.setOnClickListener {
            _setNoRepeat()
            dismiss()
        }
        do_freq_repeat_on.setOnClickListener {
            dismiss()
            _chooseRepeatFrequency()
        }
    }


    companion object {
        const val TAG = DbConstants.drugFrequencyDialogFragmentTag

        fun newInstance(
            setNoRepeat: () -> Unit,
            chooseRepeatFrequency: () -> Unit
        ): DrugFrequencyDialogFragment =
            DrugFrequencyDialogFragment(setNoRepeat, chooseRepeatFrequency)
    }
}