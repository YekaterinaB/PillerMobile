package com.example.piller.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.activities.AddNewDrugActivity
import com.example.piller.activities.ManageAccountActivity
import com.example.piller.listAdapters.EliAdapter
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.ProfileViewModel
import com.example.piller.viewModels.WeeklyCalendarViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton


class WeeklyCalendarFragment : Fragment() {
    private val weeklyCalendarViewModel: WeeklyCalendarViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()


    // add drug animation
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this.context,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this.context,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this.context,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this.context,
            R.anim.to_botton_anim
        )
    }

    private var FABClicked = false

    private var eliAdapters = mutableListOf<EliAdapter>()
    private var eliRecycles = mutableListOf<RecyclerView>()
    private lateinit var fragmentView: View

    private lateinit var newDrugFAB: FloatingActionButton
    private lateinit var newDrugCameraFAB: FloatingActionButton
    private lateinit var newDrugBoxFAB: FloatingActionButton
    private lateinit var newDrugNameFAB: FloatingActionButton
    private lateinit var newDrugLayout: ConstraintLayout
    private lateinit var newDrugCameraLabel: TextView
    private lateinit var newDrugBoxLabel: TextView
    private lateinit var newDrugNameLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_weekly_calendar, container, false)
        initViews(fragmentView)
        initListeners()
        initObservers()
        weeklyCalendarViewModel.getWeekEvents(
            profileViewModel.getCurrentEmail(),
            profileViewModel.getCurrentProfile()
        )
        initRecyclersAndAdapters()
        return fragmentView
    }

    private fun initObservers() {
        weeklyCalendarViewModel.mutableCurrentWeeklyCalendar.observe(
            viewLifecycleOwner,
            Observer { calendar ->
                calendar?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        //update view
                        updateRecyclersAndAdapters()
                        //update current profile calendar
                        profileViewModel.changeCalendarForCurrentProfile(it)
                    }

                }
            })

        weeklyCalendarViewModel.mutableToastError.observe(
            viewLifecycleOwner,
            Observer { toastMessage ->
                toastMessage?.let {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        SnackBar.showToastBar(this.requireContext(), toastMessage)
                    }

                }
            })
    }

    private fun updateRecyclersAndAdapters() {
        for (i in 0 until 7) {
            val newList = weeklyCalendarViewModel.mutableCurrentWeeklyCalendar.value!!.get(i)
            eliAdapters[i].setData(newList)
            eliAdapters[i].notifyDataSetChanged()
        }
    }

    private fun initRecyclersAndAdapters() {
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_sunday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_monday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_tuesday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_wednesday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_thursday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_friday_list))
        eliRecycles.add(fragmentView.findViewById(R.id.calendar_saturday_list))

        val weeklyEvents = profileViewModel.getCurrentProfile().getWeeklyCalendar()
        for (i in 0 until 7) {
            eliRecycles[i].layoutManager = LinearLayoutManager(fragmentView.context)
            // add as empty list
            eliAdapters.add(EliAdapter(weeklyEvents[i]))
            eliRecycles[i].adapter = eliAdapters[i]
        }

    }

    companion object {
        fun newInstance(): WeeklyCalendarFragment = WeeklyCalendarFragment()
    }

    private fun initListeners() {
        newDrugFAB.setOnClickListener {
            onNewDrugFABClicked()
        }

        newDrugCameraFAB.setOnClickListener {
            SnackBar.showToastBar(
                this.context,
                "Add by camera!"
            )
        }

        newDrugBoxFAB.setOnClickListener {
            SnackBar.showToastBar(
                this.context,
                "Add by box!"
            )
        }

        newDrugNameFAB.setOnClickListener {
            val intent = Intent(
                activity,
                AddNewDrugActivity::class.java
            )
            intent.putExtra(DbConstants.ADD_DRUG_TYPE, DbConstants.DRUG_BY_NAME)
            intent.putExtra(DbConstants.LOGGED_USER_EMAIL,profileViewModel.getCurrentEmail())
            intent.putExtra(DbConstants.LOGGED_USER_NAME, profileViewModel.getCurrentProfileName())
            startActivity(intent)
        }

        newDrugLayout.setOnClickListener {
            FABLayoutClicked()
        }
    }

    private fun FABLayoutClicked() {
        if (FABClicked) {
            setFABVisibility(FABClicked)
            setFABAnimation(FABClicked)
            setFABClickable(FABClicked)
            newDrugLayout.setBackgroundColor(Color.TRANSPARENT)
            newDrugLayout.visibility = View.GONE
            FABClicked = !FABClicked
        }
    }

    private fun onNewDrugFABClicked() {
        setFABVisibility(FABClicked)
        setFABAnimation(FABClicked)
        setFABClickable(FABClicked)
        FABClicked = !FABClicked
    }

    private fun setFABVisibility(clicked: Boolean) {
        if (!clicked) {
            newDrugCameraFAB.visibility = View.INVISIBLE
            newDrugCameraLabel.visibility = View.INVISIBLE
            newDrugBoxFAB.visibility = View.INVISIBLE
            newDrugNameFAB.visibility = View.INVISIBLE
            newDrugNameLabel.visibility = View.INVISIBLE
            newDrugBoxLabel.visibility = View.INVISIBLE
            newDrugLayout.visibility = View.VISIBLE
            newDrugLayout.setBackgroundColor(Color.parseColor("#B3000000"))
        } else {
            newDrugCameraFAB.visibility = View.VISIBLE
            newDrugCameraLabel.visibility = View.VISIBLE
            newDrugBoxFAB.visibility = View.VISIBLE
            newDrugNameFAB.visibility = View.VISIBLE
            newDrugNameLabel.visibility = View.VISIBLE
            newDrugBoxLabel.visibility = View.VISIBLE
            newDrugLayout.visibility = View.GONE
            newDrugLayout.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setFABAnimation(clicked: Boolean) {
        if (!clicked) {
            newDrugBoxFAB.startAnimation(fromBottom)
            newDrugCameraFAB.startAnimation(fromBottom)
            newDrugCameraLabel.startAnimation(fromBottom)
            newDrugNameFAB.startAnimation(fromBottom)
            newDrugBoxLabel.startAnimation(fromBottom)
            newDrugNameLabel.startAnimation(fromBottom)
            newDrugFAB.startAnimation(rotateOpen)
        } else {
            newDrugBoxFAB.startAnimation(toBottom)
            newDrugCameraFAB.startAnimation(toBottom)
            newDrugCameraLabel.startAnimation(toBottom)
            newDrugNameFAB.startAnimation(toBottom)
            newDrugNameLabel.startAnimation(toBottom)
            newDrugBoxLabel.startAnimation(toBottom)
            newDrugFAB.startAnimation(rotateClose)
        }
    }

    private fun setFABClickable(clicked: Boolean) {
        if (!clicked) {
            newDrugCameraFAB.isClickable = true
            newDrugBoxFAB.isClickable = true
            newDrugNameFAB.isClickable = true
        } else {
            newDrugCameraFAB.isClickable = false
            newDrugBoxFAB.isClickable = false
            newDrugNameFAB.isClickable = false
        }
    }

    private fun initViews(fragment: View) {
        newDrugFAB = fragment.findViewById(R.id.calendar_new_drag_fab)
        newDrugCameraFAB = fragment.findViewById(R.id.calendar_add_drug_camera)
        newDrugBoxFAB = fragment.findViewById(R.id.calendar_add_drug_box)
        newDrugNameFAB = fragment.findViewById(R.id.calendar_add_drug_name)

        newDrugLayout = fragment.findViewById(R.id.calendar_fab_root)

        newDrugCameraLabel = fragment.findViewById(R.id.calendar_drug_camera_label)
        newDrugBoxLabel = fragment.findViewById(R.id.calendar_drug_box_label)
        newDrugNameLabel = fragment.findViewById(R.id.calendar_drug_name_label)
    }
}