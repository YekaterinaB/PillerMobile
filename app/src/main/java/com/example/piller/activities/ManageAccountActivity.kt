package com.example.piller.activities

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.piller.R
import com.example.piller.utilities.DbConstants


class ManageAccountActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    private lateinit var loggedUserEmail: String
    private lateinit var loggedUserName: String
    private lateinit var emailLayout: ConstraintLayout
    private lateinit var currentEmailTV: TextView
    private lateinit var passwordLayout: ConstraintLayout
    private lateinit var deleteLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)
        toolbar = findViewById(R.id.manage_account_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()

        loggedUserEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        loggedUserName = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!

        setCurrentUserProperties()
    }

    private fun showPopupDialog(title: String, message: String, callback: Unit) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@ManageAccountActivity)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        val input = EditText(this@ManageAccountActivity)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        input.layoutParams = lp
        alertDialog.setView(input)
        alertDialog.setPositiveButton(
            "YES"
        ) { _, _ ->
            callback
        }

        alertDialog.setNegativeButton(
            "NO"
        ) { dialog, _ -> dialog.cancel() }

        alertDialog.show()
    }

    private fun initViews() {
        emailLayout = findViewById(R.id.ma_email_layout)
        currentEmailTV = findViewById(R.id.ma_current_email)
        passwordLayout = findViewById(R.id.ma_password_layout)
        deleteLayout = findViewById(R.id.ma_delete_layout)
    }

    private fun setCurrentUserProperties() {
        currentEmailTV.text = loggedUserEmail
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}