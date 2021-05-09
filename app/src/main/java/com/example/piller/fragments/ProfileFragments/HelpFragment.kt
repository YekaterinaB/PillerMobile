package com.example.piller.fragments.ProfileFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.fragments.FragmentWithUserObject
import com.example.piller.listAdapters.HelpAdapter
import com.example.piller.models.UserObject

class HelpFragment : FragmentWithUserObject() {
    private lateinit var _backButton: ImageView
    private lateinit var _fragmentView: View
    private lateinit var _qAndARV: RecyclerView
    private lateinit var _qAndAAdapter: HelpAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.settings_help_main_layout, container, false)
        initViews()
        initRecyclerView()
        setOnClickListeners()

        return _fragmentView
    }


    private fun setOnClickListeners() {
        _backButton.setOnClickListener {
            onPressBack()
        }
    }

    private fun initViews() {
        _backButton = _fragmentView.findViewById(R.id.go_back_from_help)
        _qAndARV = _fragmentView.findViewById(R.id.help_q_and_a_rv)

    }

    private fun getQANDAList(): MutableList<Pair<String, String>> {
        val qAndA = mutableListOf<Pair<String, String>>()
        qAndA.add(
            Pair(getString(R.string.howCanIContactPiller), getString(R.string.contactPiller))
        )
        qAndA.add(
            Pair(getString(R.string.howCanIAddaNewMedicine), getString(R.string.addNewMedicineWays))
        )
        qAndA.add(
            Pair(
                getString(R.string.howToStopReceivingNotifications),
                getString(R.string.stopReceivingNotificationsInstruction)
            )
        )

        return qAndA
    }


    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        _qAndARV.layoutManager = layoutManager
        _qAndAAdapter = HelpAdapter(getQANDAList())
        _qAndARV.adapter = _qAndAAdapter

        val dividerItemDecoration = DividerItemDecoration(context, layoutManager.orientation)
        _qAndARV.addItemDecoration(dividerItemDecoration)
    }


    private fun onPressBack() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(
                R.id.calender_weekly_container_fragment,
                SettingsFragment.newInstance(_loggedUserObject)
            )
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }

    companion object {
        fun newInstance(loggedUser: UserObject) =
            HelpFragment().apply {
                arguments = Bundle().apply {
                    _loggedUserObject = loggedUser
                }
            }
    }
}