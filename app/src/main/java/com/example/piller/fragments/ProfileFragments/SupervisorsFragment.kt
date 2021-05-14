package com.example.piller.fragments.ProfileFragments

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.fragments.FragmentWithUserObject
import com.example.piller.listAdapters.SupervisorsAdapter
import com.example.piller.models.UserObject
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.SupervisorsViewModel
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.supervisors_main_layout.*

class SupervisorsFragment : FragmentWithUserObject() {
    private lateinit var _fragmentView: View
    private val _viewModel: SupervisorsViewModel by activityViewModels()
    private lateinit var _supervisorAdapter: SupervisorsAdapter
    private lateinit var _supervisorRecycle: RecyclerView
    private lateinit var _thresholdCountTv: TextView
    private lateinit var _supervisorCountTv: TextView
    private lateinit var _dimLayout: RelativeLayout
    private lateinit var _backButton: ImageView
    private lateinit var _addNewSupervisorTv: TextView
    private lateinit var _changeMissedThresholdItem: ConstraintLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.supervisors_main_layout, container, false)
        initViews()
        initViewModelData()
        initObservers()

        setClickListeners()
        initRecyclersAndAdapters()
        return _fragmentView
    }


    private fun initViews() {
        _thresholdCountTv = _fragmentView.findViewById(R.id.number_of_missed_occur_textview)
        _supervisorCountTv = _fragmentView.findViewById(R.id.number_of_supervisors_title_item)
        _dimLayout = _fragmentView.findViewById(R.id.supervisor_dim_layout)
        _backButton = _fragmentView.findViewById(R.id.go_back_from_supervisors)
        _addNewSupervisorTv = _fragmentView.findViewById(R.id.add_new_supervisor_tx)
        _changeMissedThresholdItem =
            _fragmentView.findViewById(R.id.supervisor_missed_title_item_supervisors)

    }

    private fun setClickListeners() {
        _backButton.setOnClickListener {
            onPressBack()
        }

        _addNewSupervisorTv.setOnClickListener {
            showAddSupervisorWindow()
        }

        _changeMissedThresholdItem.setOnClickListener {
            setMissedThreshold()
        }
    }

    private fun showAddSupervisorWindow() {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.supervisor_add_supervisor_layout, null)

        // create pop up window for add profile
        MaterialStyledDialog.Builder(context)
            .setIcon(R.drawable.ic_supervisor_eye)
            .setTitle(getString(R.string.addSupervisorWindowTitle))
            .setCustomView(itemView)
            .setNegativeText(getString(R.string.cancel))
            .onNegative { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveText(getString(R.string.addSupervisorWindowAddButton))
            .onPositive(MaterialDialog.SingleButtonCallback { _, _ ->
                val supervisorName =
                    itemView.findViewById<View>(R.id.supervisor_name) as MaterialEditText
                val supervisorEmail =
                    itemView.findViewById<View>(R.id.supervisor_email) as MaterialEditText

                when {
                    TextUtils.isEmpty(supervisorName.text.toString()) -> {
                        SnackBar.showToastBar(
                            context,
                            getString(R.string.addSupervisorWindowEmptyName)
                        )
                        return@SingleButtonCallback
                    }

                    TextUtils.isEmpty(supervisorEmail.text.toString()) -> {
                        SnackBar.showToastBar(
                            context,
                            getString(R.string.addSupervisorWindowEmptyEmail)
                        )
                        return@SingleButtonCallback
                    }
                }
                _viewModel.addSupervisorsToDB(
                    supervisorName.text.toString(),
                    supervisorEmail.text.toString(),
                    loggedUserObject.userId
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
        _supervisorRecycle = _fragmentView.findViewById(R.id.supervisors_list_of_items)
        // initiate list of profiles with recyclers and adapters
        val supervisorList = _viewModel.mutableSupervisorList.value!!
        _supervisorRecycle.layoutManager = LinearLayoutManager(context)
        _supervisorAdapter = SupervisorsAdapter(
            supervisorList,
            clickOnDeleteButtonListener = { clickOnDeleteSupervisorButton(it) })
        _supervisorRecycle.adapter = _supervisorAdapter
    }

    private fun clickOnDeleteSupervisorButton(supervisorEmail: String) {
        removeSupervisorPopup(supervisorEmail)
    }

    private fun initObservers() {
        _viewModel.mutableSupervisorThreshold.observe(viewLifecycleOwner,
            Observer { threshold ->
                threshold?.let {
                    if (this.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        var numberOfMissed =
                            (_viewModel.mutableSupervisorThreshold.value!!).toString()
                        if (numberOfMissed == DbConstants.noMissedDaysCountStr) {
                            numberOfMissed = DbConstants.noMissedDaysText
                        }
                        val thresholdText = "$numberOfMissed "
                        _thresholdCountTv.text = thresholdText
                    }
                }
            })

        _viewModel.mutableToastError.observe(
            viewLifecycleOwner,
            Observer { toastMessage ->
                toastMessage?.let {
                    SnackBar.showToastBar(context, toastMessage)
                }
            })

        _viewModel.mutableSupervisorList.observe(
            viewLifecycleOwner,
            Observer { supervisorList ->
                supervisorList?.let {
                    updateRecyclersAndAdapters()
                    val supervisorsAmountTxt = supervisorList.size.toString() + " "
                    _supervisorCountTv.text = supervisorsAmountTxt
                }
            })

    }

    private fun removeSupervisorPopup(supervisorEmail: String) {
        val customView: View = layoutInflater.inflate(R.layout.supervisors_remove_popup, null)
        val popup = PopupWindow(
            customView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        popup.elevation = DbConstants.popupElevation

        val cancelViewText = customView.findViewById<TextView>(R.id.cancel_remove_supervisor_popup)
        val setViewText =
            customView.findViewById<TextView>(R.id.remove_supervusor_remove_supervisor_popup)
        val mailViewText = customView.findViewById<TextView>(R.id.supervisor_mail_remove_popup)
        mailViewText.text = getString(R.string.removeSupervisorWarning, supervisorEmail)

        cancelViewText.setOnClickListener {
            popup.dismiss()
            changeDarkBackgroundVisibility(false)
        }

        setViewText.setOnClickListener {
            _viewModel.deleteSupervisorsFromDB(supervisorEmail, loggedUserObject.userId)
            popup.dismiss()
            changeDarkBackgroundVisibility(false)

        }
        changeDarkBackgroundVisibility(true)
        popup.showAtLocation(_fragmentView, Gravity.CENTER, DbConstants.popupX, DbConstants.popupY)
    }

    private fun setMissedThreshold() {
        val customView: View = layoutInflater.inflate(R.layout.supervisor_missed_popup, null)
        val popup = PopupWindow(
            customView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Set an elevation value for popup window
        popup.elevation = DbConstants.popupElevation

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
        popup.showAtLocation(_fragmentView, Gravity.CENTER, DbConstants.popupX, DbConstants.popupY)

    }

    private fun updateMissedInPopup(seekBar: IndicatorSeekBar, thresholdCountPopup: TextView) {
        seekBar.setProgress(_viewModel.mutableSupervisorThreshold.value!!.toFloat())
        if (_viewModel.mutableSupervisorThreshold.value!! == DbConstants.noMissedDaysCount) {
            thresholdCountPopup.text = DbConstants.noMissedDaysText
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
        seekBar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {

                var numberOfMissed = seekBar.progress.toString()
                if (numberOfMissed == DbConstants.noMissedDaysCountStr) {
                    numberOfMissed = DbConstants.noMissedDaysText
                }
                val thresholdText = "$numberOfMissed "
                _thresholdCountTv.text = thresholdText
                thresholdCountPopup.text = thresholdText
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {}
        }
        cancelViewText.setOnClickListener {
            popup.dismiss()
            changeDarkBackgroundVisibility(false)
        }

        setViewText.setOnClickListener {
            val numberOfMissed = seekBar.progress.toString()
            _viewModel.updateThresholdInDB(numberOfMissed, loggedUserObject.userId)
            popup.dismiss()
            changeDarkBackgroundVisibility(false)

        }
    }

    private fun initViewModelData() {
        _viewModel.getSupervisorsFromDB(loggedUserObject.userId)
    }

    private fun onPressBack() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(
                R.id.calender_weekly_container_fragment,
                ProfileFragment.newInstance(loggedUserObject)
            )
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }

    companion object {
        fun newInstance(loggedUser: UserObject) =
            SupervisorsFragment().apply {
                arguments = Bundle().apply {
                    loggedUserObject = loggedUser
                }
            }
    }
}