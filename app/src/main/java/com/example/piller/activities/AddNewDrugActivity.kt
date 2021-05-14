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
import com.example.piller.fragments.AddDrugFragments.DrugByImageFragment
import com.example.piller.fragments.AddDrugFragments.DrugByNameFragment
import com.example.piller.fragments.AddDrugFragments.InteractionPopupFragment
import com.example.piller.listAdapters.NewDrugByNameAdapter
import com.example.piller.models.DrugObject
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugSearchViewModel

class AddNewDrugActivity : ActivityWithUserObject() {
    private lateinit var _searchViewModel: DrugSearchViewModel
    private lateinit var _drugOptionsList: RecyclerView
    private lateinit var _loadingScreen: RelativeLayout
    private lateinit var _selectDrugAnywayBtn: Button
    private lateinit var _toolbarTitle: TextView
    private lateinit var _toolbarBackBtn: ImageButton
    private lateinit var _drugAdapter: NewDrugByNameAdapter
    private lateinit var _addType: String
    private lateinit var _drugSearchNoResult: String
    private var _isSearchingByName = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _addType = intent.getStringExtra(DbConstants.ADD_DRUG_TYPE)!!
        initUserObject(intent)
        val calendarId = intent.getStringExtra(DbConstants.CALENDAR_ID)!!
        initViewModels(calendarId)
        setContentView(R.layout.activity_add_new_drug)
        initViews()
        initListeners()
        initRecyclersAndAdapters()
        initObservers()
        selectFragment(savedInstanceState, _addType)
    }

    private fun initListeners() {
        _selectDrugAnywayBtn.setOnClickListener {
            if (_searchViewModel.drugSearchNoResult.value != null) {
                //  no need to show interaction
                _searchViewModel.newDrug.value =
                    DrugObject(
                        DbConstants.defaultStringValue, //  drugid is empty because it's a new drug and we didn't save it in db
                        _searchViewModel.calendarId,
                        _searchViewModel.drugSearchNoResult.value!!,
                        DbConstants.defaultRxcui
                    )
                goToAddOccurrenceActivity()
            } else {
                SnackBar.showToastBar(this, getString(R.string.chooseValidDrug))
            }
        }

        _toolbarTitle.setOnClickListener {
            onBackPressed()
        }
        _toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initViewModels(calendarId: String) {
        _searchViewModel = ViewModelProvider(this).get(DrugSearchViewModel::class.java)
        _searchViewModel.calendarId = calendarId
    }

    private fun selectFragment(savedInstanceState: Bundle?, fragmentID: String) {
        when (fragmentID) {
            DbConstants.DRUG_BY_PILL -> {
                _isSearchingByName = false
                val drugByImageFragment = DrugByImageFragment.newInstance(DbConstants.DRUG_BY_PILL)
                initializeFragment(savedInstanceState, drugByImageFragment)
                _toolbarTitle.text = getString(R.string.searchByPillImage)
            }
            DbConstants.DRUG_BY_BOX -> {
                _isSearchingByName = false
                val drugByBoxFragment = DrugByImageFragment.newInstance(DbConstants.DRUG_BY_BOX)
                initializeFragment(savedInstanceState, drugByBoxFragment)
                _toolbarTitle.text = getString(R.string.searchByBoxImage)
            }
            DbConstants.DRUG_BY_NAME -> {
                _isSearchingByName = true
                val drugByNameFragment = DrugByNameFragment.newInstance()
                initializeFragment(savedInstanceState, drugByNameFragment)
                _toolbarTitle.text = getString(R.string.searchByName)
            }
            else -> {
                _toolbarTitle.text = DbConstants.defaultStringValue
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
        _searchViewModel.addedDrugSuccess.observe(
            this,
            Observer {
                //  added drug successfully, close activity
                if (it) {
                    finish()
                }
            })

        _searchViewModel.showLoadingScreen.observe(
            this,
            Observer {
                if (it) {
                    _loadingScreen.visibility = View.VISIBLE
                } else {
                    _loadingScreen.visibility = View.GONE
                }
            })

        _searchViewModel.drugsSearchResult.observe(this, Observer {
            if (_isSearchingByName) {
                _selectDrugAnywayBtn.visibility = View.VISIBLE
            }
            updateRecyclersAndAdapters()
            setButtonsEnabled(true)
        })

        _searchViewModel.drugSearchNoResult.observe(this, Observer {
            if (it.isNotEmpty()) {
                _drugSearchNoResult = it
            }
        })

        _searchViewModel.snackBarMessage.observe(this, Observer { message ->
            SnackBar.showToastBar(this, message)
        })

        _searchViewModel.drugsInteractionResult.observe(this, Observer {
            if (it.isNotBlank()) {
                //go to fragment with the interactions
                val popupInteraction: InteractionPopupFragment =
                    InteractionPopupFragment.newInstance(it)
                supportFragmentManager.let { fm ->
                    popupInteraction.show(fm, getString(R.string.interactionPopupFragment))
                }
            } else {
                //no interactions
                goToAddOccurrenceActivity()
            }
        })

    }

    private fun initViews() {
        _selectDrugAnywayBtn = findViewById(R.id.nd_select_anyway_btn)
        _loadingScreen = findViewById(R.id.loading_screen)
        _toolbarTitle = findViewById(R.id.nd_toolbar_title)
        _toolbarBackBtn = findViewById(R.id.nd_toolbar_back_button)
    }

    private fun updateRecyclersAndAdapters() {
        _searchViewModel.drugsSearchResult.value?.let { _drugAdapter.setData(it) }
        _drugAdapter.notifyDataSetChanged()
    }

    private fun initRecyclersAndAdapters() {
        _drugOptionsList = findViewById(R.id.nd_drug_options_list)
        _drugOptionsList.layoutManager = LinearLayoutManager(this)
        _drugAdapter = NewDrugByNameAdapter(
            mutableListOf(),
            clickOnItemListener = { drug -> clickOnDrug(drug) })

        _drugOptionsList.adapter = _drugAdapter
    }

    private fun clickOnDrug(rxcui: Int) {
        val drug = _searchViewModel.getDrugByRxcui(rxcui)
        if (drug != null) {
            _searchViewModel.newDrug.value = drug
            _searchViewModel.getInteractionList(loggedUserObject, drug.rxcui)
        }
    }

    fun setButtonsEnabled(enabled: Boolean) {
        if (_isSearchingByName) {
            _selectDrugAnywayBtn.isEnabled = enabled
        }
    }

    fun goToAddOccurrenceActivity() {
        val intent = Intent(this, DrugOccurrenceActivity::class.java)
        intent.putExtra(DbConstants.DRUG_OBJECT, _searchViewModel.newDrug.value!!)
        putLoggedUserObjectInIntent(intent)
        startActivity(intent)
    }
}