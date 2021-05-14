package com.example.piller.fragments.AddDrugFragments

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.piller.R
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugOccurrenceViewModel
import kotlinx.android.synthetic.main.drug_frequency_repeat_dialog.*
import java.util.*

class DrugFrequencyRepeatDialogFragment(
    private val _setRepeat: (DrugOccurrenceViewModel.RepeatOn, freqValue: Int) -> Unit,
    private val _backPressCallback: () -> Unit,
    private val _weeklyCallback: (DrugOccurrenceViewModel.RepeatOn, freqValue: Int) -> Unit,
    private val _defaultValue: Int = DbConstants.invalidFrequencyValue,
    private val _defaultFreqValue: DrugOccurrenceViewModel.RepeatOn = DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
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
            if (do_freq_repeat.getItemAt(newVal)
                    .toLowerCase(Locale.ROOT) == DbConstants.weeksRepeat
            ) {
                do_freq_done.text = getString(R.string.drugFrequencyNext)
            } else {
                do_freq_done.text = getString(R.string.drugFrequencyDone)
            }
        }

        do_freq_back_btn.setOnClickListener {
            dismiss()
            _backPressCallback()
        }

        do_freq_done.setOnClickListener {
            val chosenFrequency = convertStringToFrequency()
            if (chosenFrequency != DrugOccurrenceViewModel.RepeatOn.WEEK) {
                _setRepeat(chosenFrequency, do_freq_repeat_number.value)
            } else {
                _weeklyCallback(chosenFrequency, do_freq_repeat_number.value)
            }
            dismiss()
        }
    }

    private fun setInitialData() {
        do_freq_repeat_number.value =
            if (_defaultValue > DbConstants.invalidFrequencyValue) _defaultValue else DbConstants.defaultFrequencyValue
        if (_defaultFreqValue != DrugOccurrenceViewModel.RepeatOn.NO_REPEAT) {
            do_freq_repeat.value = convertEnumToIndex()
        }
    }

    private fun convertEnumToIndex(): Int {
        return when (_defaultFreqValue) {
            DrugOccurrenceViewModel.RepeatOn.DAY -> DbConstants.dayEnumValue
            DrugOccurrenceViewModel.RepeatOn.WEEK -> DbConstants.weekEnumValue
            DrugOccurrenceViewModel.RepeatOn.MONTH -> DbConstants.monthEnumValue
            DrugOccurrenceViewModel.RepeatOn.YEAR -> DbConstants.yearEnumValue
            else -> DbConstants.noRepeatEnumValue
        }
    }

    private fun convertStringToFrequency(): DrugOccurrenceViewModel.RepeatOn {
        return when (do_freq_repeat.getSelectedItemString()?.toLowerCase(Locale.ROOT)) {
            getString(R.string.daysFrequencyString) -> DrugOccurrenceViewModel.RepeatOn.DAY
            getString(R.string.weeksFrequencyString) -> DrugOccurrenceViewModel.RepeatOn.WEEK
            getString(R.string.monthsFrequencyString) -> DrugOccurrenceViewModel.RepeatOn.MONTH
            getString(R.string.yearsFrequencyString) -> DrugOccurrenceViewModel.RepeatOn.YEAR
            else -> DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
        }
    }

    companion object {
        const val TAG = DbConstants.drugFrequencyRepeatDialogFragmentTag

        fun newInstance(
            setRepeat: (DrugOccurrenceViewModel.RepeatOn, freqValue: Int) -> Unit,
            backPressCallback: () -> Unit,
            weeklyCallback: (DrugOccurrenceViewModel.RepeatOn, freqValue: Int) -> Unit,
            defaultValue: Int = DbConstants.invalidFrequencyValue,
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