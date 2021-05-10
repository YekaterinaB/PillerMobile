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
    private lateinit var newDrugName: TextView
    private lateinit var drugOccurrencesDate: TextView
    private lateinit var drugOccurrencesTime: TextView
    private lateinit var drugRefillSwitch: SwitchCompat
    private lateinit var drugRefillCurrentAmount: EditText
    private lateinit var drugRefillWhenIHaveLeft: TextView
    private lateinit var drugRefillReminderTime: TextView
    private lateinit var _drugFrequencyTV: TextView
    private lateinit var _drugFrequencyContainer: ConstraintLayout
    private lateinit var _addDrug: Button
    private lateinit var _goBackBtn: ImageView
    private lateinit var _refillDataContainer: ConstraintLayout
    private lateinit var _drugDosageET: EditText
    private lateinit var _drugDosageLabelTV: TextView
    private lateinit var repeatEndDateTV: TextView
    private lateinit var viewModel: DrugOccurrenceViewModel
    private var drugIntakeTime: Date = Date()
    private var drugRepeatEnd: Date = Date()
    private var repeatOnValue = DbConstants.repeatOnDefaultValue
    private var isInEditMode = false
    private var firstClickOnLabel = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drug_occurrence)
        viewModel = ViewModelProvider(this).get(DrugOccurrenceViewModel::class.java)
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
        if (isInEditMode) {
            _drugDosageET.setText(viewModel.getDrug().dose.totalDose.toString())
            initRefillForEditMode()
            initRepeatEnd()
            initRepeatCheckedWeekdays()
            _addDrug.text = getString(R.string.saveChanges)
            viewModel.dosageMeasurementType = viewModel.getDrug().dose.measurementType
        }
    }

    private fun initRepeatCheckedWeekdays() {
        updateRepeat(occurrenceRepeatToEnumAndUpdate())
        viewModel.initRepeatCheckedWeekdays()
    }

    private fun occurrenceRepeatToEnumAndUpdate(): DrugOccurrenceViewModel.RepeatOn {
        val drug = viewModel.getDrug()
        val repeat: DrugOccurrenceViewModel.RepeatOn
        when {
            drug.occurrence.hasRepeatDay() -> {
                repeatOnValue = drug.occurrence.repeatDay
                repeat = DrugOccurrenceViewModel.RepeatOn.DAY
            }
            drug.occurrence.hasRepeatWeek() -> {
                repeatOnValue = drug.occurrence.repeatWeek
                repeat = DrugOccurrenceViewModel.RepeatOn.WEEK
            }
            drug.occurrence.hasRepeatMonth() -> {
                repeatOnValue = drug.occurrence.repeatMonth
                repeat = DrugOccurrenceViewModel.RepeatOn.MONTH
            }
            drug.occurrence.hasRepeatYear() -> {
                repeatOnValue = drug.occurrence.repeatYear
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
        drug.occurrence.repeatStart = drugIntakeTime.time
        viewModel.setDrug(drug)
    }

    private fun hasIntakeTime(intakeTime: Long): Boolean {
        return intakeTime > DbConstants.defaultIntakeTime
    }

    private fun initDrugIntakeTime(intakeFromIntent: Long) {
        if (hasIntakeTime(intakeFromIntent)) {
            isInEditMode = true
            drugIntakeTime.time = intakeFromIntent
        } else {
            val calendar = Calendar.getInstance()
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            drugIntakeTime.time = calendar.timeInMillis
        }
    }

    private fun initRefillForEditMode() {
        val refill = viewModel.getDrug().refill
        if (refill.isToNotify) {
            drugRefillSwitch.isChecked = true
            setRefillVisibility(true)
            drugRefillReminderTime.text = refill.reminderTime
            val currentRemaining = "When I have ${refill.pillsBeforeReminder} meds remaining"
            drugRefillWhenIHaveLeft.text = currentRemaining
            drugRefillCurrentAmount.setText(refill.pillsLeft.toString())
            viewModel.refillReminder = refill.pillsBeforeReminder
            viewModel.refillReminderTime = refill.reminderTime
        }
    }

    private fun initRepeatEnd() {
        val currentDrug = viewModel.getDrug()
        if (currentDrug.occurrence.hasRepeatEnd()) {
            viewModel.hasRepeatEnd = true
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = drugRepeatEnd.time
            setRepeatEndDateLabel(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR)
            )
        }
    }

    private fun initViewModelObservers() {
        viewModel.snackBarMessage.observe(this, Observer { message ->
            SnackBar.showToastBar(this, message)
        })

        viewModel.addedDrugSuccess.observe(this, Observer { added ->
            if (added) {
                goBackToCalendarActivity(getString(R.string.newDrugAdded))
            }
        })

        viewModel.updatedDrugSuccess.observe(this, Observer { added ->
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
        calendar.timeInMillis = drugIntakeTime.time
        setRepeatStartDateLabel(
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.YEAR)
        )

        //  initiate start time
        setTimeLabel(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        drugRepeatEnd = Date(viewModel.getDrug().occurrence.repeatEnd)
    }

    private fun initListeners() {
        drugOccurrencesDate.setOnClickListener {
            showStartDatePickerDialog()
        }

        drugOccurrencesTime.setOnClickListener {
//            showRepeatStartDialog()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = drugIntakeTime.time
            showTimePickerDialog(
                calendar[Calendar.HOUR_OF_DAY],
                calendar[Calendar.MINUTE]
            ) { hourOfDay, minute ->
                viewModel.setDrugRepeatStartTime(hourOfDay, minute)
                setTimeLabel(hourOfDay, minute)
            }
        }

        repeatEndDateTV.setOnClickListener {
            showEndDatePickerDialog()
        }

        newDrugName.setOnLongClickListener {
            SnackBar.showToastBar(this, viewModel.getDrug().drugName)
            return@setOnLongClickListener true
        }

        _addDrug.setOnClickListener {
            if (isDataValid()) {
                viewModel.totalDose = _drugDosageET.text.toString().toFloat()
                addOrEditDrug()
            }
        }

        _goBackBtn.setOnClickListener {
            finish()
        }

        val dosages = resources.getStringArray(R.array.drug_dosage)
        //  set first dosage measurement type as default
        viewModel.dosageMeasurementType = dosages[0]
        _drugDosageLabelTV.setOnClickListener {
            showPickerDialog(getString(R.string.dosageTitle), dosages) { dosage ->
                _drugDosageLabelTV.text = dosage
                viewModel.dosageMeasurementType = dosage
            }
        }

        drugRefillSwitch.setOnClickListener {
            setRefillVisibility(drugRefillSwitch.isChecked)
        }

        //  get numbers between min and max as string
        val dataset = IntRange(DbConstants.minRefill, DbConstants.maxRefill).map { it.toString() }
            .toTypedArray()
        drugRefillWhenIHaveLeft.setOnClickListener {
            showPickerDialog(getString(R.string.refillReminderTitle), dataset) { refill ->
                viewModel.refillReminder = refill.toInt()
                val currentRemaining = "When I have ${viewModel.refillReminder} meds remaining"
                drugRefillWhenIHaveLeft.text = currentRemaining
            }
        }

        _drugFrequencyContainer.setOnClickListener {
            if (viewModel.repeatOnEnum == DrugOccurrenceViewModel.RepeatOn.NO_REPEAT) {
                showDrugFreqDialog()
            } else {
                chooseRepeatFrequency()
            }
        }

        drugRefillReminderTime.setOnClickListener {
            val selectedHour = viewModel.getRefillReminderHour()
            val selectedMinute = viewModel.getRefillReminderMinute()
            showTimePickerDialog(selectedHour, selectedMinute) { hourOfDay, minute ->
                viewModel.refillReminderTime = getStringTime(hourOfDay, minute)
                drugRefillReminderTime.text = viewModel.refillReminderTime
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
            optionSelected = { option -> optionSelected(option) }
        ).apply {
            show(supportFragmentManager, DrugPickerDialogFragment.TAG)
        }
    }

    private fun showDrugFreqDialog() {
        DrugFrequencyDialogFragment(
            setNoRepeat = {
                updateRepeat(DrugOccurrenceViewModel.RepeatOn.NO_REPEAT)
                repeatOnValue = DbConstants.repeatOnDefaultValue
            },
            chooseRepeatFrequency = { chooseRepeatFrequency() }).apply {
            show(supportFragmentManager, DrugFrequencyDialogFragment.TAG)
        }
    }

    private fun updateRepeat(repeatEnum: DrugOccurrenceViewModel.RepeatOn) {
        viewModel.repeatOnEnum = repeatEnum
        _drugFrequencyTV.text = viewModel.convertRepeatEnumToString(viewModel.repeatOnEnum)
    }

    private fun showRepeatStartDialog() {
        DrugStartRepeatDialogFragment(
            viewModel.repeatStartTime,
            doneCallback = { mutableList ->
                viewModel.repeatStartTime = mutableList
            }
        ).apply {
            show(supportFragmentManager, DrugStartRepeatDialogFragment.TAG)
        }
    }

    private fun chooseRepeatFrequency() {
        DrugFrequencyRepeatDialogFragment(
            setRepeat = { repeatOn, freqValue ->
                updateRepeat(repeatOn)
                repeatOnValue = freqValue
            },
            backPressCallback = { showDrugFreqDialog() },
            weeklyCallback = { repeatOn, freqValue -> chooseDaysOfWeek(repeatOn, freqValue) },
            defaultValue = repeatOnValue,
            defaultFreqValue = viewModel.repeatOnEnum
        ).apply {
            show(supportFragmentManager, DrugFrequencyRepeatDialogFragment.TAG)
        }
    }

    private fun chooseDaysOfWeek(repeat: DrugOccurrenceViewModel.RepeatOn, freqValue: Int) {
        DrugFrequencyWeeklyDialogFragment(
            daysCheck = viewModel.repeatCheckWeekdays,
            doneCallback = { daysCheck ->
                updateRepeat(repeat)
                viewModel.repeatCheckWeekdays = daysCheck
                repeatOnValue = freqValue
            },
            backCallback = { chooseRepeatFrequency() }
        ).apply {
            show(supportFragmentManager, DrugFrequencyWeeklyDialogFragment.TAG)
        }
    }

    private fun initViews() {
        newDrugName = findViewById(R.id.do_drug_name)
        newDrugName.text = viewModel.getDrug().drugName
        drugOccurrencesDate = findViewById(R.id.do_repeat_start)
        drugOccurrencesTime = findViewById(R.id.do_repeat_time)
        repeatEndDateTV = findViewById(R.id.do_repeat_end)
        _drugDosageET = findViewById(R.id.do_dosage)
        _drugDosageLabelTV = findViewById(R.id.do_dosage_label)

        drugRefillSwitch = findViewById(R.id.do_refill_switch)
        drugRefillCurrentAmount = findViewById(R.id.do_current_meds)
        drugRefillWhenIHaveLeft = findViewById(R.id.do_remaining_meds)
        drugRefillReminderTime = findViewById(R.id.do_refill_reminder_time)
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
        if (isInEditMode && firstClickOnLabel) {
            calTime = drugIntakeTime
            //  set isInEditMode to false so we won't enter this if again (and then the user
            //  won't br able to update intake date)
            firstClickOnLabel = false
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = drugIntakeTime.time
            DateUtils.setCalendarTime(calendar, hour, minutes)
            calTime = calendar.time
        }

        drugOccurrencesTime.text = formatter.format(calTime)
        drugIntakeTime.time = calTime.time
    }

    private fun setRepeatStartDateLabel(day: Int, month: Int, year: Int) {
        val sdf = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        val calDate: Date
        if (isInEditMode && firstClickOnLabel) {
            calDate = drugIntakeTime
            //  set isInEditMode to false so we won't enter this if again (and then the user
            //  won't br able to update intake time)
            firstClickOnLabel = false
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = drugIntakeTime.time
            calendar.set(year, month, day)
            calDate = calendar.time
        }

        // Display Selected date in textbox
        drugOccurrencesDate.text = sdf.format(calDate)
        drugIntakeTime.time = calDate.time
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
        startCalendar.timeInMillis = drugIntakeTime.time
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
                    drugRepeatEnd = cal.time
                    viewModel.hasRepeatEnd = true
                },
                year,
                month,
                day
            )
        dpd.datePicker.minDate = endCalendar.timeInMillis
        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, which ->
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                viewModel.hasRepeatEnd = false
                repeatEndDateTV.text = getString(R.string.none)
            }
        }
        dpd.show()
    }

    private fun setRepeatEndDateLabel(day: Int, month: Int, year: Int) {
        val sdf = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        val calDate: Date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = drugIntakeTime.time
        calendar.set(year, month, day)
        calDate = calendar.time

        // Display Selected date in textbox
        repeatEndDateTV.text = sdf.format(calDate)
    }

    private fun showStartDatePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = drugIntakeTime.time
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd =
            DatePickerDialog(
                this,
                { _, yearSelected, monthOfYear, dayOfMonth ->
                    viewModel.setDrugRepeatStartDate(yearSelected, monthOfYear, dayOfMonth)
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
        if (viewModel.hasRepeatEnd && DateUtils.isDateBefore(drugRepeatEnd, selectedDate.time)) {
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
        if (drugRefillSwitch.isChecked) {
            viewModel.setDrugRefill(
                drugRefillCurrentAmount.text.toString().toInt(),
                viewModel.refillReminderTime
            )
        } else {
            viewModel.removeDrugRefill()
        }

        viewModel.updateDrugDosage(_drugDosageET.text.toString().toFloat())
        //  if it's in edit mode and the user chose to edit all occurrences - go to update
        if (isInEditMode
            && intent.getBooleanExtra(DbConstants.EDIT_ONLY_FUTURE_OCCURRENCES, false)
        ) {
            //edit drug
            viewModel.updateDrugOccurrence(
                loggedUserObject,
                repeatOnValue,
                drugRepeatEnd,
                this
            )
        } else {
            //  otherwise, add a new drug
            viewModel.addNewDrugToUser(
                loggedUserObject,
                repeatOnValue,
                drugRepeatEnd,
                this
            )
        }
    }

    private fun isDataValid(): Boolean {
        var valid = true
        if (drugRefillSwitch.isChecked && drugRefillCurrentAmount.text.isNullOrEmpty()) {
            valid = false
            drugRefillCurrentAmount.error = getString(R.string.invalidRefill)
            drugRefillCurrentAmount.requestFocus()
        }

        return valid
    }
}