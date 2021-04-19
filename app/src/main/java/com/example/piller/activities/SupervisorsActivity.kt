package com.example.piller.activities

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.fragments.ProfileFragment
import com.example.piller.listAdapters.SupervisorsAdapter
import com.example.piller.models.Supervisor
import com.example.piller.utilities.DbConstants.DEFAULT_SUPERVISOR_THRESHOLD
import com.example.piller.viewModels.SupervisorsViewModel
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.drug_by_name_list_item.*
import kotlinx.android.synthetic.main.supervisors_main_layout.*


class SupervisorsActivity : ActivityWithUserObject() {
    private lateinit var _viewModel: SupervisorsViewModel
    private lateinit var _supervisorAdapter: SupervisorsAdapter
    private lateinit var _supervisorRecycle: RecyclerView
    private lateinit var _thresholdCount: TextView
    private lateinit var _supervisorCount: TextView
    private lateinit var _dimLayout: RelativeLayout
    private lateinit var _mRelativeLayout: ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUserObject(intent)
        setContentView(R.layout.supervisors_main_layout)
        _mRelativeLayout = findViewById(R.id.supervisors_activity_laoyut)

        initViews()
        initViewModels()
        initObservers()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setClickListeners()
        initRecyclersAndAdapters()
    }

    private fun initViews() {
        _thresholdCount = findViewById(R.id.number_of_missed_occur_textview)
        _supervisorCount = findViewById(R.id.number_of_supervisors_title_item)
        _dimLayout = findViewById(R.id.supervisor_dim_layout)
    }

    private fun setClickListeners() {
        go_back_from_supervisors.setOnClickListener{
            //onPressBack()
        }

        add_new_supervisor_tx.setOnClickListener {
            showAddSupervisorWindow()
        }
        supervisor_missed_title_item.setOnClickListener {
            setMissedThreshold()
        }
    }

    private fun showAddSupervisorWindow() {
        val itemView = LayoutInflater.from(this)
            .inflate(R.layout.supervisor_add_supervisor_layout, null)

        // create pop up window for add profile
        MaterialStyledDialog.Builder(this)
            .setIcon(R.drawable.ic_supervisor_eye)
            .setTitle("ADD A NEW SUPERVISOR")
            .setCustomView(itemView)
            .setNegativeText("Cancel")
            .onNegative { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveText("Add supervisor")
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
                _viewModel.addSupervisorsToDB(
                    supervisorName.text.toString(),
                    supervisorEmail.text.toString(),
                    _loggedUserObject.userId
                )
            })
            .build()
            .show()
    }


    private fun updateRecyclersAndAdapters() {
        _supervisorAdapter.setData(_viewModel.mutableSupervisorList.value!!)
        _supervisorAdapter.notifyDataSetChanged()
    }


    private fun initRecyclersAndAdapters() {
        _supervisorRecycle = this.findViewById(R.id.supervisors_list_of_items)
        // initiate list of profiles with recyclers and adapters
        val supervisorList = _viewModel.mutableSupervisorList.value!!
        _supervisorRecycle.layoutManager = LinearLayoutManager(this)
        _supervisorAdapter = SupervisorsAdapter(
            supervisorList,
            clickOnDeleteButtonListener = { clickOnDeleteSupervisorButton(it) })
        _supervisorRecycle.adapter = _supervisorAdapter
    }

    private fun clickOnDeleteSupervisorButton(supervisorEmail: String) {
        removeSupervisorPopup(supervisorEmail)

    }


    private fun initObservers() {
        _viewModel.mutableSupervisorThreshold.observe(
            this,
            Observer { threshold ->
                threshold?.let {
                    if (this.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        var numberOfMissed =
                            (_viewModel.mutableSupervisorThreshold.value!!).toString()
                        if (numberOfMissed == "0") {
                            numberOfMissed = "No"
                        }
                        _thresholdCount.text = numberOfMissed

                    }
                }
            })

        _viewModel.mutableToastError.observe(
            this,
            Observer { toastMessage ->
                toastMessage?.let {
                    SnackBar.showToastBar(this, toastMessage)
                }
            })

        _viewModel.mutableSupervisorList.observe(
            this,
            Observer { supervisorList ->
                supervisorList?.let {
                    updateRecyclersAndAdapters()
                    _supervisorCount.text = supervisorList.size.toString()
                }
            })

    }

    private fun removeSupervisorPopup(supervisorEmail: String) {
        val customView: View = layoutInflater.inflate(R.layout.supervisors_remove_popup, null)
        val popup = PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        popup.elevation = 5.0f;


        val cancelViewText = customView.findViewById<TextView>(R.id.cancel_remove_supervisor_popup)
        val setViewText =
            customView.findViewById<TextView>(R.id.remove_supervusor_remove_supervisor_popup)
        val mailViewText = customView.findViewById<TextView>(R.id.supervisor_mail_remove_popup)
        mailViewText.text = supervisorEmail + " will be\nremoved from your supervisor list."

        cancelViewText.setOnClickListener {
            popup.dismiss()
            changeDarkBackgroundVisibility(false)
        }

        setViewText.setOnClickListener {
            _viewModel.deleteSupervisorsFromDB(supervisorEmail, _loggedUserObject.userId)
            popup.dismiss()
            changeDarkBackgroundVisibility(false)

        }
        changeDarkBackgroundVisibility(true)
        popup.showAtLocation(_mRelativeLayout, Gravity.CENTER, 0, 0)
    }

    private fun setMissedThreshold() {
        val customView: View = layoutInflater.inflate(R.layout.supervisor_missed_popup, null)
        val popup = PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            popup.setElevation(5.0f);
        }

        val cancelViewText = customView.findViewById<TextView>(R.id.cancel_missed_popup)
        val setViewText = customView.findViewById<TextView>(R.id.set_missed_popup)
        val seekBar = customView.findViewById<IndicatorSeekBar>(R.id.indicatorSeekBar_missed_popup)
        val thresholdCountInPopup =
            customView.findViewById<TextView>(R.id.number_of_missed_occur_textview)
        updateMissedInPopup(seekBar, thresholdCountInPopup)

        setClickListenersInPopup(
            cancelViewText, popup, seekBar, setViewText, thresholdCountInPopup
        )
        changeDarkBackgroundVisibility(true)
        popup.showAtLocation(_mRelativeLayout, Gravity.CENTER, 0, 0)

    }

    private fun updateMissedInPopup(seekBar: IndicatorSeekBar, thresholdCountPopup: TextView) {
        seekBar.setProgress(_viewModel.mutableSupervisorThreshold.value!!.toFloat())
        if (_viewModel.mutableSupervisorThreshold.value!! == 0) {
            thresholdCountPopup.text = "No"
        } else {
            thresholdCountPopup.text = _viewModel.mutableSupervisorThreshold.value!!.toString()
        }
    }

    private fun changeDarkBackgroundVisibility(isVisible: Boolean) {
        if (isVisible) {
            _dimLayout.visibility = View.VISIBLE

        } else {
            _dimLayout.visibility = View.GONE
        }
    }

    private fun setClickListenersInPopup(
        cancelViewText: TextView, popup: PopupWindow, seekBar: IndicatorSeekBar,
        setViewText: TextView, thresholdCountPopup: TextView
    ) {


        seekBar.setOnSeekChangeListener(object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {

                var numberOfMissed = seekBar.progress.toString()
                if (numberOfMissed == "0") {
                    numberOfMissed = "No"
                }
                _thresholdCount.text = numberOfMissed
                thresholdCountPopup.text = numberOfMissed
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {}
        })
        cancelViewText.setOnClickListener {
            popup.dismiss()
            changeDarkBackgroundVisibility(false)
        }

        setViewText.setOnClickListener {
            val numberOfMissed = seekBar.progress.toString()
            _viewModel.updateThresholdInDB(numberOfMissed, _loggedUserObject.userId)
            popup.dismiss()
            changeDarkBackgroundVisibility(false)

        }
    }

    private fun initViewModels() {
        _viewModel = ViewModelProvider(this).get(SupervisorsViewModel::class.java)
        _viewModel.mutableSupervisorList.value = mutableListOf<Supervisor>()
        _viewModel.mutableSupervisorThreshold.value = DEFAULT_SUPERVISOR_THRESHOLD
        _viewModel.getSupervisorsFromDB(_loggedUserObject.userId)
    }


//    private fun onPressBack() {
//        val transaction = activity?.supportFragmentManager?.beginTransaction()
//        if (transaction != null) {
//            transaction.replace(
//                R.id.calender_weekly_container_fragment, ProfileFragment
//                    .newInstance(_loggedUserObject)
//            )
//            transaction.disallowAddToBackStack()
//            transaction.commit()
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}