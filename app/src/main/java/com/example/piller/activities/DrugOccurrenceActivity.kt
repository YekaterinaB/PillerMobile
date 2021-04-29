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
    private var repeatOnEnum = DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
    private var repeatOnValue = 0
    private var isInEditMode = false
    private var firstClickOnLabel = true
    private var refillReminder = 20
    private var refillReminderTime = "11:00"
    private var _hasRepeatEnd = false
    private var _repeatCheckWeekdays: Array<Boolean> =
        arrayOf(false, false, false, false, false, false, false)
    private var _repeatStartTime: MutableList<Calendar> = mutableListOf(Calendar.getInstance())
    private var _dosageMeasurementType: String = ""

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
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "0") {
                    SnackBar.showToastBar(
                        this@DrugOccurrenceActivity,
                        "Please enter a valid dosage"
                    )
                    _drugDosageET.setText("1")
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
            _addDrug.text = "Save changes"
            _dosageMeasurementType = viewModel.getDrug().dose.measurementType
        }
    }

    private fun initRepeatCheckedWeekdays() {
        val drug = viewModel.getDrug()
        updateRepeat(occurrenceRepeatToEnumAndUpdate())
        if (drug.occurrence.repeatWeek > 0) {
            for (day in drug.occurrence.repeatWeekday) {
                _repeatCheckWeekdays[day - 1] = true
            }
        }
    }

    private fun occurrenceRepeatToEnumAndUpdate(): DrugOccurrenceViewModel.RepeatOn {
        val drug = viewModel.getDrug()
        val repeat: DrugOccurrenceViewModel.RepeatOn
        when {
            drug.occurrence.repeatDay > 0 -> {
                repeatOnValue = drug.occurrence.repeatDay
                repeat = DrugOccurrenceViewModel.RepeatOn.DAY
            }
            drug.occurrence.repeatWeek > 0 -> {
                repeatOnValue = drug.occurrence.repeatWeek
                repeat = DrugOccurrenceViewModel.RepeatOn.WEEK
            }
            drug.occurrence.repeatMonth > 0 -> {
                repeatOnValue = drug.occurrence.repeatMonth
                repeat = DrugOccurrenceViewModel.RepeatOn.MONTH
            }
            drug.occurrence.repeatYear > 0 -> {
                repeatOnValue = drug.occurrence.repeatYear
                repeat = DrugOccurrenceViewModel.RepeatOn.YEAR
            }
            else -> repeat = DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
        }

        return repeat
    }


    private fun initAllIntentExtras() {
        initUserObject(intent)
        initDrugIntakeTime(intent.getLongExtra(DbConstants.INTAKE_DATE, -1))
        val drug = intent.getParcelableExtra<DrugObject>(DbConstants.DRUG_OBJECT)!!
        drug.occurrence.repeatStart = drugIntakeTime.time
        viewModel.setDrug(drug)
    }

    private fun initDrugIntakeTime(intakeFromIntent: Long) {
        if (intakeFromIntent > -1) {
            isInEditMode = true
            drugIntakeTime.time = intakeFromIntent
        } else {
            val calendar = Calendar.getInstance()
            calendar[Calendar.SECOND] = 0
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
            refillReminder = refill.pillsBeforeReminder
            refillReminderTime = refill.reminderTime
        }
    }

    private fun initRepeatEnd() {
        val currentDrug = viewModel.getDrug()
        if (currentDrug.occurrence.repeatEnd > 0) {
            _hasRepeatEnd = true
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = drugRepeatEnd.time
            setRepeatEndDateLabel(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR)
            )
        }
    }

    private fun updateDosage(measurementType: String) {
        viewModel.setDrugDosage(measurementType, _drugDosageET.text.toString().toFloat())
    }

    private fun initViewModelObservers() {
        viewModel.snackBarMessage.observe(this, Observer { message ->
            SnackBar.showToastBar(this, message)
        })

        viewModel.addedDrugSuccess.observe(this, Observer { added ->
            if (added) {
                goBackToCalendarActivity("New drug added!")
            }
        })

        viewModel.updatedDrugSuccess.observe(this, Observer { added ->
            if (added) {
                goBackToCalendarActivity("Drug occurrence was updated!")
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
            if (_hasRepeatEnd) {
                viewModel.setDrugRepeatEndDate(drugRepeatEnd)
            } else {
                //  if the user didn't choose repeat end - then set it to 0
                viewModel.removeDrugRepeatEndDate()
            }
            if (isDataValid()) {
                updateDosage(_dosageMeasurementType)
                addOrEditDrug()
            }
        }

        _goBackBtn.setOnClickListener {
            finish()
        }

        val dosages = resources.getStringArray(R.array.drug_dosage)
        _dosageMeasurementType = dosages[0]
        _drugDosageLabelTV.setOnClickListener {
            showPickerDialog("Dosage", dosages) { dosage ->
                _drugDosageLabelTV.text = dosage
                _dosageMeasurementType = dosage
            }
        }

        drugRefillSwitch.setOnClickListener {
            setRefillVisibility(drugRefillSwitch.isChecked)
        }

        //  get numbers between 1 and 100 as string
        val dataset = IntRange(1, 101).map { it.toString() }.toTypedArray()
        drugRefillWhenIHaveLeft.setOnClickListener {
            showPickerDialog("Refill reminder", dataset) { refill ->
                refillReminder = refill.toInt()
                val currentRemaining = "When I have $refillReminder meds remaining"
                drugRefillWhenIHaveLeft.text = currentRemaining
            }
        }

        _drugFrequencyContainer.setOnClickListener {
            if (repeatOnEnum == DrugOccurrenceViewModel.RepeatOn.NO_REPEAT) {
                showDrugFreqDialog()
            } else {
                chooseRepeatFrequency()
            }
        }

        drugRefillReminderTime.setOnClickListener {
            val selectedHour = refillReminderTime.substring(0, 2).toInt()
            val selectedMinute = refillReminderTime.substring(3).toInt()
            showTimePickerDialog(selectedHour, selectedMinute) { hourOfDay, minute ->
                val stringHour = if (hourOfDay < 10) "0$hourOfDay" else "$hourOfDay"
                val stringMinute = if (minute < 10) "0$minute" else "$minute"
                refillReminderTime = "$stringHour:$stringMinute"
                drugRefillReminderTime.text = refillReminderTime
            }
        }
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
                repeatOnValue = 0
            },
            chooseRepeatFrequency = { chooseRepeatFrequency() }).apply {
            show(supportFragmentManager, DrugFrequencyDialogFragment.TAG)
        }
    }

    private fun updateRepeat(repeatEnum: DrugOccurrenceViewModel.RepeatOn) {
        repeatOnEnum = repeatEnum
        _drugFrequencyTV.text = viewModel.convertRepeatEnumToString(repeatOnEnum)
    }

    private fun showRepeatStartDialog() {
        DrugStartRepeatDialogFragment(
            _repeatStartTime,
            doneCallback = { mutableList ->
                _repeatStartTime = mutableList
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
            defaultFreqValue = repeatOnEnum
        ).apply {
            show(supportFragmentManager, DrugFrequencyRepeatDialogFragment.TAG)
        }
    }

    private fun chooseDaysOfWeek(repeat: DrugOccurrenceViewModel.RepeatOn, freqValue: Int) {
        DrugFrequencyWeeklyDialogFragment(
            daysCheck = _repeatCheckWeekdays,
            doneCallback = { daysCheck ->
                updateRepeat(repeat)
                _repeatCheckWeekdays = daysCheck
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
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
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
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
        DateUtils.setCalendarTime(endCalendar, 0, 0, 0)
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
                    _hasRepeatEnd = true
                },
                year,
                month,
                day
            )
        dpd.datePicker.minDate = endCalendar.timeInMillis
        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { _, which ->
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                _hasRepeatEnd = false
                repeatEndDateTV.text = getString(R.string.none)
            }
        }
        dpd.show()
    }

    private fun setRepeatEndDateLabel(day: Int, month: Int, year: Int) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
        if (_hasRepeatEnd && DateUtils.isDateBefore(drugRepeatEnd, selectedDate.time)) {
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
                refillReminder,
                refillReminderTime
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
                _loggedUserObject,
                repeatOnEnum,
                repeatOnValue,
                _repeatCheckWeekdays,
                this
            )
        } else {
            //  otherwise, add a new drug
            viewModel.addNewDrugToUser(
                _loggedUserObject,
                repeatOnEnum,
                repeatOnValue,
                _repeatCheckWeekdays,
                this
            )
        }
    }

    private fun isDataValid(): Boolean {
        var valid = true
        if (drugRefillSwitch.isChecked && drugRefillCurrentAmount.text.isNullOrEmpty()) {
            valid = false
            drugRefillCurrentAmount.error = "Please enter the amount of meds you currently have"
            drugRefillCurrentAmount.requestFocus()
        }

        return valid
    }
}