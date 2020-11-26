package com.example.piller.activities

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.UserAPI
import com.example.piller.utilities.DbConstants


class ManageAccountActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
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
        initListeners()
    }

    private fun initListeners() {
        emailLayout.setOnClickListener {
            run {
                val inputLayout = LinearLayout(this@ManageAccountActivity)
                val label = TextView(this@ManageAccountActivity)
                val input = EditText(this@ManageAccountActivity)
                val lp = LinearLayout.LayoutParams(700, 200)

                inputLayout.orientation = LinearLayout.HORIZONTAL
                label.setPadding(20, 0, 0, 0)
                label.text = "Enter new email:"
                input.layoutParams = lp
                input.maxLines = 1
                inputLayout.addView(label)
                inputLayout.addView(input)

                showPopupDialog(
                    "Update Email",
                    "Update",
                    arrayOf(inputLayout),
                    callback = {
                        if (loggedUserEmail != input.text.toString()) {
                            SnackBar.showSnackBar(
                                this@ManageAccountActivity,
                                "TODO update email"
                            )
                        }
                    })
            }
        }

        passwordLayout.setOnClickListener {
            runPasswordClickEvent()
        }

        deleteLayout.setOnClickListener {
            val label = TextView(this@ManageAccountActivity)
            label.text = "Are you sure you want to delete your account?"
            label.setPadding(80, 0, 0, 0)
            showPopupDialog(
                "Delete Account",
                "Delete",
                arrayOf(label),
                callback = {
                    SnackBar.showSnackBar(
                        this@ManageAccountActivity,
                        "TODO delete account"
                    )
                })
        }
    }

    private fun runPasswordClickEvent() {
        val oldPassLayout = LinearLayout(this@ManageAccountActivity)
        val oldPasswordLabel = TextView(this@ManageAccountActivity)
        val oldPassword = EditText(this@ManageAccountActivity)

        val newPasswordLayout = LinearLayout(this@ManageAccountActivity)
        val newPasswordLabel = TextView(this@ManageAccountActivity)
        val newPassword = EditText(this@ManageAccountActivity)

        val newPasswordConfLayout = LinearLayout(this@ManageAccountActivity)
        val newPasswordConfLabel = TextView(this@ManageAccountActivity)
        val newPasswordConf = EditText(this@ManageAccountActivity)

        initiateDialogViews(
            oldPassLayout,
            oldPasswordLabel,
            oldPassword,
            newPasswordLayout,
            newPasswordLabel,
            newPassword,
            newPasswordConfLayout,
            newPasswordConfLabel,
            newPasswordConf
        )

        showPopupDialog(
            "Update Password",
            "Update",
            arrayOf(
                oldPassLayout,
                newPasswordLayout,
                newPasswordConfLayout
            ),
            callback = {
                run {
                    SnackBar.showSnackBar(
                        this@ManageAccountActivity,
                        "TODO update password"
                    )
                }
            })
    }

    private fun initiateDialogViews(
        oldPassLayout: LinearLayout,
        oldPasswordLabel: TextView,
        oldPassword: EditText,
        newPasswordLayout: LinearLayout,
        newPasswordLabel: TextView,
        newPassword: EditText,
        newPasswordConfLayout: LinearLayout,
        newPasswordConfLabel: TextView,
        newPasswordConf: EditText
    ) {
        val lp = LinearLayout.LayoutParams(700, 200)
        oldPassLayout.orientation = LinearLayout.HORIZONTAL
        oldPasswordLabel.text = "Old Password:"
        oldPasswordLabel.setPadding(20, 0, 0, 0)
        oldPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        oldPassword.layoutParams = lp
        oldPassword.maxLines = 1
        oldPassLayout.addView(oldPasswordLabel)
        oldPassLayout.addView(oldPassword)

        newPasswordLayout.orientation = LinearLayout.HORIZONTAL
        newPasswordLabel.text = "New Password:"
        newPasswordLabel.setPadding(20, 0, 0, 0)
        newPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        newPassword.layoutParams = lp
        newPassword.maxLines = 1
        newPasswordLayout.addView(newPasswordLabel)
        newPasswordLayout.addView(newPassword)

        newPasswordConfLayout.orientation = LinearLayout.HORIZONTAL
        newPasswordConfLabel.text = "Confirm Password:"
        newPasswordConfLabel.setPadding(20, 0, 0, 0)
        newPasswordConf.transformationMethod = PasswordTransformationMethod.getInstance()
        newPasswordConf.layoutParams = lp
        newPasswordConf.maxLines = 1
        newPasswordConfLayout.addView(newPasswordConfLabel)
        newPasswordConfLayout.addView(newPasswordConf)
    }

    private fun showPopupDialog(
        title: String,
        yesLabel: String,
        inputs: Array<View>,
        callback: () -> Unit
    ) {
        val layout = LinearLayout(this@ManageAccountActivity)
        layout.orientation = LinearLayout.VERTICAL
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@ManageAccountActivity)
        alertDialog.setTitle(title)
        for (input in inputs) {
            layout.addView(input)
        }

        alertDialog.setView(layout)

        alertDialog.setPositiveButton(yesLabel) { _, _ ->
            callback()
        }

        alertDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

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