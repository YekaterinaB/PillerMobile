package com.example.piller.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.piller.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "drug name"

/**
 * A simple [Fragment] subclass.
 * Use the [NewDrugOccurrencesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewDrugOccurrencesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var drugName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            drugName = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_drug_occurrences, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param newDrugName Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewDrugOccurrencesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(newDrugName: String/*, param2: String*/) =
            NewDrugOccurrencesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, newDrugName)
                    /*putString(ARG_PARAM2, param2)*/
                }
            }
    }
}