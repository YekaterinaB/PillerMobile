package com.example.piller.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.fragments.NewDrugByNameFragment
import com.example.piller.fragments.NewDrugOccurrencesFragment
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.AddNewDrugViewModel

class AddNewDrugActivity : AppCompatActivity() {
    private lateinit var viewModel: AddNewDrugViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var addType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_drug)
        initViews()
        initViewModels()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addType = intent.getStringExtra(DbConstants.ADD_DRUG_TYPE)!!

        selectFragment(addType)
    }

    private fun initViewModels() {
        viewModel = ViewModelProvider(this).get(AddNewDrugViewModel::class.java)
    }

    private fun selectFragment(fragmentID: String, data: String = "") {
        when (fragmentID) {
            DbConstants.DRUG_BY_CAMERA -> {
                SnackBar.showToastBar(this@AddNewDrugActivity, "Add by Camera!")
            }
            DbConstants.DRUG_BY_BOX -> {
                SnackBar.showToastBar(this@AddNewDrugActivity, "Add by Camera!")
            }
            DbConstants.DRUG_BY_NAME -> {
                val drugByNameFragment = NewDrugByNameFragment.newInstance()
                openFragment(drugByNameFragment, DbConstants.DRUG_BY_NAME)
            }
            DbConstants.DRUG_OCCURRENCE -> {
                val drugOccurrencesFragment = NewDrugOccurrencesFragment.newInstance(data)
                openFragment(drugOccurrencesFragment, DbConstants.DRUG_OCCURRENCE)
            }
        }
    }

    fun fragmentResult(data: String) {
        SnackBar.showToastBar(this, data)
        selectFragment(DbConstants.DRUG_OCCURRENCE)
    }

    private fun openFragment(fragment: Fragment, id_fragment: String) {
        val fragmentTransaction: FragmentTransaction =
            this.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nd_container, fragment, id_fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.nd_toolbar)
    }
}