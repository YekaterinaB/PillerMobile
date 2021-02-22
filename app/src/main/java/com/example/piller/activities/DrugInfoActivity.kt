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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.models.CalendarEvent
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugInfoViewModel
import com.example.piller.viewModels.ProfileViewModel
import kotlinx.android.synthetic.main.activity_drug_occurrence.*
import java.text.SimpleDateFormat
import java.util.*


class DrugInfoActivity : AppCompatActivity() {
    private lateinit var viewModel: DrugInfoViewModel
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var drugNameTV: TextView
    private lateinit var drugIntakeTimeTV: TextView
    private lateinit var drugTakenCB: CheckBox
    private lateinit var drugImageIV: ImageView

    private lateinit var loggedEmail: String
    private lateinit var loggedName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drug_info)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loggedEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        loggedName = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        initViewModels()
        initViews()
        initObservers()
    }

    private fun initObservers() {
        viewModel.mutableToastError.observe(
            this,
            Observer { toastMessage ->
                toastMessage?.let { SnackBar.showToastBar(this, toastMessage) }
            })

        viewModel.deleteSuccess.observe(
            this,
            Observer { success ->
                if (success) {
                    val returnIntent = Intent()
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            })
    }

    private fun initViews() {
        drugNameTV = findViewById(R.id.di_drug_name)
        drugIntakeTimeTV = findViewById(R.id.di_drug_intake_time)
        drugTakenCB = findViewById(R.id.di_drug_taken)
        drugImageIV = findViewById(R.id.di_drug_image)

        initViewsData()
    }

    private fun initViewsData() {
        val calendarEvent: CalendarEvent = viewModel.getCalendarEvent()
        drugNameTV.text = calendarEvent.drug_name
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        drugIntakeTimeTV.text = sdf.format(calendarEvent.intake_time)
        drugTakenCB.isChecked = calendarEvent.is_taken
    }

    private fun initViewModels() {
        viewModel = ViewModelProvider(this).get(DrugInfoViewModel::class.java)
        viewModel.setCalendarEvent(intent.getParcelableExtra(DbConstants.CALENDAR_EVENT)!!)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeletePopup() {
        val calendarEvent: CalendarEvent = viewModel.getCalendarEvent()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure you want to delete this drug?")
        builder.setItems(arrayOf<CharSequence>(
            "Delete all occurrences",
            "Delete future occurrences",
            "cancel"
        ),
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 -> viewModel.deleteAllOccurrencesOfDrug(
                        loggedEmail,
                        loggedName,
                        calendarEvent.drug_rxcui
                    )

                    1 -> Toast.makeText(
                        this@DrugInfoActivity,
                        "Delete future occurrences",
                        Toast.LENGTH_SHORT
                    ).show()

                    else -> {
                        return@OnClickListener
                    }
                }
            })
        builder.create().show()
    }
}