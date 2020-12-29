package com.example.piller.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.models.Drug
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
    private lateinit var drugRepeatSpinner: Spinner
    private lateinit var drugRepeatContainer: ConstraintLayout
    private lateinit var drug: Drug
    private lateinit var currentProfileName: String
    private lateinit var loggedEmail: String
    private lateinit var viewModel: DrugOccurrenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drug_occurrence)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drug = intent.getParcelableExtra(DbConstants.DRUG_OBJECT)!!
        currentProfileName = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        loggedEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        viewModel = ViewModelProvider(this).get(DrugOccurrenceViewModel::class.java)
        initViews()
        initViewsInitialData()
        initListeners()
        initViewModelObservers()
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
                drug.drug_name
            )
            return@setOnLongClickListener true
        }
    }

    private fun initViews() {
        newDrugName = findViewById(R.id.ndo_new_drug_name)
        newDrugName.text = drug.drug_name
        drugOccurrencesDate = findViewById(R.id.ndo_first_occurrence_date)
        drugOccurrencesTime = findViewById(R.id.ndo_first_occurrence_time)
        drugRepeatSpinner = findViewById(R.id.ndo_repeat_spinner)
        drugRepeatContainer = findViewById(R.id.ndo_repeat_container)
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
                    drug
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