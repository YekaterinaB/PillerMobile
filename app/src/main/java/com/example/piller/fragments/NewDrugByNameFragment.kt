package com.example.piller.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.activities.AddNewDrugActivity
import com.example.piller.listAdapters.NewDrugByNameAdapter
import com.example.piller.viewModels.AddNewDrugViewModel
import com.google.android.material.textfield.TextInputLayout

class NewDrugByNameFragment : Fragment() {
    private lateinit var drugNameTIL: TextInputLayout
    private lateinit var searchBtn: Button
    private lateinit var drugSelectedBtn: Button
    private lateinit var drugOptionsList: RecyclerView

    private val viewModel: AddNewDrugViewModel by activityViewModels()

    private lateinit var drugAdapter: NewDrugByNameAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val newFragment = inflater.inflate(R.layout.fragment_new_drug_by_name, container, false)
        initViews(newFragment)
        initListeners(newFragment)
        setViewModelsObservers()
        initAdapter(newFragment)
        return newFragment
    }

    private fun updateRecyclersAndAdapters() {
        viewModel.drugsSearchResult.value?.let { drugAdapter.setData(it) }
        drugAdapter.notifyDataSetChanged()
    }

    private fun initAdapter(fragment: View) {
        drugOptionsList.layoutManager = LinearLayoutManager(fragment.context)
        drugAdapter = NewDrugByNameAdapter(
            mutableListOf(),
            clickOnItemListener = { drug -> clickOnDrug(drug) })

        drugOptionsList.adapter = drugAdapter
    }

    private fun clickOnDrug(rxcui: Int) {
        val drug = viewModel.getDrugByRxcui(rxcui)
        if (drug != null) {
            (activity as AddNewDrugActivity).fragmentResult(drug)
        }
    }

    private fun setViewModelsObservers() {
        activity?.let {
            viewModel.drugsSearchResult.observe(it, Observer {
                updateRecyclersAndAdapters()
                setButtonsEnabled(true)
            })

            viewModel.snackBarMessage.observe(it, Observer { message ->
                SnackBar.showToastBar(it, message)
            })
        }
    }

    private fun initListeners(fragment: View) {
        searchBtn.setOnClickListener {
            searchDrugCommand(fragment)
        }

        drugNameTIL.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDrugCommand(fragment)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun searchDrugCommand(fragment: View) {
        setButtonsEnabled(false)
        //  close the keyboard when clicking search
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(fragment.windowToken, 0)
        //  todo check if the drug name changed from before??
        searchDrug()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        searchBtn.isEnabled = enabled
        drugSelectedBtn.isEnabled = enabled
        drugNameTIL.editText?.isEnabled = enabled
    }

    private fun searchDrug() {
        val drugName = drugNameTIL.editText!!.text.toString()
        if (drugName.isEmpty()) {
            activity?.let { thisActivity ->
                SnackBar.showToastBar(
                    thisActivity,
                    "Please enter drug name!"
                )
                return
            }
        }

        viewModel.searchDrugByName(drugName.trim())
    }

    private fun initViews(fragment: View) {
        drugNameTIL = fragment.findViewById(R.id.nd_drug_search_til)
        searchBtn = fragment.findViewById(R.id.nd_search_btn)
        drugSelectedBtn = fragment.findViewById(R.id.nd_drug_selected_btn)
        drugOptionsList = fragment.findViewById(R.id.nd_drug_options_list)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            NewDrugByNameFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}