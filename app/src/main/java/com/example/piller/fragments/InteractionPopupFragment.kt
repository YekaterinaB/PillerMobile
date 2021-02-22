package com.example.piller.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.piller.R
import com.example.piller.activities.AddNewDrugActivity


class InteractionPopupFragment : DialogFragment() {
    private lateinit var interactionsTxt: String
    private lateinit var titleTv: TextView
    private lateinit var interactionScrollViewTV: TextView
    private lateinit var cancelButton: Button
    private lateinit var proceedButton: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //  set round corners
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner)
        val fragmentView = inflater.inflate(R.layout.interaction_popup, container, false)
        interactionsTxt = arguments?.getString(DRUG_INTERACTIONS).toString()
        initViews(fragmentView)
        initListeners()
        return fragmentView
    }

    private fun initListeners(){
        cancelButton.setOnClickListener {
            dismiss()
        }

        proceedButton.setOnClickListener {
            (activity as AddNewDrugActivity).goToAddOccurrenceActivity()
        }

    }

    private fun initViews(fragment: View) {
        titleTv = fragment.findViewById(R.id.inter_popup_title_tv)
        titleTv.text = "WARNING:\nThere are interactions with the drug you added:"

        interactionScrollViewTV = fragment.findViewById(R.id.inter_popup_list_tv)
        interactionScrollViewTV.movementMethod = ScrollingMovementMethod()
        interactionScrollViewTV.text=interactionsTxt
        cancelButton=fragment.findViewById(R.id.inter_button_cancel)
        proceedButton=fragment.findViewById(R.id.inter_button_proceedAnyway)
    }


    override fun onStart() {
        super.onStart()
        //  set size
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        //val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        const val DRUG_INTERACTIONS = "drug_interactions"

        fun newInstance(interactionTxt: String): InteractionPopupFragment {
            val fragment = InteractionPopupFragment()

            val bundle = Bundle().apply {
                putString(DRUG_INTERACTIONS, interactionTxt)
            }

            fragment.arguments = bundle

            return fragment
        }
    }
}