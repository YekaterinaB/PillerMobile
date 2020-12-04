package com.example.piller.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.models.Supervisor
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.DbConstants.DEFAULT_SUPERVISOR_THRESHOLD
import com.example.piller.viewModels.ManageSupervisorsViewModel


class ManageSupervisorsActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: ManageSupervisorsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModels()
        initObservers()
        setContentView(R.layout.manage_supervisors_layout)

        //toolbar
        toolbar = findViewById(R.id.manage_supervisors_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setSpinnerThreshold()

    }

    private fun initObservers() {
        viewModel.mutableSupervisorThreshold.observe(
            this,
            Observer { threshold ->
                threshold?.let {
                    //todo change spinner to selected value
                }
            })

        viewModel.mutableToastError.observe(
            this,
            Observer { toastMessage ->
                toastMessage?.let {
                    SnackBar.showToastBar(this, toastMessage)
                }
            })

        viewModel.mutableSupervisorList.observe(
            this,
            Observer { supervisorList ->
                supervisorList?.let {
                    //todo supervisors list
                }
            })

    }



    private fun setSpinnerThreshold() {
        // access the items of the list
        val daysThreshold = resources.getStringArray(R.array.threshold_alarm)

        // access the spinner
        val spinner = findViewById<Spinner>(R.id.supervisors_alarm_threshold_spinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, daysThreshold
            )
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    viewModel.updateThresholdInDB(daysThreshold[position]!!)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
    }

    private fun initViewModels() {
        viewModel = ViewModelProvider(this).get(ManageSupervisorsViewModel::class.java)
        viewModel.setEmailAndName(
            intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!,
            intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        )
        viewModel.mutableSupervisorList.value = mutableListOf<Supervisor>()
        viewModel.mutableSupervisorThreshold.value = DEFAULT_SUPERVISOR_THRESHOLD
        viewModel.getSupervisorsFromDB()
        viewModel.getThresholdFromDB()
    }
}