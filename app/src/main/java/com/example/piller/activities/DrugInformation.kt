package com.example.piller.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.piller.R
import com.example.piller.utilities.DbConstants
import java.util.*

class DrugInformation : AppCompatActivity() {
    lateinit var fullDrugName:String
    lateinit var intakeDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullDrugName=intent.getStringExtra(DbConstants.FULL_DRUG_NAME)!!
        intakeDate=intent.getStringExtra(DbConstants.INTAKE_DATE)!!

        setContentView(R.layout.activity_drug_information)



        if (intent.getBooleanExtra("notification", false)) { //Just for confirmation
//            txtTitleView.text = intent.getStringExtra("title")
//            txtMsgView.text = intent.getStringExtra("message")

        }
    }
}