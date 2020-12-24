package com.example.piller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.activities.AddNewDrugActivity
import com.example.piller.viewModels.AddNewDrugViewModel
import com.google.android.material.textfield.TextInputLayout

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NewDrugByNameFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var drugNameTIL: TextInputLayout
    private lateinit var searchBtn: Button
    private lateinit var drugSelectedBtn: Button
    private lateinit var drugOptionsList: RecyclerView

    private lateinit var viewModel: AddNewDrugViewModel

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
        initListeners()
        viewModel = ViewModelProvider(this).get(AddNewDrugViewModel::class.java)
        setViewModelsObservers()
        return newFragment
    }

    private fun setViewModelsObservers() {
        activity?.let {
            viewModel.drugsSearchResult.observe(it, Observer { updatedDrugsList ->
                SnackBar.showToastBar(it, "FOUND!")
            })
        }
    }

    private fun initListeners() {
        searchBtn.setOnClickListener {
            searchDrug()
        }
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
        (activity as AddNewDrugActivity).fragmentResult(drugName)
    }

    private fun initViews(fragment: View) {
        drugNameTIL = fragment.findViewById(R.id.nd_drug_search_til)
        searchBtn = fragment.findViewById(R.id.nd_search_btn)
        drugSelectedBtn = fragment.findViewById(R.id.nd_drug_selected_btn)
        drugOptionsList = fragment.findViewById(R.id.nd_drug_options_list)
    }

    companion object {
        @JvmStatic
        fun newInstance(/*param1: String, param2: String*/) =
            NewDrugByNameFragment().apply {
                arguments = Bundle().apply {
                    /*putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)*/
                }
            }
    }
}