package com.example.piller.fragments.AddDrugFragments

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.piller.R
import com.example.piller.viewModels.DrugOccurrenceViewModel
import kotlinx.android.synthetic.main.drug_frequency_repeat_dialog.*
import java.util.*

class DrugFrequencyRepeatDialogFragment(
    private val setRepeat: (DrugOccurrenceViewModel.RepeatOn, freqValue: Int) -> Unit,
    private val backPressCallback: () -> Unit,
    private val weeklyCallback: (DrugOccurrenceViewModel.RepeatOn, freqValue: Int) -> Unit,
    private val defaultValue: Int = -1,
    private val defaultFreqValue: DrugOccurrenceViewModel.RepeatOn = DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.drug_frequency_repeat_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initListeners()
        setInitialData()
    }

    private fun initListeners() {
        do_freq_repeat.setOnValueChangedListener { _, _, newVal ->
            if (do_freq_repeat.getItemAt(newVal).toLowerCase(Locale.ROOT) == "weeks") {
                do_freq_done.text = "Next"
            } else {
                do_freq_done.text = "Done"
            }
        }

        do_freq_back_btn.setOnClickListener {
            dismiss()
            backPressCallback()
        }

        do_freq_done.setOnClickListener {
            val chosenFrequency = convertStringToFrequency()
            if (chosenFrequency != DrugOccurrenceViewModel.RepeatOn.WEEK) {
                setRepeat(chosenFrequency, do_freq_repeat_number.value)
            } else {
                weeklyCallback(chosenFrequency, do_freq_repeat_number.value)
            }
            dismiss()
        }
    }

    private fun setInitialData() {
        do_freq_repeat_number.value = if (defaultValue > -1) defaultValue else 0
        if (defaultFreqValue != DrugOccurrenceViewModel.RepeatOn.NO_REPEAT) {
            do_freq_repeat.value = convertEnumToIndex()
        }
    }

    private fun convertEnumToIndex(): Int {
        return when (defaultFreqValue) {
            DrugOccurrenceViewModel.RepeatOn.DAY -> 0
            DrugOccurrenceViewModel.RepeatOn.WEEK -> 1
            DrugOccurrenceViewModel.RepeatOn.MONTH -> 2
            DrugOccurrenceViewModel.RepeatOn.YEAR -> 3
            else -> 0
        }
    }

    private fun convertStringToFrequency(): DrugOccurrenceViewModel.RepeatOn {
        return when (do_freq_repeat.getSelectedItemString()?.toLowerCase(Locale.ROOT)) {
            "days" -> DrugOccurrenceViewModel.RepeatOn.DAY
            "weeks" -> DrugOccurrenceViewModel.RepeatOn.WEEK
            "months" -> DrugOccurrenceViewModel.RepeatOn.MONTH
            "years" -> DrugOccurrenceViewModel.RepeatOn.YEAR
            else -> DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
        }
    }

    companion object {
        const val TAG = "DrugFrequencyRepeatDialogFragment"

        fun newInstance(
            setRepeat: (DrugOccurrenceViewModel.RepeatOn, freqValue: Int) -> Unit,
            backPressCallback: () -> Unit,
            weeklyCallback: (DrugOccurrenceViewModel.RepeatOn, freqValue: Int) -> Unit,
            defaultValue: Int = -1,
            defaultFreqValue: DrugOccurrenceViewModel.RepeatOn = DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
        ): DrugFrequencyRepeatDialogFragment = DrugFrequencyRepeatDialogFragment(
            setRepeat,
            backPressCallback,
            weeklyCallback,
            defaultValue,
            defaultFreqValue
        )
    }
}