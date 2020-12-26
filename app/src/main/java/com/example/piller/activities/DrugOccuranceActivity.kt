package com.example.piller.activities


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.piller.R
import com.example.piller.models.Drug
import com.example.piller.utilities.DbConstants
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_NEW_DRUG = "new drug"

class DrugOccurrencesActivity : AppCompatActivity() {
    //  todo check if already has the drug by rxcui
    //  todo - add X button on top so the user will be able to cancel
    private lateinit var newDrugName: TextView
    private lateinit var drugOccurrencesDate: TextView
    private lateinit var drugOccurrencesTime: TextView
    private lateinit var drugRepeatSpinner: Spinner
    private lateinit var drugRepeatContainer: ConstraintLayout
    private lateinit var drug:Drug
    private lateinit var currentProfile: String
    private lateinit var loggedEmail: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drug.drug_name = intent.getStringExtra(DbConstants.FULL_DRUG_NAME)!!
        drug.rxcui = intent.getIntExtra(DbConstants.DRUG_RXCUI, 0)
        currentProfile = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        loggedEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        setContentView(R.layout.activity_add_new_drug)
        initViews()
        initViewsInitialData()
        initListeners()

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

//        newDrugName.setOnLongClickListener {
//            viewModel.newDrug.value?.drug_name?.let { drugName ->
//                SnackBar.showToastBar(
//                    this,
//                    drugName
//                )
//            }
//            return@setOnLongClickListener true
//        }
    }

    private fun initViews() {
        newDrugName = findViewById(R.id.ndo_new_drug_name)
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
            this.let {
                TimePickerDialog(
                    it,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        setTimeLabel(hourOfDay, minute)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
            }

        tpd.show()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd =
            this.let {
                DatePickerDialog(
                    it,
                    DatePickerDialog.OnDateSetListener { _, yearSelected, monthOfYear, dayOfMonth ->
                        setDateLabel(dayOfMonth, monthOfYear, yearSelected)
                    },
                    year,
                    month,
                    day
                )
            }

        dpd.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //  todo get email and name
        return when (item.itemId) {
            R.id.ndo_menu_add_drug -> {
//                viewModel.addNewDrugToUser(
//                    profileViewModel.getCurrentEmail(),
//                    profileViewModel.getCurrentProfileName()
//                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.new_drug_occurrence_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param newDrug Drug to add
         * @return A new instance of fragment NewDrugOccurrencesFragment.
         */
        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(newDrug: Drug) =
//            NewDrugOccurrencesFragment().apply {
//                arguments = Bundle().apply {
//                    putParcelable(ARG_NEW_DRUG, newDrug)
//                }
//            }
    }
}