package com.example.piller.fragments.AddDrugFragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.activities.AddNewDrugActivity
import com.example.piller.viewModels.DrugSearchViewModel

class DrugByNameFragment : Fragment() {
    private lateinit var drugNameET: EditText
    private lateinit var searchBtn: ImageButton
    private lateinit var fragmentView: View
    private val searchViewModel: DrugSearchViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_new_drug_by_name, container, false)
        initViews()
        initListeners()
        initObservers()
        return fragmentView
    }

    private fun initListeners() {
        searchBtn.setOnClickListener {
            searchDrugCommand()
        }

        drugNameET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDrugCommand()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        val searchTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchBtn.isEnabled = !(s.isNullOrEmpty() or s.isNullOrBlank())
                searchBtn.isClickable = !(s.isNullOrEmpty() or s.isNullOrBlank())
            }
        }

        drugNameET.addTextChangedListener(searchTextWatcher)
    }

    private fun initObservers() {
        searchViewModel.drugsSearchResult.observe(requireActivity(), Observer {
            setButtonsEnabled(true)
            //  set the drug name regardless of results (in case the user wants to select the drug
            //  not from the result list
            searchViewModel.drugSearchNoResult.value = drugNameET.text.toString()
        })
    }

    private fun searchDrug() {
        val drugName = drugNameET.text.toString()
        if (drugName.isEmpty()) {
            activity?.let { thisActivity ->
                SnackBar.showToastBar(thisActivity, "Please enter drug name!")
                setButtonsEnabled(true)
                return
            }
        }
        searchViewModel.searchDrugByName(drugName.trim())
    }

    private fun searchDrugCommand() {
        setButtonsEnabled(false)
        //  close the keyboard when clicking search
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(fragmentView.windowToken, 0)
        //  todo check if the drug name changed from before??
        searchDrug()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        drugNameET.isEnabled = enabled
        searchBtn.isEnabled = enabled

        // enable select button
        (activity as AddNewDrugActivity?)!!.setButtonsEnabled(enabled)
    }


    private fun initViews() {
        drugNameET = fragmentView.findViewById(R.id.nd_drug_search_et)
        searchBtn = fragmentView.findViewById(R.id.nd_search_btn)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DrugByNameFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}