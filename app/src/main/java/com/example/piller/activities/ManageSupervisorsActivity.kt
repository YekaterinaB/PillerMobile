package com.example.piller.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.ManageSupervisorsViewModel


class ManageSupervisorsActivity : AppCompatActivity() {
    private lateinit var viewModel: ManageSupervisorsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModels()
        setContentView(R.layout.manage_supervisors_layout)



    }

    private fun initViewModels() {
        viewModel = ViewModelProvider(this).get(ManageSupervisorsViewModel::class.java)
        viewModel.setEmailAndName(
            intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!,
            intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        )
    }
}