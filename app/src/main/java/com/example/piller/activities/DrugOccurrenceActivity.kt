package com.example.piller.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.EventInterpreter
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.customWidgets.CheckboxWithTextInside
import com.example.piller.listAdapters.DosageAdapter
import com.example.piller.models.DrugObject
import com.example.piller.utilities.DateUtils
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugOccurrenceViewModel
import kotlinx.android.synthetic.main.activity_drug_occurrence.*
import java.text.SimpleDateFormat
import java.util.*


class DrugOccurrenceActivity : AppCompatActivity() {
    private lateinit var newDrugName: TextView
    private lateinit var medsLeftLabel: TextView
    private lateinit var drugOccurrencesDate: TextView
    private lateinit var drugOccurrencesTime: TextView
    private lateinit var drugRefillSwitch: Switch
    private lateinit var drugRefillCurrentAmount: EditText
    private lateinit var drugRefillWhenIHaveLeft: TextView
    private lateinit var drugRefillReminderTime: TextView
    private lateinit var drugRefillWhatTime: TextView
    private lateinit var drugRepeatsOnEditText: EditText
    private lateinit var drugDosageET: EditText
    private lateinit var drugDosageList: RecyclerView
    private lateinit var drugShouldRepeatSpinner: Spinner
    private lateinit var drugRepeatOnSpinner: Spinner
    private lateinit var drugRepeatContainer: ConstraintLayout
    private lateinit var drugWeekdayContainer: ConstraintLayout
    private lateinit var setRepeatEnd: Switch
    private lateinit var repeatEndDateTV: TextView
    private lateinit var currentProfileName: String
    private lateinit var loggedEmail: String
    private lateinit var viewModel: DrugOccurrenceViewModel
    private lateinit var drugRepeatOptions: Array<String>
    private lateinit var drugShouldRepeat: Array<String>
    private var weekDaysCBs = mutableListOf<CheckboxWithTextInside>()
    private var drugIntakeTime: Date = Date()
    private var drugRepeatEnd: Date = Date()
    private var repeatOnEnum = DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
    private var isInEditMode = false
    private var firstClickOnLabel = true
    private var refillReminder = 20
    private var refillReminderTime = "11:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drug_occurrence)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel = ViewModelProvider(this).get(DrugOccurrenceViewModel::class.java)
        initAllIntentExtras()
        initViews()
        initViewsInitialData()
        initListeners()
        initViewModelObservers()
        initSpinners()
        initEditModeData()
    }

    private fun initEditModeData() {
        if (isInEditMode) {
            drugDosageET.setText(viewModel.getDrug().dose.totalDose.toString())
            initRefillForEditMode()
            initRepeatOnEdit(drugRepeatOptions, drugShouldRepeat)
            initRepeatEnd()
        }
    }

    private fun initAllIntentExtras() {
        currentProfileName = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        loggedEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        initDrugIntakeTime(intent.getLongExtra(DbConstants.INTAKE_DATE, -1))
        setDrug(intent.getParcelableExtra(DbConstants.DRUG_OBJECT)!!)
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

    private fun setDrug(drug: DrugObject) {
        drug.occurrence.repeatStart = drugIntakeTime.time
        viewModel.setDrug(drug)
    }

    private fun initSpinners() {
        drugShouldRepeat = resources.getStringArray(R.array.drug_should_repeat)

        val drugShouldRepeatAdapter = ArrayAdapter(
            this,
            R.layout.drug_repeat_option, drugShouldRepeat
        )
        drugShouldRepeatSpinner.adapter = drugShouldRepeatAdapter

        drugRepeatOptions = resources.getStringArray(R.array.drug_repeat_options)
        val drugRepeatAdapter = ArrayAdapter(
            this,
            R.layout.drug_repeat_option, drugRepeatOptions
        )
        drugRepeatOnSpinner.adapter = drugRepeatAdapter

        drugShouldRepeatSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (drugShouldRepeat[position].contains("Repeat on")) {
                    drugRepeatContainer.visibility = View.VISIBLE
                } else {
                    drugRepeatContainer.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        drugRepeatOnSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                when (drugRepeatOptions[position]) {
                    "Day" -> {
                        repeatOnEnum = DrugOccurrenceViewModel.RepeatOn.DAY
                        drugWeekdayContainer.visibility = View.GONE
                    }
                    "Week" -> {
                        drugWeekdayContainer.visibility = View.VISIBLE
                        repeatOnEnum = DrugOccurrenceViewModel.RepeatOn.WEEK
                    }
                    "Month" -> {
                        repeatOnEnum = DrugOccurrenceViewModel.RepeatOn.MONTH
                        drugWeekdayContainer.visibility = View.GONE
                    }
                    "Year" -> {
                        repeatOnEnum = DrugOccurrenceViewModel.RepeatOn.YEAR
                        drugWeekdayContainer.visibility = View.GONE
                    }
                    else -> {
                        repeatOnEnum = DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val dosages = resources.getStringArray(R.array.drug_dosage)
        var initialDosageMeasurementType = dosages[0]
        if (isInEditMode) {
            initialDosageMeasurementType = viewModel.getDrug().dose.measurementType
        }
        drugDosageList.adapter =
            DosageAdapter(dosages, initialDosageMeasurementType) { dosage -> updateDosage(dosage) }
        drugDosageList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
            setRepeatEnd.isChecked = true
            setRepeatEndDateVisibility(true)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = drugRepeatEnd.time
            repeatEndDateTV.visibility = View.VISIBLE
            setRepeatEndDateLabel(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR)
            )
        }
    }

    private fun initRepeatOnEdit(
        drugRepeatOptions: Array<String>,
        drugShouldRepeat: Array<String>
    ) {
        val currentDrug = viewModel.getDrug()
        val eventInterpreter = EventInterpreter()
        if (!eventInterpreter.isOnlyRepeatOnce(currentDrug.occurrence)) {
            //  select correct repeat option and set the repeat value
            when {
                currentDrug.occurrence.repeatDay != 0 -> {
                    drugRepeatOnSpinner.setSelection(drugRepeatOptions.indexOf("Day"))
                    drugRepeatsOnEditText.setText(currentDrug.occurrence.repeatDay.toString())
                }

                currentDrug.occurrence.repeatWeek != 0 -> {
                    drugRepeatOnSpinner.setSelection(drugRepeatOptions.indexOf("Week"))
                    drugRepeatsOnEditText.setText(currentDrug.occurrence.repeatWeek.toString())
                    selectWeekDayRepeats(currentDrug.occurrence.repeatWeekday)
                }
                currentDrug.occurrence.repeatMonth != 0 -> {
                    drugRepeatOnSpinner.setSelection(drugRepeatOptions.indexOf("Month"))
                    drugRepeatsOnEditText.setText(currentDrug.occurrence.repeatMonth.toString())
                }

                currentDrug.occurrence.repeatYear != 0 -> {
                    drugRepeatOnSpinner.setSelection(drugRepeatOptions.indexOf("Year"))
                    drugRepeatsOnEditText.setText(currentDrug.occurrence.repeatYear.toString())
                }
            }

            //  show the repeat (repeat/ does not repeat)
            drugShouldRepeatSpinner.setSelection(drugShouldRepeat.indexOf("Repeat on…"))
        }
    }

    private fun selectWeekDayRepeats(repeatWeekday: List<Int>) {
        for (day in repeatWeekday) {
            weekDaysCBs[day - 1].isChecked = true
            setWeekdayChecked(day, true)
        }
    }

    private fun updateDosage(measurementType: String) {
        viewModel.setDrugDosage(measurementType, drugDosageET.text.toString().toFloat())
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
        val intent = Intent(this@DrugOccurrenceActivity, CalendarActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(DbConstants.LOGGED_USER_EMAIL, loggedEmail)
        intent.putExtra(DbConstants.LOGGED_USER_NAME, currentProfileName)
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
        if (viewModel.getDrug().occurrence.repeatEnd == 0L) {
            calendar.timeInMillis = DateUtils.getTomorrowDateInMillis(Date())

            setRepeatEndDateLabel(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR)
            )
        }
    }

    private fun initListeners() {
        drugOccurrencesDate.setOnClickListener {
            showStartDatePickerDialog()
        }

        drugOccurrencesTime.setOnClickListener {
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
            SnackBar.showToastBar(
                this,
                viewModel.getDrug().drugName
            )
            return@setOnLongClickListener true
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "0") {
                    SnackBar.showToastBar(
                        this@DrugOccurrenceActivity,
                        "Please enter a value between 1 and 99"
                    )
                    drugRepeatsOnEditText.setText("1")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        drugRepeatsOnEditText.addTextChangedListener(textWatcher)

        for ((index, checkbox) in weekDaysCBs.withIndex()) {
            checkbox.setOnClickListener {
                setWeekdayChecked(index + 1, checkbox.isChecked)
            }
        }

        setRepeatEnd.setOnClickListener {
            setRepeatEndDateVisibility(setRepeatEnd.isChecked)
        }

        drugRefillSwitch.setOnClickListener {
            setRefillVisibility(drugRefillSwitch.isChecked)
        }

        drugRefillWhenIHaveLeft.setOnClickListener {
            showRefillRemainingPicker()
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

    private fun showRefillRemainingPicker() {
        val numberPicker = NumberPicker(this)
        numberPicker.maxValue = 100 //  Maximum value to select
        numberPicker.minValue = 0 //    Minimum value to select
        numberPicker.value = refillReminder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setView(numberPicker)
        builder.setTitle("Refill reminder")
        builder.setMessage("How many meds do you want remaining before you get a refill reminder?")
        builder.setPositiveButton("OK") { _, _ ->
            refillReminder = numberPicker.value
            val currentRemaining = "When I have $refillReminder meds remaining"
            drugRefillWhenIHaveLeft.text = currentRemaining
        }
        builder.setNegativeButton(
            "CANCEL"
        ) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun setRepeatEndDateVisibility(shouldShow: Boolean) {
        if (shouldShow) {
            repeatEndDateTV.visibility = View.VISIBLE
        } else {
            repeatEndDateTV.visibility = View.GONE
        }
    }

    /**
    sends to viewmodel that the day is checked / not checked
    @param weekdayNumber day number, sunday is 0 and so on
    @param checked mean if day is selected
     **/
    private fun setWeekdayChecked(weekdayNumber: Int, checked: Boolean) {
        viewModel.setWeekdayChecked(weekdayNumber, checked)
    }

    private fun initViews() {
        newDrugName = findViewById(R.id.ndo_new_drug_name)
        newDrugName.text = viewModel.getDrug().drugName
        drugOccurrencesDate = findViewById(R.id.ndo_first_occurrence_date)
        drugOccurrencesTime = findViewById(R.id.ndo_first_occurrence_time)
        drugShouldRepeatSpinner = findViewById(R.id.ndo_should_repeat_spinner)
        drugRepeatContainer = findViewById(R.id.ndo_repeat_container)
        drugWeekdayContainer = findViewById(R.id.ndo_weekday_container)

        weekDaysCBs.add(findViewById(R.id.ndo_weekday_sun))
        weekDaysCBs.add(findViewById(R.id.ndo_weekday_mon))
        weekDaysCBs.add(findViewById(R.id.ndo_weekday_tue))
        weekDaysCBs.add(findViewById(R.id.ndo_weekday_wed))
        weekDaysCBs.add(findViewById(R.id.ndo_weekday_thu))
        weekDaysCBs.add(findViewById(R.id.ndo_weekday_fri))
        weekDaysCBs.add(findViewById(R.id.ndo_weekday_sat))

        drugRepeatsOnEditText = findViewById(R.id.ndo_repeat_every_number)
        drugRepeatOnSpinner = findViewById(R.id.ndo_repeat_options_spinner)
        setRepeatEnd = findViewById(R.id.ndo_has_repeat_end)
        repeatEndDateTV = findViewById(R.id.ndo_repeat_end_date)
        drugDosageET = findViewById(R.id.ndo_dosage_number)
        drugDosageList = findViewById(R.id.ndo_dosage_list)

        drugRefillSwitch = findViewById(R.id.ndo_refill_switch)
        drugRefillCurrentAmount = findViewById(R.id.ndoCurrentMedsET)
        drugRefillWhenIHaveLeft = findViewById(R.id.ndoRemainingMeds)
        drugRefillReminderTime = findViewById(R.id.ndoRefillReminderTime)
        drugRefillWhatTime = findViewById(R.id.ndoWhatTimeTv)
        medsLeftLabel = findViewById(R.id.ndo_enter_meds_label)
    }

    private fun setRefillVisibility(show: Boolean) {
        if (show) {
            drugRefillWhenIHaveLeft.visibility = View.VISIBLE
            drugRefillReminderTime.visibility = View.VISIBLE
            drugRefillWhatTime.visibility = View.VISIBLE
            drugRefillCurrentAmount.visibility = View.VISIBLE
            medsLeftLabel.visibility = View.VISIBLE
        } else {
            drugRefillWhenIHaveLeft.visibility = View.GONE
            drugRefillReminderTime.visibility = View.GONE
            drugRefillWhatTime.visibility = View.GONE
            drugRefillCurrentAmount.visibility = View.GONE
            medsLeftLabel.visibility = View.GONE
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
                },
                year,
                month,
                day
            )
        dpd.datePicker.minDate = endCalendar.timeInMillis
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
        if (DateUtils.isDateBefore(drugRepeatEnd, selectedDate.time)) {
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
        }
        viewModel.updateDrugDosage(drugDosageET.text.toString().toFloat())
        //  if it's in edit mode and the user chose to edit all occurrences - go to update
        if (isInEditMode
            && intent.getBooleanExtra(DbConstants.EDIT_ONLY_FUTURE_OCCURRENCES, false)
        ) {
            //edit drug
            viewModel.updateDrugOccurrence(
                loggedEmail,
                currentProfileName,
                repeatOnEnum,
                drugRepeatsOnEditText.text.toString(),
                this
            )
        } else {
            //  otherwise, add a new drug
            viewModel.addNewDrugToUser(
                loggedEmail,
                currentProfileName,
                repeatOnEnum,
                drugRepeatsOnEditText.text.toString(),
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
        } else if (repeatOnEnum == DrugOccurrenceViewModel.RepeatOn.WEEK && !isAtLeastOneDayChosen()) {
            valid = false
            SnackBar.showToastBar(this, "Please choose at least one week day")
        }

        return valid
    }

    private fun isAtLeastOneDayChosen(): Boolean =
        weekDaysCBs.any { checkBox -> checkBox.isChecked }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.new_drug_occurrence_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ndo_menu_add_drug -> {
                //  if the user didn't choose repeat end - then set it to 0
                if (setRepeatEnd.isChecked) {
                    viewModel.setDrugRepeatEndDate(drugRepeatEnd)
                } else {
                    viewModel.removeDrugRepeatEndDate()
                }

                if (isDataValid()) {
                    addOrEditDrug()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}