package com.example.piller.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.utilities.DateUtils
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.models.CalendarEvent
import com.example.piller.models.DrugOccurrence
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.ImageUtils
import com.example.piller.viewModels.DrugInfoViewModel
import com.example.piller.viewModels.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*


class DrugInfoActivity : AppCompatActivity() {
    private lateinit var _viewModel: DrugInfoViewModel
    private lateinit var _profileViewModel: ProfileViewModel

    private lateinit var _drugNameTV: TextView
    private lateinit var _drugIntakeTimeTV: TextView
    private lateinit var _drugTakenCB: CheckBox
    private lateinit var _drugImageIV: ImageView

    private lateinit var _loggedEmail: String
    private lateinit var _currentProfile: String
    private lateinit var _calendarEvent: CalendarEvent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drug_info)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initIntent()
        initViewModels()
        initViews()
        initObservers()
        initListeners()
    }

    private fun initListeners() {
        _drugTakenCB.setOnClickListener {

        }
    }

    private fun initIntent() {
        _loggedEmail = intent.extras!!.getString(DbConstants.LOGGED_USER_EMAIL)!!
        _currentProfile = intent.extras!!.getString(DbConstants.LOGGED_USER_NAME)!!

        try {
            _calendarEvent = intent.extras!!.getBundle(DbConstants.CALENDAR_EVENT_BUNDLE)
                ?.getParcelable<CalendarEvent>(DbConstants.CALENDAR_EVENT)!!
        } catch (e: Exception) {
            _calendarEvent = intent.getParcelableExtra(DbConstants.CALENDAR_EVENT)!!
        }
    }

    private fun initObservers() {
        _viewModel.mutableToastError.observe(
            this,
            Observer { toastMessage ->
                toastMessage?.let { SnackBar.showToastBar(this, toastMessage) }
            })

        _viewModel.deleteSuccess.observe(
            this,
            Observer { success ->
                if (success) {
                    //  remove drug image from cache
                    ImageUtils.deleteFile(_calendarEvent.drug_rxcui.toString(), this)
                    _viewModel.deleteSuccess.value = false
                    val returnIntent = Intent()
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            })

        _viewModel.deleteFutureSuccess.observe(
            this,
            Observer { success ->
                if (success) {
                    _viewModel.deleteFutureSuccess.value = false
                    val returnIntent = Intent()
                    setResult(DbConstants.REMOVE_DRUG_FUTURE, returnIntent)
                    finish()
                }
            })

        _viewModel.drugImageBitmap.observe(
            this,
            Observer { image ->
                if (image != null) {
                    _drugImageIV.setImageBitmap(image)
                }
            })
    }

    private fun initViews() {
        _drugNameTV = findViewById(R.id.di_drug_name)
        _drugIntakeTimeTV = findViewById(R.id.di_drug_intake_time)
        _drugTakenCB = findViewById(R.id.di_drug_taken)
        _drugImageIV = findViewById(R.id.di_drug_image)

        if (!_calendarEvent.showTakenCheckBox) {
            _drugTakenCB.visibility = View.GONE
        }

        initViewsData()
    }

    private fun initViewsData() {
        _drugNameTV.text = _calendarEvent.drug_name
        setIntakeTimeTextView()
        _drugTakenCB.isChecked = _calendarEvent.is_taken
    }

    private fun setIntakeTimeTextView() {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calTime: Date

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _calendarEvent.intake_time.time
        calTime = calendar.time

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val intakeTime = "${sdf.format(_calendarEvent.intake_time)}, ${formatter.format(calTime)}"
        _drugIntakeTimeTV.text = intakeTime

    }

    private fun initViewModels() {
        _viewModel = ViewModelProvider(this).get(DrugInfoViewModel::class.java)
        _viewModel.initiateDrugImage(this, _calendarEvent.drug_rxcui.toString())
        _profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.di_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.di_menu_delete -> {
                showDeletePopup()
                true
            }
            R.id.di_menu_edit -> {
                goToEditActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToEditActivity() {
        val intent = Intent(
            this,
            DrugOccurrenceActivity::class.java
        )
        intent.putExtra(
            DbConstants.DRUG_OBJECT,
            DrugOccurrence(
                _calendarEvent.drug_name,
                _calendarEvent.drug_rxcui.toInt(),
                _calendarEvent.event_id,
                repeatEnd = _calendarEvent.repeat_end
            )
        )
        intent.putExtra(DbConstants.LOGGED_USER_EMAIL, _loggedEmail)
        intent.putExtra(DbConstants.LOGGED_USER_NAME, _currentProfile)
        intent.putExtra(DbConstants.INTAKE_DATE, _calendarEvent.intake_time.time)

        startActivity(intent)

    }

    private fun showDeletePopup() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure you want to delete this drug?")
        builder.setItems(arrayOf<CharSequence>(
            "Delete all occurrences",
            "Delete future occurrences",
            "cancel"
        ),
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    //  delete all occurrences

                    0 -> _viewModel.deleteAllOccurrencesOfDrug(
                        email = _loggedEmail,
                        currentProfile = _currentProfile,
                        drug = DrugOccurrence(
                            _calendarEvent.drug_name,
                            _calendarEvent.drug_rxcui,
                            _calendarEvent.event_id,
                            repeatWeekday = _calendarEvent.repeat_weekday,
                            repeatEnd = _calendarEvent.repeat_end
                        ),
                        context = this
                    )

                    //  delete future occurrences
                    1 -> {
                        val tomorrow =
                            DateUtils.getTomorrowDateInMillis(_calendarEvent.intake_time)
                        _calendarEvent.repeat_end = tomorrow
                        _viewModel.deleteFutureOccurrencesOfDrug(
                            email = _loggedEmail,
                            currentProfile = _currentProfile,
                            drug = DrugOccurrence(
                                _calendarEvent.drug_name,
                                _calendarEvent.drug_rxcui,
                                _calendarEvent.event_id,
                                repeatWeekday = _calendarEvent.repeat_weekday,
                                repeatEnd = _calendarEvent.repeat_end
                            ),
                            repeatEnd = tomorrow.toString(),
                            context = this
                        )
                    }

                    else -> {
                        return@OnClickListener
                    }
                }
            })
        builder.create().show()
    }
}