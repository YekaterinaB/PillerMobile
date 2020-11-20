package com.example.piller.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.piller.R
import com.example.piller.utilities.*

class CalendarActivity : AppCompatActivity() {
    private lateinit var loggedUserEmail: String
    private lateinit var loggedUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        //  todo: disable going back to login
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        loggedUserEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        loggedUserName = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
    }
}