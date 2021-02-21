package com.example.piller.activities

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.models.CalendarEvent
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugInfoViewModel
import kotlinx.android.synthetic.main.activity_drug_occurrence.*
import java.text.SimpleDateFormat
import java.util.*

class DrugInfoActivity : AppCompatActivity() {
    private lateinit var viewModel: DrugInfoViewModel

    private lateinit var drugNameTV: TextView
    private lateinit var drugIntakeTimeTV: TextView
    private lateinit var drugTakenCB: CheckBox
    private lateinit var drugImageIV: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drug_info)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initViewModel()
        initViews()
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

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(DrugInfoViewModel::class.java)
        viewModel.setCalendarEvent(intent.getParcelableExtra(DbConstants.CALENDAR_EVENT)!!)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}