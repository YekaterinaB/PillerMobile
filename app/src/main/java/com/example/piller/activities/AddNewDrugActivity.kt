package com.example.piller.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.fragments.DrugByNameFragment
import com.example.piller.models.Drug
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.AddNewDrugViewModel

class AddNewDrugActivity : AppCompatActivity() {
    private lateinit var viewModel: AddNewDrugViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var addType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addType = intent.getStringExtra(DbConstants.ADD_DRUG_TYPE)!!
        selectFragment(savedInstanceState, addType)
        setContentView(R.layout.activity_add_new_drug)
        initViews()
        initViewModels()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initObservers()
    }

    private fun initializeFragment(savedInstanceState: Bundle?, fragment: Fragment) {
        if (savedInstanceState == null) {
            val fragmentTransaction: FragmentTransaction =
                supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.container, fragment)
            fragmentTransaction.commit()
        }
    }

    private fun initObservers() {
        viewModel.addedDrugSuccess.observe(
            this,
            Observer {
                //  added drug successfully, close activity
                if (it) {
                    finish()
                }
            })
    }

    private fun initViewModels() {
        viewModel = ViewModelProvider(this).get(AddNewDrugViewModel::class.java)
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

    fun fragmentResult(drug: Drug) {
        viewModel.newDrug.value = drug
        selectFragment(DbConstants.DRUG_OCCURRENCE, drug)
    }

    private fun initViews() {
        toolbar = findViewById(R.id.nd_toolbar)
    }
}