//package com.example.piller.fragments
//
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.os.Bundle
//import android.view.*
//import android.widget.Spinner
//import android.widget.TextView
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.lifecycle.Observer
//import com.example.piller.R
//import com.example.piller.SnackBar
//import com.example.piller.models.Drug
//import com.example.piller.viewModels.AddNewDrugViewModel
//import com.example.piller.viewModels.ProfileViewModel
//import java.text.SimpleDateFormat
//import java.util.*
//
//private const val ARG_NEW_DRUG = "new drug"
//
//class NewDrugOccurrencesFragment : Fragment() {
//    //  todo check if already has the drug by rxcui
//    //  todo - add X button on top so the user will be able to cancel
//    private lateinit var newDrugName: TextView
//    private lateinit var drugOccurrencesDate: TextView
//    private lateinit var drugOccurrencesTime: TextView
//    private lateinit var drugRepeatSpinner: Spinner
//    private lateinit var drugRepeatContainer: ConstraintLayout
//
//    //  get the viewmodel from the add new drug activity
//    private val viewModel: AddNewDrugViewModel by activityViewModels()
//    private val profileViewModel: ProfileViewModel by activityViewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
//        arguments?.let {
////            viewModel.setNewDrug(it.getParcelable(ARG_NEW_DRUG)!!)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val newFragment = inflater.inflate(R.layout.fragment_new_drug_occurrences, container, false)
//        initViews(newFragment)
//        initListeners()
//        initViewsInitialData()
//
//        return newFragment
//    }
//
//    private fun initViewsInitialData() {
//        viewModel.newDrug.observe(
//            requireActivity(),
//            Observer { toastMessage ->
//                toastMessage?.let {
//                    newDrugName.text = it.drug_name
//                }
//            })
//
//        //  initiate start date
//        val calendar = Calendar.getInstance()
//        setDateLabel(
//            calendar.get(Calendar.DAY_OF_MONTH),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.YEAR)
//        )
//
//        //  initiate start time
//        setTimeLabel(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
//    }
//
//    private fun initListeners() {
//        drugOccurrencesDate.setOnClickListener {
//            showDatePickerDialog()
//        }
//
//        drugOccurrencesTime.setOnClickListener {
//            showTimePickerDialog()
//        }
//
//        newDrugName.setOnLongClickListener {
//            viewModel.newDrug.value?.drug_name?.let { drugName ->
//                SnackBar.showToastBar(
//                    activity,
//                    drugName
//                )
//            }
//            return@setOnLongClickListener true
//        }
//    }
//
//    private fun initViews(fragment: View) {
//        newDrugName = fragment.findViewById(R.id.ndo_new_drug_name)
//        drugOccurrencesDate = fragment.findViewById(R.id.ndo_first_occurrence_date)
//        drugOccurrencesTime = fragment.findViewById(R.id.ndo_first_occurrence_time)
//        drugRepeatSpinner = fragment.findViewById(R.id.ndo_repeat_spinner)
//        drugRepeatContainer = fragment.findViewById(R.id.ndo_repeat_container)
//    }
//
//    private fun setTimeLabel(hour: Int, minutes: Int) {
//        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.HOUR_OF_DAY, hour)
//        calendar.set(Calendar.MINUTE, minutes)
//        val calTime = calendar.time
//        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
//
//        drugOccurrencesTime.text = formatter.format(calTime)
//    }
//
//    private fun setDateLabel(day: Int, month: Int, year: Int) {
//        val calendar = Calendar.getInstance()
//        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//        calendar.set(year, month, day)
//        // Display Selected date in textbox
//        drugOccurrencesDate.text = sdf.format(calendar.time)
//    }
//
//    private fun showTimePickerDialog() {
//        val calendar = Calendar.getInstance()
//
//        val tpd =
//            activity?.let {
//                TimePickerDialog(
//                    it,
//                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
//                        setTimeLabel(hourOfDay, minute)
//                    },
//                    calendar.get(Calendar.HOUR_OF_DAY),
//                    calendar.get(Calendar.MINUTE),
//                    true
//                )
//            }
//
//        tpd?.show()
//    }
//
//    private fun showDatePickerDialog() {
//        val calendar = Calendar.getInstance()
//        val year = calendar.get(Calendar.YEAR)
//        val month = calendar.get(Calendar.MONTH)
//        val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//        val dpd =
//            activity?.let {
//                DatePickerDialog(
//                    it,
//                    DatePickerDialog.OnDateSetListener { _, yearSelected, monthOfYear, dayOfMonth ->
//                        setDateLabel(dayOfMonth, monthOfYear, yearSelected)
//                    },
//                    year,
//                    month,
//                    day
//                )
//            }
//
//        dpd?.show()
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        //  todo get email and name
//        return when (item.itemId) {
//            R.id.ndo_menu_add_drug -> {
//                viewModel.addNewDrugToUser(
//                    profileViewModel.getCurrentEmail(),
//                    profileViewModel.getCurrentProfileName()
//                )
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.new_drug_occurrence_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param newDrug Drug to add
//         * @return A new instance of fragment NewDrugOccurrencesFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(newDrug: Drug) =
//            NewDrugOccurrencesFragment().apply {
//                arguments = Bundle().apply {
//                    putParcelable(ARG_NEW_DRUG, newDrug)
//                }
//            }
//    }
//}