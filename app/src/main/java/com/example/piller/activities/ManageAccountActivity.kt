package com.example.piller.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.viewModels.ManageAccountViewModel
import org.json.JSONObject


class ManageAccountActivity : ActivityWithUserObject() {
    private lateinit var viewModel: ManageAccountViewModel
    private lateinit var supervisorsLayout: ConstraintLayout
    private lateinit var emailLayout: ConstraintLayout
    private lateinit var passwordLayout: ConstraintLayout
    private lateinit var deleteLayout: ConstraintLayout
    private lateinit var currentEmailTV: TextView
    private lateinit var showNotificationSW: Switch
    private var userDataChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModels()
        initUserObject(intent)
        viewModel.loggedUserEmail.value = _loggedUserObject.email

        setContentView(R.layout.activity_manage_account)
        //  todo remove this after toolbar is no longer needed
        //  supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initViews()
        setOnClickListeners()
        setViewModelsObservers()
    }

    private fun initViewModels() {
        viewModel = ViewModelProvider(this).get(ManageAccountViewModel::class.java)
    }

    private fun setViewModelsObservers() {
        viewModel.loggedUserEmail.observe(this, Observer { loggedEmail ->
            //update current logged email
            currentEmailTV.text = loggedEmail
        })

        viewModel.snackBarMessage.observe(this, Observer { message ->
            run {
                if (message.isNotEmpty()) {
                    SnackBar.showToastBar(this, message)
                }
            }
        })

        viewModel.goToLoginActivity.observe(this, Observer { goToLoginActivity ->
            if (goToLoginActivity) {
                //  go back to login activity
                val intent = Intent(applicationContext, LoginActivity::class.java)
                AppPreferences.init(this)
                AppPreferences.stayLoggedIn = false
                //  close all previous activities
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        })
    }

    private fun setUpdateEmailDialogViews(
        emailLayout: LinearLayout,
        emailLabel: TextView,
        emailInput: EditText,
        passwordLayout: LinearLayout,
        passwordLabel: TextView,
        passwordInput: EditText
    ) {
        val lp = LinearLayout.LayoutParams(700, 200)
        emailLayout.orientation = LinearLayout.HORIZONTAL
        emailLabel.setPadding(20, 0, 0, 0)
        emailLabel.text = "Enter new email:"
        emailInput.layoutParams = lp
        emailInput.maxLines = 1
        emailLayout.addView(emailLabel)
        emailLayout.addView(emailInput)

        passwordLayout.orientation = LinearLayout.HORIZONTAL
        passwordLabel.setPadding(20, 0, 0, 0)
        passwordLabel.text = "Enter your password:"
        passwordInput.layoutParams = lp
        passwordInput.maxLines = 1
        passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
        passwordLayout.addView(passwordLabel)
        passwordLayout.addView(passwordInput)
    }

    private fun setUpdateEmailDialog() {
        val emailLayout = LinearLayout(this@ManageAccountActivity)
        val emailLabel = TextView(this@ManageAccountActivity)
        val emailInput = EditText(this@ManageAccountActivity)
        val passwordLayout = LinearLayout(this@ManageAccountActivity)
        val passwordLabel = TextView(this@ManageAccountActivity)
        val passwordInput = EditText(this@ManageAccountActivity)

        setUpdateEmailDialogViews(
            emailLayout,
            emailLabel,
            emailInput,
            passwordLayout,
            passwordLabel,
            passwordInput
        )

        showPopupDialog(
            "Update Email",
            "Update",
            arrayOf(emailLayout, passwordLayout),
            callback = {
                val newEmail = emailInput.text.toString()
                val password = passwordInput.text.toString()
                if (viewModel.loggedUserEmail.value != emailInput.text.toString()
                    && newEmail.isNotEmpty()
                    && password.isNotEmpty()
                ) {
                    viewModel.updateUserEmail(_loggedUserObject, newEmail, password)
                    userDataChanged = true
                }
            })
    }

    private fun setOnClickListeners() {
        emailLayout.setOnClickListener { setUpdateEmailDialog() }

        passwordLayout.setOnClickListener { setUpdatePasswordDialog() }

        deleteLayout.setOnClickListener { setDeleteAccountDialog() }

        supervisorsLayout.setOnClickListener {
            val intent = Intent(this@ManageAccountActivity, SupervisorsActivity::class.java)
            putLoggedUserObjectInIntent(intent)
            startActivity(intent)
        }

        showNotificationSW.setOnClickListener {
            AppPreferences.showNotifications = showNotificationSW.isChecked
        }
    }

    private fun setDeleteAccountDialog() {
        val label = TextView(this@ManageAccountActivity)
        label.text = "Are you sure you want to delete your account?"
        label.setPadding(85, 50, 0, 0)
        showPopupDialog(
            "Delete Account",
            "Delete",
            arrayOf(label),
            callback = { viewModel.deleteUser(_loggedUserObject) })
    }

    private fun setUpdatePasswordDialog() {
        val oldPassLayout = LinearLayout(this@ManageAccountActivity)
        val oldPasswordLabel = TextView(this@ManageAccountActivity)
        val oldPasswordInput = EditText(this@ManageAccountActivity)

        val newPasswordLayout = LinearLayout(this@ManageAccountActivity)
        val newPasswordLabel = TextView(this@ManageAccountActivity)
        val newPasswordInput = EditText(this@ManageAccountActivity)

        val newPasswordConfLayout = LinearLayout(this@ManageAccountActivity)
        val newPasswordConfLabel = TextView(this@ManageAccountActivity)
        val confNewPasswordInput = EditText(this@ManageAccountActivity)

        initiateDialogViews(
            oldPassLayout,
            oldPasswordLabel,
            oldPasswordInput,
            newPasswordLayout,
            newPasswordLabel,
            newPasswordInput,
            newPasswordConfLayout,
            newPasswordConfLabel,
            confNewPasswordInput
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
                updatePasswordCallback(oldPasswordInput, newPasswordInput, confNewPasswordInput)
            })
    }

    private fun updatePasswordCallback(
        oldPasswordInput: EditText,
        newPasswordInput: EditText,
        confNewPasswordInput: EditText
    ) {
        val oldPassword = oldPasswordInput.text.toString()
        val newPassword = newPasswordInput.text.toString()
        val confNewPassword = confNewPasswordInput.text.toString()
        if (arePasswordsValid(oldPassword, newPassword, confNewPassword)) {
            val updatedUser = JSONObject()
            updatedUser.put("email", viewModel.loggedUserEmail.value)
            updatedUser.put("password", newPassword)
            updatedUser.put("oldPassword", oldPassword)
            viewModel.updatePassword(_loggedUserObject, updatedUser)
        }
    }


    private fun arePasswordsValid(
        oldPassword: String,
        newPassword: String,
        confNewPassword: String
    ): Boolean {
        var valid = true
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confNewPassword.isEmpty()) {
            valid = false
        } else if (newPassword != confNewPassword) {
            valid = false
        } else if (newPassword == oldPassword) {
            valid = false
        }

        return valid
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

        //  Add all views to the layout
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
        supervisorsLayout = findViewById(R.id.ma_manage_supervisors_layout)

        showNotificationSW = findViewById(R.id.ma_show_notifications)
        showNotificationSW.isChecked = AppPreferences.showNotifications
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (userDataChanged) {
            val intent = Intent(this, CalendarActivity::class.java)
            putLoggedUserObjectInIntent(intent)
            startActivity(intent)
        }
        super.onBackPressed()
    }
}