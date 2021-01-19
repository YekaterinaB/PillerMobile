package com.example.piller.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.customWidgets.CheckboxWithTextInside
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugOccurrenceViewModel
import java.text.SimpleDateFormat
import java.util.*

class DrugOccurrenceActivity : AppCompatActivity() {
    //  todo check if already has the drug by rxcui
    //  todo - add X button on top so the user will be able to cancel
    private lateinit var newDrugName: TextView
    private lateinit var drugOccurrencesDate: TextView
    private lateinit var drugOccurrencesTime: TextView
    private lateinit var drugRepeatsOnEditText: EditText
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
    private lateinit var repeatOnEnum: DrugOccurrenceViewModel.RepeatOn
    private lateinit var currentProfileName: String
    private lateinit var loggedEmail: String
    private lateinit var viewModel: DrugOccurrenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drug_occurrence)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        currentProfileName = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        loggedEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        viewModel = ViewModelProvider(this).get(DrugOccurrenceViewModel::class.java)
        viewModel.setDrug(intent.getParcelableExtra(DbConstants.DRUG_OBJECT)!!)
        initViews()
        initViewsInitialData()
        initListeners()
        initViewModelObservers()
        initSpinners()
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
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun initViewModelObservers() {
        viewModel.snackBarMessage.observe(this, Observer { message ->
            SnackBar.showToastBar(this, message)
        })

        viewModel.addedDrugSuccess.observe(this, Observer { added ->
            if (added) {
                SnackBar.showToastBar(this, "Drug added!")
                finish()
            }
        })
    }

    private fun initViewsInitialData() {
        //  initiate start date
        val calendar = Calendar.getInstance()
        setDateLabel(
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.YEAR)
        )

        //  initiate start time
        setTimeLabel(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    private fun initListeners() {
        drugOccurrencesDate.setOnClickListener {
            showDatePickerDialog()
        }

        drugOccurrencesTime.setOnClickListener {
            showTimePickerDialog()
        }

        newDrugName.setOnLongClickListener {
            SnackBar.showToastBar(
                this,
                viewModel.getDrug().drug_name
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
        newDrugName.text = viewModel.getDrug().drug_name
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
    }

    private fun setTimeLabel(hour: Int, minutes: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minutes)
        val calTime = calendar.time
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        drugOccurrencesTime.text = formatter.format(calTime)
    }

    private fun setDateLabel(day: Int, month: Int, year: Int) {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        calendar.set(year, month, day)
        // Display Selected date in textbox
        drugOccurrencesDate.text = sdf.format(calendar.time)
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()

        val tpd =
            TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    viewModel.setDrugRepeatStartTime(hourOfDay, minute)
                    setTimeLabel(hourOfDay, minute)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )

        tpd.show()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd =
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, yearSelected, monthOfYear, dayOfMonth ->
                    viewModel.setDrugRepeatStartDate(yearSelected, monthOfYear, dayOfMonth)
                    setDateLabel(dayOfMonth, monthOfYear, yearSelected)
                },
                year,
                month,
                day
            )

        dpd.show()
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
                viewModel.addNewDrugToUser(
                    loggedEmail,
                    currentProfileName,
                    repeatOnEnum,
                    drugRepeatsOnEditText.text.toString()
                )
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