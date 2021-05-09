package com.example.piller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.piller.R
import com.example.piller.utilities.DbConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_drug_dialog_dialog.*

class DrugBottomDialogFragment(
    private val title: String,
    private val buttonsTitles: Pair<String, String>,
    private val clickOnFirstButtonListener: () -> Unit,
    private val clickOnSecondButtonListener: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_drug_dialog_dialog, container, false)
    }

    private fun initViewsData() {
        btm_dialog_title.text = title
        btm_dialog_first_button.text = buttonsTitles.first
        btm_dialog_second_button.text = buttonsTitles.second
    }

    private fun initListeners() {
        btm_dialog_first_button.setOnClickListener { clickOnFirstButtonListener() }
        btm_dialog_second_button.setOnClickListener { clickOnSecondButtonListener() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        initViewsData()
    }

    companion object {

        const val TAG = DbConstants.drugBottomDialogFragmentTag

        fun newInstance(
            title: String,
            buttonsTitles: Pair<String, String>,
            clickOnFirstButtonListener: () -> Unit,
            clickOnSecondButtonListener: () -> Unit
        ): DrugBottomDialogFragment =
            DrugBottomDialogFragment(
                title,
                buttonsTitles,
                clickOnFirstButtonListener,
                clickOnSecondButtonListener
            )
    }
}