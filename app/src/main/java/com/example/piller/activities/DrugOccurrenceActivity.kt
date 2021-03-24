package com.example.piller.activities

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
    private lateinit var drugOccurrencesDate: TextView
    private lateinit var drugOccurrencesTime: TextView
    private lateinit var drugRepeatsOnEditText: EditText
    private lateinit var drugDosageET: EditText
    private lateinit var drugDosageList: RecyclerView
    private lateinit var drugShouldRepeatSpinner: Spinner
    private lateinit var drugRepeatOnSpinner: Spinner
    private lateinit var drugRepeatContainer: ConstraintLayout
    private lateinit var drugWeekdayContainer: ConstraintLayout
    private lateinit var weekdaySundayCB: CheckboxWithTextInside
    private lateinit var weekdayMondayCB: CheckboxWithTextInside
    private lateinit var weekdayTuesdayCB: CheckboxWithTextInside
    private lateinit var weekdayWednesdayCB: CheckboxWithTextInside
    private lateinit var weekdayThursdayCB: CheckboxWithTextInside
    private lateinit var weekdayFridayCB: CheckboxWithTextInside
    private lateinit var weekdaySaturdayCB: CheckboxWithTextInside
    private lateinit var setRepeatEnd: Switch
    private lateinit var repeatEndDateTV: TextView
    private lateinit var currentProfileName: String
    private lateinit var loggedEmail: String
    private lateinit var viewModel: DrugOccurrenceViewModel
    private var drugIntakeTime: Date = Date()
    private var drugRepeatEnd: Date = Date()
    private var repeatOnEnum = DrugOccurrenceViewModel.RepeatOn.NO_REPEAT
    private var isInEditMode = false
    private var firstClickOnLabel = true

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
        val drugShouldRepeat = resources.getStringArray(R.array.drug_should_repeat)

        val drugShouldRepeatAdapter = ArrayAdapter(
            this,
            R.layout.drug_repeat_option, drugShouldRepeat
        )
        drugShouldRepeatSpinner.adapter = drugShouldRepeatAdapter

        val drugRepeatOptions = resources.getStringArray(R.array.drug_repeat_options)
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
        drugDosageList.adapter = DosageAdapter(dosages) { dosage -> updateDosage(dosage) }
        drugDosageList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
        if (drugRepeatEnd.time > 0) {
            calendar.timeInMillis = drugRepeatEnd.time
            setRepeatEnd.isChecked = true
            repeatEndDateTV.visibility = View.VISIBLE
        } else {
            drugRepeatEnd = Date(DateUtils.getTomorrowDateInMillis(Date()))
        }
        calendar.timeInMillis = DateUtils.getTomorrowDateInMillis(Date())
        setRepeatEndDateLabel(
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.YEAR)
        )

    }

    private fun initListeners() {
        drugOccurrencesDate.setOnClickListener {
            showStartDatePickerDialog()
        }

        drugOccurrencesTime.setOnClickListener {
            showTimePickerDialog()
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

        weekdaySundayCB.setOnClickListener {
            setWeekdayChecked(1, weekdaySundayCB.isChecked)
        }
        weekdayMondayCB.setOnClickListener {
            setWeekdayChecked(2, weekdayMondayCB.isChecked)
        }
        weekdayTuesdayCB.setOnClickListener {
            setWeekdayChecked(3, weekdayTuesdayCB.isChecked)
        }
        weekdayWednesdayCB.setOnClickListener {
            setWeekdayChecked(4, weekdayWednesdayCB.isChecked)
        }
        weekdayThursdayCB.setOnClickListener {
            setWeekdayChecked(5, weekdayThursdayCB.isChecked)
        }
        weekdayFridayCB.setOnClickListener {
            setWeekdayChecked(6, weekdayFridayCB.isChecked)
        }
        weekdaySaturdayCB.setOnClickListener {
            setWeekdayChecked(7, weekdaySaturdayCB.isChecked)
        }

        setRepeatEnd.setOnClickListener {
            setRepeatEndDateVisibility(setRepeatEnd.isChecked)
        }
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
        weekdaySundayCB = findViewById(R.id.ndo_weekday_sun)
        weekdayMondayCB = findViewById(R.id.ndo_weekday_mon)
        weekdayTuesdayCB = findViewById(R.id.ndo_weekday_tue)
        weekdayWednesdayCB = findViewById(R.id.ndo_weekday_wed)
        weekdayThursdayCB = findViewById(R.id.ndo_weekday_thu)
        weekdayFridayCB = findViewById(R.id.ndo_weekday_fri)
        weekdaySaturdayCB = findViewById(R.id.ndo_weekday_sat)
        drugRepeatsOnEditText = findViewById(R.id.ndo_repeat_every_number)
        drugRepeatOnSpinner = findViewById(R.id.ndo_repeat_options_spinner)
        setRepeatEnd = findViewById(R.id.ndo_has_repeat_end)
        repeatEndDateTV = findViewById(R.id.ndo_repeat_end_date)
        drugDosageET = findViewById(R.id.ndo_dosage_number)
        drugDosageList = findViewById(R.id.ndo_dosage_list)
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

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = drugIntakeTime.time
        val tpd =
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    viewModel.setDrugRepeatStartTime(hourOfDay, minute)
                    setTimeLabel(hourOfDay, minute)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.new_drug_occurrence_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //  todo set drug occurrences before sending to server!!
        return when (item.itemId) {
            R.id.ndo_menu_add_drug -> {
                //  if the user didn't choose repeat end - then set it to 0
                if (setRepeatEnd.isChecked) {
                    viewModel.setDrugRepeatEndDate(drugRepeatEnd)
                } else {
                    viewModel.removeDrugRepeatEndDate()
                }

                addOrEditDrug()
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