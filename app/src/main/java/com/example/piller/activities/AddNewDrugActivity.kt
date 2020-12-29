package com.example.piller.activities

import android.content.Intent
import android.os.Bundle
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
import com.example.piller.fragments.DrugByNameFragment
import com.example.piller.listAdapters.NewDrugByNameAdapter
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugSearchViewModel

class AddNewDrugActivity : AppCompatActivity() {
    private lateinit var searchViewModel: DrugSearchViewModel
    private lateinit var drugOptionsList: RecyclerView
    private lateinit var drugSelectedBtn: Button
    private lateinit var drugAdapter: NewDrugByNameAdapter
    private lateinit var currentProfile: String
    private lateinit var loggedEmail: String
    private lateinit var addType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addType = intent.getStringExtra(DbConstants.ADD_DRUG_TYPE)!!
        currentProfile = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        loggedEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        initViewModels()
        setContentView(R.layout.activity_add_new_drug)
        initViews()
        initRecyclersAndAdapters()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initObservers()
        selectFragment(savedInstanceState, addType)
    }

    private fun initViewModels() {
        searchViewModel = ViewModelProvider(this).get(DrugSearchViewModel::class.java)
    }

    private fun selectFragment(savedInstanceState: Bundle?, fragmentID: String) {
        when (fragmentID) {
            DbConstants.DRUG_BY_CAMERA -> {
                SnackBar.showToastBar(this@AddNewDrugActivity, "Add by Camera!")
            }
            DbConstants.DRUG_BY_BOX -> {
                SnackBar.showToastBar(this@AddNewDrugActivity, "Add by Camera!")
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
            updateRecyclersAndAdapters()
            setButtonsEnabled(true)
        })

        searchViewModel.snackBarMessage.observe(this, Observer { message ->
            SnackBar.showToastBar(this, message)
        })

    }

    private fun initViews() {
        drugSelectedBtn = findViewById(R.id.nd_drug_selected_btn)
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
            val intent = Intent(
                this,
                DrugOccurrenceActivity::class.java
            )
            intent.putExtra(DbConstants.DRUG_OBJECT, drug)
            intent.putExtra(DbConstants.LOGGED_USER_EMAIL, loggedEmail)
            intent.putExtra(DbConstants.LOGGED_USER_NAME, currentProfile)
            startActivity(intent)
        }
    }

    fun setButtonsEnabled(enabled: Boolean) {
        drugSelectedBtn.isEnabled = enabled
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}