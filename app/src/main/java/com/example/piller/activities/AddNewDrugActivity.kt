package com.example.piller.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.fragments.DrugByImageFragment
import com.example.piller.fragments.DrugByNameFragment
import com.example.piller.fragments.InteractionPopupFragment
import com.example.piller.listAdapters.NewDrugByNameAdapter
import com.example.piller.models.DrugOccurrence
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugSearchViewModel

class AddNewDrugActivity : AppCompatActivity() {
    private lateinit var searchViewModel: DrugSearchViewModel
    private lateinit var drugOptionsList: RecyclerView
    private lateinit var selectDrugAnywayBtn: Button
    private lateinit var drugAdapter: NewDrugByNameAdapter
    private lateinit var currentProfile: String
    private lateinit var loggedEmail: String
    private lateinit var addType: String
    private lateinit var drugSearchNoResult: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addType = intent.getStringExtra(DbConstants.ADD_DRUG_TYPE)!!
        currentProfile = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        loggedEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        initViewModels()
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
            //  no need to show interaction
            searchViewModel.newDrug.value =
                DrugOccurrence(searchViewModel.drugSearchNoResult.value!!, 0)
            goToAddOccurrenceActivity()
        }
    }

    private fun initViewModels() {
        searchViewModel = ViewModelProvider(this).get(DrugSearchViewModel::class.java)
    }

    private fun selectFragment(savedInstanceState: Bundle?, fragmentID: String) {
        when (fragmentID) {
            DbConstants.DRUG_BY_CAMERA -> {
                val drugByImageFragment = DrugByImageFragment.newInstance()
                initializeFragment(savedInstanceState, drugByImageFragment)
            }
            DbConstants.DRUG_BY_BOX -> {
                SnackBar.showToastBar(this@AddNewDrugActivity, "Add by Box!")
            }
            DbConstants.DRUG_BY_NAME -> {
                val drugByNameFragment = DrugByNameFragment.newInstance()
                initializeFragment(savedInstanceState, drugByNameFragment)
            }
        }
    }

    private fun initializeFragment(savedInstanceState: Bundle?, fragment: Fragment) {
        if (savedInstanceState == null) {
            val fragmentTransaction: FragmentTransaction =
                supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.nd_container_fragment, fragment)
            fragmentTransaction.commit()
        }
    }

    private fun updateSelectAnywayButtonVisibility(visible: Boolean) {
        if (visible) {
            selectDrugAnywayBtn.visibility = View.VISIBLE
            selectDrugAnywayBtn.isEnabled = true
        } else {
            selectDrugAnywayBtn.visibility = View.INVISIBLE
            selectDrugAnywayBtn.isEnabled = false
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

        searchViewModel.drugsSearchResult.observe(this, Observer {
//            updateSelectAnywayButtonVisibility(it.isEmpty())
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
            searchViewModel.getInteractionList(loggedEmail, currentProfile, drug.rxcui)
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
        val intent = Intent(
            this,
            DrugOccurrenceActivity::class.java
        )
        intent.putExtra(DbConstants.DRUG_OBJECT, searchViewModel.newDrug.value!!)
        intent.putExtra(DbConstants.LOGGED_USER_EMAIL, loggedEmail)
        intent.putExtra(DbConstants.LOGGED_USER_NAME, currentProfile)
        startActivity(intent)
    }

}