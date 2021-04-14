package com.example.piller.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.listAdapters.SupervisorsAdapter
import com.example.piller.models.Supervisor
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.DbConstants.DEFAULT_SUPERVISOR_THRESHOLD
import com.example.piller.viewModels.ManageSupervisorsViewModel
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.manage_supervisors_layout.*


class ManageSupervisorsActivity : AppCompatActivity() {
    private lateinit var thresholdSpinner: Spinner
    private lateinit var viewModel: ManageSupervisorsViewModel
    private lateinit var supervisorAdapter: SupervisorsAdapter
    private lateinit var supervisorRecycle: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModels()
        initObservers()
        setContentView(R.layout.manage_supervisors_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setClickListeners()
        initRecyclersAndAdapters()

    }

    private fun setClickListeners() {
        setSpinnerThreshold()
        supervisors_add_button.setOnClickListener {
            showAddSupervisorWindow()
        }
    }

    private fun showAddSupervisorWindow() {
        val itemView = LayoutInflater.from(this)
            .inflate(R.layout.add_supervisor_layout, null)

        // create pop up window for add profile
        MaterialStyledDialog.Builder(this)
            .setIcon(R.drawable.ic_supervisors)
            .setTitle("ADD A NEW SUPERVISOR")
            .setCustomView(itemView)
            .setNegativeText("CANCEL")
            .onNegative { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveText("ADD SUPERVISOR")
            .onPositive(MaterialDialog.SingleButtonCallback { _, _ ->
                val supervisorName =
                    itemView.findViewById<View>(R.id.supervisor_name) as MaterialEditText
                val supervisorEmail =
                    itemView.findViewById<View>(R.id.supervisor_email) as MaterialEditText

                when {
                    TextUtils.isEmpty(supervisorName.text.toString()) -> {
                        SnackBar.showToastBar(
                            this,
                            "Supervisor name cannot be empty"
                        )
                        return@SingleButtonCallback
                    }

                    TextUtils.isEmpty(supervisorEmail.text.toString()) -> {
                        SnackBar.showToastBar(
                            this,
                            "Supervisor email cannot be empty"
                        )
                        return@SingleButtonCallback
                    }
                }
                viewModel.addSupervisorsToDB(
                    supervisorName.text.toString(),
                    supervisorEmail.text.toString()
                )

            })
            .build()
            .show()
    }


    private fun updateRecyclersAndAdapters() {
        supervisorAdapter.setData(viewModel.mutableSupervisorList.value!!)
        supervisorAdapter.notifyDataSetChanged()
    }


    private fun initRecyclersAndAdapters() {
        supervisorRecycle = this.findViewById(R.id.supervisors_list)
        // initiate list of profiles with recyclers and adapters
        val supervisorList = viewModel.mutableSupervisorList.value!!
        supervisorRecycle.layoutManager = LinearLayoutManager(this)
        supervisorAdapter = SupervisorsAdapter(
            supervisorList,
            clickOnDeleteButtonListener = { clickOnDeleteSupervisorButton(it) })
        supervisorRecycle.adapter = supervisorAdapter
    }

    private fun clickOnDeleteSupervisorButton(supervisorEmail: String) {
        viewModel.deleteSupervisorsFromDB(supervisorEmail)
    }


    private fun initObservers() {
        viewModel.mutableSupervisorThreshold.observe(
            this,
            Observer { threshold ->
                threshold?.let {
                    if (this.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        thresholdSpinner.setSelection(viewModel.mutableSupervisorThreshold.value!!)
                    }
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
                    updateRecyclersAndAdapters()
                }
            })

    }


    private fun setSpinnerThreshold() {
        // access the items of the list
        val daysThreshold = resources.getStringArray(R.array.threshold_alarm)

        // access the spinner
        thresholdSpinner = findViewById<Spinner>(R.id.supervisors_alarm_threshold_spinner)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, daysThreshold
        )
        thresholdSpinner.adapter = adapter
        //false to not invoke listener on create
        thresholdSpinner.setSelection(viewModel.mutableSupervisorThreshold.value!!, false)

        thresholdSpinner.onItemSelectedListener = object :
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

    private fun initViewModels() {
        viewModel = ViewModelProvider(this).get(ManageSupervisorsViewModel::class.java)
        viewModel.setUserInfo(
            intent.getStringExtra(DbConstants.LOGGED_USER_ID)!!,
            intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!,
            intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        )
        viewModel.mutableSupervisorList.value = mutableListOf<Supervisor>()
        viewModel.mutableSupervisorThreshold.value = DEFAULT_SUPERVISOR_THRESHOLD
        viewModel.getSupervisorsFromDB()
        viewModel.getThresholdFromDB()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}