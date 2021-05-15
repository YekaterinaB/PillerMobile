package com.example.piller.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.fragments.AddDrugFragments.*
import com.example.piller.models.DrugObject
import com.example.piller.utilities.DateUtils
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugOccurrenceViewModel
import kotlinx.android.synthetic.main.activity_drug_occurrence.*
import kotlinx.android.synthetic.main.drug_occurrence.*
import java.text.SimpleDateFormat
import java.util.*


class DrugOccurrenceActivity : ActivityWithUserObject() {
    private lateinit var _newDrugName: TextView
    private lateinit var _drugOccurrencesDate: TextView
    private lateinit var _drugOccurrencesTime: TextView
    private lateinit var _drugRefillSwitch: SwitchCompat
    private lateinit var _drugRefillCurrentAmount: EditText
    private lateinit var _drugRefillWhenIHaveLeft: TextView
    private lateinit var _drugRefillReminderTime: TextView
    private lateinit var _drugFrequencyTV: TextView
    private lateinit var _drugFrequencyContainer: ConstraintLayout
    private lateinit var _addDrug: Button
    private lateinit var _goBackBtn: ImageView
    private lateinit var _refillDataContainer: ConstraintLayout
    private lateinit var _drugDosageET: EditText
    private lateinit var _drugDosageLabelTV: TextView
    private lateinit var _repeatEndDateTV: TextView
    private lateinit var _viewModel: DrugOccurrenceViewModel
    private var _drugIntakeTime: Date = Date()
    private var _drugRepeatEnd: Date = Date()
    private var _repeatOnValue = DbConstants.repeatOnDefaultValue
    private var _isInEditMode = false
    private var _firstClickOnLabel = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drug_occurrence)
        _viewModel = ViewModelProvider(this).get(DrugOccurrenceViewModel::class.java)
        initAllIntentExtras()
        initViews()
        initTextWatchers()
        initViewsInitialData()
        initListeners()
        initViewModelObservers()
        initEditModeData()
    }

    private fun initTextWatchers() {
        val textWatcher = object : TextWatcher {
            private val minDosage = getString(R.string.minDosage)
            private val defaultDosage = getString(R.string.defaultDosage)

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == minDosage) {
                    SnackBar.showToastBar(
                        this@DrugOccurrenceActivity,
                        getString(R.string.pleaseEnterAValidDosage)
                    )
                    _drugDosageET.setText(defaultDosage)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        _drugDosageET.addTextChangedListener(textWatcher)
    }

    private fun initEditModeData() {
        if (_isInEditMode) {
            _drugDosageET.setText(_viewModel.getDrug().dose.totalDose.toString())
            initRefillForEditMode()
            initRepeatEnd()
            initRepeatCheckedWeekdays()
            _addDrug.text = getString(R.string.saveChanges)
            _viewModel.dosageMeasurementType = _viewModel.getDrug().dose.measurementType
        }
    }

    private fun initRepeatCheckedWeekdays() {
        updateRepeat(occurrenceRepeatToEnumAndUpdate())
        _viewModel.initRepeatCheckedWeekdays()
    }

    private fun occurrenceRepeatToEnumAndUpdate(): DrugOccurrenceViewModel.RepeatOn {
        val drug = _viewModel.getDrug()
        val repeat: DrugOccurrenceViewModel.RepeatOn
        when {
            drug.occurrence.hasRepeatDay() -> {
                _repeatOnValue = drug.occurrence.repeatDay
                repeat = DrugOccurrenceViewModel.RepeatOn.DAY
            }
            drug.occurrence.hasRepeatWeek() -> {
                _repeatOnValue = drug.occurrence.repeatWeek
                repeat = DrugOccurrenceViewModel.RepeatOn.WEEK
            }
            drug.occurrence.hasRepeatMonth() -> {
                _repeatOnValue = drug.occurrence.repeatMonth
                repeat = DrugOccurrenceViewModel.RepeatOn.MONTH
            }
            drug.occurrence.hasRepeatYear() -> {
                _repeatOnValue = drug.occurrence.repeatYear
                repeat = DrugOccurrenceViewModel.RepeatOn.YEAR
            }
            else -> repeat = DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
        }

        return repeat
    }

    private fun initAllIntentExtras() {
        initUserObject(intent)
        initDrugIntakeTime(
            intent.getLongExtra(
                DbConstants.INTAKE_DATE,
                DbConstants.defaultIntakeTime
            )
        )

        val drug = intent.getParcelableExtra<DrugObject>(DbConstants.DRUG_OBJECT)!!
        drug.occurrence.repeatStart = _drugIntakeTime.time
        _viewModel.setDrug(drug)
    }

    private fun hasIntakeTime(intakeTime: Long): Boolean {
        return intakeTime > DbConstants.defaultIntakeTime
    }

    private fun initDrugIntakeTime(intakeFromIntent: Long) {
        if (hasIntakeTime(intakeFromIntent)) {
            _isInEditMode = true
            _drugIntakeTime.time = intakeFromIntent
        } else {
            val calendar = Calendar.getInstance()
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            _drugIntakeTime.time = calendar.timeInMillis
        }
    }

    private fun initRefillForEditMode() {
        val refill = _viewModel.getDrug().refill
        if (refill.isToNotify) {
            _drugRefillSwitch.isChecked = true
            setRefillVisibility(true)
            _drugRefillReminderTime.text = refill.reminderTime
            val currentRemaining = "When I have ${refill.pillsBeforeReminder} meds remaining"
            _drugRefillWhenIHaveLeft.text = currentRemaining
            _drugRefillCurrentAmount.setText(refill.pillsLeft.toString())
            _viewModel.refillReminder = refill.pillsBeforeReminder
            _viewModel.refillReminderTime = refill.reminderTime
        }
    }

    private fun initRepeatEnd() {
        val currentDrug = _viewModel.getDrug()
        if (currentDrug.occurrence.hasRepeatEnd()) {
            _viewModel.hasRepeatEnd = true
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = _drugRepeatEnd.time
            setRepeatEndDateLabel(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR)
            )
        }
    }

    private fun initViewModelObservers() {
        _viewModel.snackBarMessage.observe(this, Observer { message ->
            SnackBar.showToastBar(this, message)
        })

        _viewModel.addedDrugSuccess.observe(this, Observer { added ->
            if (added) {
                goBackToCalendarActivity(getString(R.string.newDrugAdded))
            }
        })

        _viewModel.updatedDrugSuccess.observe(this, Observer { added ->
            if (added) {
                goBackToCalendarActivity(getString(R.string.drugOccurrenceWasUpdated))
            }
        })
    }

    private fun goBackToCalendarActivity(toastBarMessage: String) {
        SnackBar.showToastBar(this, toastBarMessage)
        val intent = Intent(this@DrugOccurrenceActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        putLoggedUserObjectInIntent(intent)
        startActivity(intent)
    }

    private fun initViewsInitialData() {
        //  initiate start date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _drugIntakeTime.time
        setRepeatStartDateLabel(
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.YEAR)
        )

        //  initiate start time
        setTimeLabel(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        _drugRepeatEnd = Date(_viewModel.getDrug().occurrence.repeatEnd)
    }

    private fun initListeners() {
        _drugOccurrencesDate.setOnClickListener {
            showStartDatePickerDialog()
        }

        _drugOccurrencesTime.setOnClickListener {
//            showRepeatStartDialog()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = _drugIntakeTime.time
            showTimePickerDialog(
                calendar[Calendar.HOUR_OF_DAY],
                calendar[Calendar.MINUTE]
            ) { hourOfDay, minute ->
                _viewModel.setDrugRepeatStartTime(hourOfDay, minute)
                setTimeLabel(hourOfDay, minute)
            }
        }

        _repeatEndDateTV.setOnClickListener {
            showEndDatePickerDialog()
        }

        _newDrugName.setOnLongClickListener {
            SnackBar.showToastBar(this, _viewModel.getDrug().drugName)
            return@setOnLongClickListener true
        }

        _addDrug.setOnClickListener {
            if (isDataValid()) {
                _viewModel.totalDose = _drugDosageET.text.toString().toFloat()
                addOrEditDrug()
            }
        }

        _goBackBtn.setOnClickListener {
            finish()
        }

        val dosages = resources.getStringArray(R.array.drug_dosage)
        //  set first dosage measurement type as default
        _viewModel.dosageMeasurementType = dosages[0]
        _drugDosageLabelTV.setOnClickListener {
            showPickerDialog(getString(R.string.dosageTitle), dosages) { dosage ->
                _drugDosageLabelTV.text = dosage
                _viewModel.dosageMeasurementType = dosage
            }
        }

        _drugRefillSwitch.setOnClickListener {
            setRefillVisibility(_drugRefillSwitch.isChecked)
        }

        //  get numbers between min and max as string
        val dataset = IntRange(DbConstants.minRefill, DbConstants.maxRefill).map { it.toString() }
            .toTypedArray()
        _drugRefillWhenIHaveLeft.setOnClickListener {
            showPickerDialog(getString(R.string.refillReminderTitle), dataset) { refill ->
                _viewModel.refillReminder = refill.toInt()
                val currentRemaining = "When I have ${_viewModel.refillReminder} meds remaining"
                _drugRefillWhenIHaveLeft.text = currentRemaining
            }
        }

        _drugFrequencyContainer.setOnClickListener {
            if (_viewModel.repeatOnEnum == DrugOccurrenceViewModel.RepeatOn.NO_REPEAT) {
                showDrugFreqDialog()
            } else {
                chooseRepeatFrequency()
            }
        }

        _drugRefillReminderTime.setOnClickListener {
            val selectedHour = _viewModel.getRefillReminderHour()
            val selectedMinute = _viewModel.getRefillReminderMinute()
            showTimePickerDialog(selectedHour, selectedMinute) { hourOfDay, minute ->
                _viewModel.refillReminderTime = getStringTime(hourOfDay, minute)
                _drugRefillReminderTime.text = _viewModel.refillReminderTime
            }
        }
    }

    private fun getStringTime(hourOfDay: Int, minute: Int): String {
        val stringHour = if (hourOfDay < 10) "0$hourOfDay" else "$hourOfDay"
        val stringMinute = if (minute < 10) "0$minute" else "$minute"
        return "$stringHour:$stringMinute"
    }

    private fun showPickerDialog(
        title: String,
        dataset: Array<String>,
        optionSelected: (String) -> Unit
    ) {
        DrugPickerDialogFragment(
            title,
            dataset,
            _optionSelected = { option -> optionSelected(option) }
        ).apply {
            show(supportFragmentManager, DrugPickerDialogFragment.TAG)
        }
    }

    private fun showDrugFreqDialog() {
        DrugFrequencyDialogFragment(
            _setNoRepeat = {
                updateRepeat(DrugOccurrenceViewModel.RepeatOn.NO_REPEAT)
                _repeatOnValue = DbConstants.repeatOnDefaultValue
            },
            _chooseRepeatFrequency = { chooseRepeatFrequency() }).apply {
            show(supportFragmentManager, DrugFrequencyDialogFragment.TAG)
        }
    }

    private fun updateRepeat(repeatEnum: DrugOccurrenceViewModel.RepeatOn) {
        _viewModel.repeatOnEnum = repeatEnum
        _drugFrequencyTV.text = _viewModel.convertRepeatEnumToString(_viewModel.repeatOnEnum)
    }

    private fun showRepeatStartDialog() {
        DrugStartRepeatDialogFragment(
            _viewModel.repeatStartTime,
            _doneCallback = { mutableList ->
                _viewModel.repeatStartTime = mutableList
            }
        ).apply {
            show(supportFragmentManager, DrugStartRepeatDialogFragment.TAG)
        }
    }

    private fun chooseRepeatFrequency() {
        DrugFrequencyRepeatDialogFragment(
            _setRepeat = { repeatOn, freqValue ->
                updateRepeat(repeatOn)
                _repeatOnValue = freqValue
            },
            _backPressCallback = { showDrugFreqDialog() },
            _weeklyCallback = { repeatOn, freqValue -> chooseDaysOfWeek(repeatOn, freqValue) },
            _defaultValue = _repeatOnValue,
            _defaultFreqValue = _viewModel.repeatOnEnum
        ).apply {
            show(supportFragmentManager, DrugFrequencyRepeatDialogFragment.TAG)
        }
    }

    private fun chooseDaysOfWeek(repeat: DrugOccurrenceViewModel.RepeatOn, freqValue: Int) {
        DrugFrequencyWeeklyDialogFragment(
            _daysCheck = _viewModel.repeatCheckWeekdays,
            _doneCallback = { daysCheck ->
                updateRepeat(repeat)
                _viewModel.repeatCheckWeekdays = daysCheck
                _repeatOnValue = freqValue
            },
            _backCallback = { chooseRepeatFrequency() }
        ).apply {
            show(supportFragmentManager, DrugFrequencyWeeklyDialogFragment.TAG)
        }
    }

    private fun initViews() {
        _newDrugName = findViewById(R.id.do_drug_name)
        _newDrugName.text = _viewModel.getDrug().drugName
        _drugOccurrencesDate = findViewById(R.id.do_repeat_start)
        _drugOccurrencesTime = findViewById(R.id.do_repeat_time)
        _repeatEndDateTV = findViewById(R.id.do_repeat_end)
        _drugDosageET = findViewById(R.id.do_dosage)
        _drugDosageLabelTV = findViewById(R.id.do_dosage_label)

        _drugRefillSwitch = findViewById(R.id.do_refill_switch)
        _drugRefillCurrentAmount = findViewById(R.id.do_current_meds)
        _drugRefillWhenIHaveLeft = findViewById(R.id.do_remaining_meds)
        _drugRefillReminderTime = findViewById(R.id.do_refill_reminder_time)
        _drugFrequencyTV = findViewById(R.id.do_frequency)
        _drugFrequencyContainer = findViewById(R.id.do_frequency_container)

        _addDrug = findViewById(R.id.do_add_btn)
        _goBackBtn = findViewById(R.id.do_back_btn)
        _refillDataContainer = findViewById(R.id.do_refill_data_container)
    }

    private fun setRefillVisibility(show: Boolean) {
        if (show) {
            _refillDataContainer.visibility = View.VISIBLE
        } else {
            _refillDataContainer.visibility = View.GONE
        }
    }

    private fun setTimeLabel(hour: Int, minutes: Int) {
        val formatter = SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault())
        val calTime: Date
        if (_isInEditMode && _firstClickOnLabel) {
            calTime = _drugIntakeTime
            //  set isInEditMode to false so we won't enter this if again (and then the user
            //  won't br able to update intake date)
            _firstClickOnLabel = false
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = _drugIntakeTime.time
            DateUtils.setCalendarTime(calendar, hour, minutes)
            calTime = calendar.time
        }

        _drugOccurrencesTime.text = formatter.format(calTime)
        _drugIntakeTime.time = calTime.time
    }

    private fun setRepeatStartDateLabel(day: Int, month: Int, year: Int) {
        val sdf = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        val calDate: Date
        if (_isInEditMode && _firstClickOnLabel) {
            calDate = _drugIntakeTime
            //  set isInEditMode to false so we won't enter this if again (and then the user
            //  won't br able to update intake time)
            _firstClickOnLabel = false
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = _drugIntakeTime.time
            calendar.set(year, month, day)
            calDate = calendar.time
        }

        // Display Selected date in textbox
        _drugOccurrencesDate.text = sdf.format(calDate)
        _drugIntakeTime.time = calDate.time
    }

    private fun showTimePickerDialog(
        InitialHour: Int,
        initialMinute: Int,
        callback: (Int, Int) -> Unit
    ) {
        val tpd =
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    callback(hourOfDay, minute)
                },
                InitialHour,
                initialMinute,
                true
            )

        tpd.show()
    }

    private fun showEndDatePickerDialog() {
        val startCalendar = Calendar.getInstance()
        startCalendar.timeInMillis = _drugIntakeTime.time
        val endCalendar = Calendar.getInstance()
        endCalendar.timeInMillis = startCalendar.timeInMillis
        DateUtils.zeroTime(endCalendar)
        //  minimum end date is day after start date
        endCalendar.add(Calendar.DATE, 1)
        val year = startCalendar.get(Calendar.YEAR)
        val month = startCalendar.get(Calendar.MONTH)
        val day = startCalendar.get(Calendar.DAY_OF_MONTH)

        val dpd =
            DatePickerDialog(
                this,
                { _, yearSelected, monthOfYear, dayOfMonth ->
                    setRepeatEndDateLabel(dayOfMonth, monthOfYear, yearSelected)
                    val cal = Calendar.getInstance()
                    cal.set(yearSelected, monthOfYear, dayOfMonth)
                    _drugRepeatEnd = cal.time
                    _viewModel.hasRepeatEnd = true
                },
                year,
                month,
                day
            )
        dpd.datePicker.minDate = endCalendar.timeInMillis
        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, which ->
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                _viewModel.hasRepeatEnd = false
                _repeatEndDateTV.text = getString(R.string.none)
            }
        }
        dpd.show()
    }

    private fun setRepeatEndDateLabel(day: Int, month: Int, year: Int) {
        val sdf = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        val calDate: Date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _drugIntakeTime.time
        calendar.set(year, month, day)
        calDate = calendar.time

        // Display Selected date in textbox
        _repeatEndDateTV.text = sdf.format(calDate)
    }

    private fun showStartDatePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _drugIntakeTime.time
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd =
            DatePickerDialog(
                this,
                { _, yearSelected, monthOfYear, dayOfMonth ->
                    _viewModel.setDrugRepeatStartDate(yearSelected, monthOfYear, dayOfMonth)
                    setRepeatStartDateLabel(dayOfMonth, monthOfYear, yearSelected)
                    updateRepeatEndDate(yearSelected, monthOfYear, dayOfMonth)
                },
                year,
                month,
                day
            )

        dpd.show()
    }

    private fun updateRepeatEndDate(yearSelected: Int, monthOfYear: Int, dayOfMonth: Int) {
        val selectedDate = Calendar.getInstance()
        selectedDate.set(yearSelected, monthOfYear, dayOfMonth)
        if (_viewModel.hasRepeatEnd && DateUtils.isDateBefore(_drugRepeatEnd, selectedDate.time)) {
            val endCalendar = Calendar.getInstance()
            endCalendar.timeInMillis =
                DateUtils.getDayAfterInMillis(yearSelected, monthOfYear, dayOfMonth)
            setRepeatEndDateLabel(
                endCalendar.get(Calendar.DAY_OF_MONTH),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.YEAR)
            )
        }
    }

    private fun addOrEditDrug() {
        if (_drugRefillSwitch.isChecked) {
            _viewModel.setDrugRefill(
                _drugRefillCurrentAmount.text.toString().toInt(),
                _viewModel.refillReminderTime
            )
        } else {
            _viewModel.removeDrugRefill()
        }

        _viewModel.updateDrugDosage(_drugDosageET.text.toString().toFloat())
        //  if it's in edit mode and the user chose to edit all occurrences - go to update
        if (_isInEditMode
            && intent.getBooleanExtra(DbConstants.EDIT_ONLY_FUTURE_OCCURRENCES, false)
        ) {
            //edit drug
            _viewModel.updateDrugOccurrence(
                loggedUserObject,
                _repeatOnValue,
                _drugRepeatEnd,
                this
            )
        } else {
            //  otherwise, add a new drug
            _viewModel.addNewDrugToUser(
                loggedUserObject,
                _repeatOnValue,
                _drugRepeatEnd,
                this
            )
        }
    }

    private fun isDataValid(): Boolean {
        var valid = true
        if (_drugRefillSwitch.isChecked && _drugRefillCurrentAmount.text.isNullOrEmpty()) {
            valid = false
            _drugRefillCurrentAmount.error = getString(R.string.invalidRefill)
            _drugRefillCurrentAmount.requestFocus()
        }

        return valid
    }
}