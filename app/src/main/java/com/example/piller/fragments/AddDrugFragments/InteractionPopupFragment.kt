package com.example.piller.fragments.AddDrugFragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.piller.R
import com.example.piller.activities.AddNewDrugActivity
import com.example.piller.utilities.DbConstants


class InteractionPopupFragment : DialogFragment() {
    private lateinit var _interactionsTxt: String
    private lateinit var _interactionScrollViewTV: TextView
    private lateinit var _cancelButton: TextView
    private lateinit var _proceedButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //  set round corners
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner)
        val fragmentView = inflater.inflate(R.layout.interaction_popup, container, false)
        _interactionsTxt = arguments?.getString(DRUG_INTERACTIONS).toString()
        initViews(fragmentView)
        initListeners()
        return fragmentView
    }

    private fun initListeners() {
        _cancelButton.setOnClickListener {
            dismiss()
        }

        _proceedButton.setOnClickListener {
            (activity as AddNewDrugActivity).goToAddOccurrenceActivity()
        }
    }

    private fun initViews(fragment: View) {
        _interactionScrollViewTV = fragment.findViewById(R.id.inter_popup_list_tv)
        _interactionScrollViewTV.movementMethod = ScrollingMovementMethod()
        _interactionScrollViewTV.text = _interactionsTxt
        _cancelButton = fragment.findViewById(R.id.cancel_interaction_popup)
        _proceedButton = fragment.findViewById(R.id.proceed_anyway_interaction_popup)
    }


    override fun onStart() {
        super.onStart()
        //  set size
        val width =
            (resources.displayMetrics.widthPixels * DbConstants.interactionWindowWidthFactor).toInt()
        //val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        const val DRUG_INTERACTIONS = DbConstants.drugInteractionsIntentTag

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