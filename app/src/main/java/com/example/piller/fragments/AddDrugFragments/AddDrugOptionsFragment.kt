package com.example.piller.fragments.AddDrugFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.piller.R
import com.example.piller.fragments.FragmentWithUserObject
import com.example.piller.models.UserObject

class AddDrugOptionsFragment : FragmentWithUserObject() {
    private lateinit var _fragmentView: View
    private lateinit var _searchName: ConstraintLayout
    private lateinit var _searchBox: ConstraintLayout
    private lateinit var _searchPill: ConstraintLayout


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

    private fun initViews(){
        _searchName=_fragmentView.findViewById(R.id.drug_option_name)
        _searchBox=_fragmentView.findViewById(R.id.drug_option_box)
        _searchPill=_fragmentView.findViewById(R.id.drug_option_pill)
    }

    private fun setOnClickListeners(){
        _searchName.setOnClickListener {
//            val transaction = activity?.supportFragmentManager?.beginTransaction()
//            if (transaction != null) {
//                transaction.replace(
//                    R.id.calender_weekly_container_fragment, DrugByNameFragment
//                        .newInstance(_loggedUserObject)
//                )
//                transaction.disallowAddToBackStack()
//                transaction.commit()
//            }
        }

        _searchBox.setOnClickListener {
//            val transaction = activity?.supportFragmentManager?.beginTransaction()
//            if (transaction != null) {
//                transaction.replace(
//                    R.id.calender_weekly_container_fragment, DrugByBoxFragment
//                        .newInstance(_loggedUserObject)
//                )
//                transaction.disallowAddToBackStack()
//                transaction.commit()
//            }
        }

        _searchPill.setOnClickListener {
//            val transaction = activity?.supportFragmentManager?.beginTransaction()
//            if (transaction != null) {
//                transaction.replace(
//                    R.id.calender_weekly_container_fragment, DrugByImageFragment
//                        .newInstance(_loggedUserObject)
//                )
//                transaction.disallowAddToBackStack()
//                transaction.commit()
//            }
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