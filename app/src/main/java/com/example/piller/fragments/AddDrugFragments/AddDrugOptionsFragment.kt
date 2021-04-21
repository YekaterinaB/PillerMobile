package com.example.piller.fragments.AddDrugFragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import com.example.piller.R
import com.example.piller.activities.AddNewDrugActivity
import com.example.piller.fragments.FragmentWithUserObject
import com.example.piller.models.UserObject
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.WeeklyCalendarViewModel

class AddDrugOptionsFragment : FragmentWithUserObject() {
    private lateinit var _fragmentView: View
    private lateinit var _searchName: ConstraintLayout
    private lateinit var _searchBox: ConstraintLayout
    private lateinit var _searchPill: ConstraintLayout
    private val _weeklyCalendarViewModel: WeeklyCalendarViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.add_drug_options_main_layout, container, false)
        initViews()

        setOnClickListeners()

        return _fragmentView
    }

    /**
     * @fragmentID: says which fragment AddNewDrugActivity should show
     */
    private fun showAddNewDrugActivity(addType: String) {
        val intent = Intent(activity, AddNewDrugActivity::class.java)
        intent.putExtra(DbConstants.ADD_DRUG_TYPE, addType)
        putLoggedUserObjectInIntent(intent)
        intent.putExtra(DbConstants.CALENDAR_ID, _weeklyCalendarViewModel.calendarId)
        startActivity(intent)
    }

    private fun initViews(){
        _searchName=_fragmentView.findViewById(R.id.drug_option_name)
        _searchBox=_fragmentView.findViewById(R.id.drug_option_box)
        _searchPill=_fragmentView.findViewById(R.id.drug_option_pill)
    }

    private fun setOnClickListeners(){
        _searchName.setOnClickListener {
            showAddNewDrugActivity(DbConstants.DRUG_BY_NAME)
        }

        _searchBox.setOnClickListener {
            showAddNewDrugActivity(DbConstants.DRUG_BY_BOX)

        }

        _searchPill.setOnClickListener {
            showAddNewDrugActivity(DbConstants.DRUG_BY_PILL)

        }
    }

    companion object {
        fun newInstance(loggedUser: UserObject) =
            AddDrugOptionsFragment().apply {
                arguments = Bundle().apply {
                    _loggedUserObject = loggedUser
                }
            }
    }
}