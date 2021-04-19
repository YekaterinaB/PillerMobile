package com.example.piller.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.fragments.DrugByBoxFragment
import com.example.piller.fragments.DrugByImageFragment
import com.example.piller.fragments.DrugByNameFragment
import com.example.piller.fragments.InteractionPopupFragment
import com.example.piller.listAdapters.NewDrugByNameAdapter
import com.example.piller.models.DrugObject
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugSearchViewModel

class AddNewDrugActivity : ActivityWithUserObject() {
    private lateinit var searchViewModel: DrugSearchViewModel
    private lateinit var drugOptionsList: RecyclerView
    private lateinit var loadingScreen: RelativeLayout
    private lateinit var selectDrugAnywayBtn: Button
    private lateinit var _toolbarTitle: TextView
    private lateinit var _toolbarBackBtn: ImageButton
    private lateinit var drugAdapter: NewDrugByNameAdapter
    private lateinit var addType: String
    private lateinit var drugSearchNoResult: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addType = intent.getStringExtra(DbConstants.ADD_DRUG_TYPE)!!
        initUserObject(intent)
        val calendarId = intent.getStringExtra(DbConstants.CALENDAR_ID)!!
        initViewModels(calendarId)
        setContentView(R.layout.activity_add_new_drug)
        initViews()
        initListeners()
        initRecyclersAndAdapters()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initObservers()
        selectFragment(savedInstanceState, addType)
    }

    private fun initListeners() {
        selectDrugAnywayBtn.setOnClickListener {
            if (searchViewModel.drugSearchNoResult.value != null) {
                //  no need to show interaction
                searchViewModel.newDrug.value =
                    DrugObject(
                        "", //  drugid is empty because it's a new drug and we didn't save it in db
                        searchViewModel.calendarId,
                        searchViewModel.drugSearchNoResult.value!!,
                        0
                    )
                goToAddOccurrenceActivity()
            } else {
                SnackBar.showToastBar(this, "Please choose a valid drug!")
            }
        }

        _toolbarTitle.setOnClickListener {
            //  todo go back to main search fragment
            onBackPressed()
        }
        _toolbarBackBtn.setOnClickListener {
            //  todo go back to main search fragment
            onBackPressed()
        }
    }

    private fun initViewModels(calendarId: String) {
        searchViewModel = ViewModelProvider(this).get(DrugSearchViewModel::class.java)
        searchViewModel.calendarId = calendarId
    }

    private fun selectFragment(savedInstanceState: Bundle?, fragmentID: String) {
        when (fragmentID) {
            DbConstants.DRUG_BY_CAMERA -> {
                val drugByImageFragment = DrugByImageFragment.newInstance()
                initializeFragment(savedInstanceState, drugByImageFragment)
                _toolbarTitle.text = "Search by pill image"
            }
            DbConstants.DRUG_BY_BOX -> {
                val drugByBoxFragment = DrugByBoxFragment.newInstance()
                initializeFragment(savedInstanceState, drugByBoxFragment)
                _toolbarTitle.text = "Search by box image"
            }
            DbConstants.DRUG_BY_NAME -> {
                val drugByNameFragment = DrugByNameFragment.newInstance()
                initializeFragment(savedInstanceState, drugByNameFragment)
                _toolbarTitle.text = "Search by name"
            }
            else -> {
                _toolbarTitle.text = ""
            }
        }
    }

    private fun initializeFragment(savedInstanceState: Bundle?, fragment: Fragment) {
        if (savedInstanceState == null) {
            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.nd_container_fragment, fragment)
            fragmentTransaction.commit()
        }
    }

    private fun initObservers() {
        searchViewModel.addedDrugSuccess.observe(
            this,
            Observer {
                //  added drug successfully, close activity
                if (it) {
                    finish()
                }
            })

        searchViewModel.showLoadingScreen.observe(
            this,
            Observer {
                if (it) {
                    loadingScreen.animate().alpha(1.0F).duration = 500
                    loadingScreen.visibility = View.VISIBLE
                } else {
                    loadingScreen.visibility = View.GONE
                }
            })

        searchViewModel.drugsSearchResult.observe(this, Observer {
            selectDrugAnywayBtn.visibility = View.VISIBLE
            updateRecyclersAndAdapters()
            setButtonsEnabled(true)
        })

        searchViewModel.drugSearchNoResult.observe(this, Observer {
            if (it.isNotEmpty()) {
                drugSearchNoResult = it
            }
        })

        searchViewModel.snackBarMessage.observe(this, Observer { message ->
            SnackBar.showToastBar(this, message)
        })

        searchViewModel.drugsInteractionResult.observe(this, Observer {
            if (it.isNotBlank()) {
                //go to fragment with the interactions
                val popupInteraction: InteractionPopupFragment =
                    InteractionPopupFragment.newInstance(it)
                supportFragmentManager.let { popupInteraction.show(it, "InteractionPopupFragment") }
            } else {
                //no interactions
                goToAddOccurrenceActivity()
            }
        })

    }

    private fun initViews() {
        selectDrugAnywayBtn = findViewById(R.id.nd_select_anyway_btn)
        loadingScreen = findViewById(R.id.loading_screen)
        _toolbarTitle = findViewById(R.id.nd_toolbar_title)
        _toolbarBackBtn = findViewById(R.id.nd_toolbar_back_button)
    }

    private fun updateRecyclersAndAdapters() {
        searchViewModel.drugsSearchResult.value?.let { drugAdapter.setData(it) }
        drugAdapter.notifyDataSetChanged()
    }

    private fun initRecyclersAndAdapters() {
        drugOptionsList = findViewById(R.id.nd_drug_options_list)
        drugOptionsList.layoutManager = LinearLayoutManager(this)
        drugAdapter = NewDrugByNameAdapter(
            mutableListOf(),
            clickOnItemListener = { drug -> clickOnDrug(drug) })

        drugOptionsList.adapter = drugAdapter
    }

    private fun clickOnDrug(rxcui: Int) {
        val drug = searchViewModel.getDrugByRxcui(rxcui)
        if (drug != null) {
            searchViewModel.newDrug.value = drug
            searchViewModel.getInteractionList(_loggedUserObject, drug.rxcui)
        }
    }

    fun setButtonsEnabled(enabled: Boolean) {
        selectDrugAnywayBtn.isEnabled = enabled
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun goToAddOccurrenceActivity() {
        val intent = Intent(this, DrugOccurrenceActivity::class.java)
        intent.putExtra(DbConstants.DRUG_OBJECT, searchViewModel.newDrug.value!!)
        putLoggedUserObjectInIntent(intent)
        startActivity(intent)
    }
}