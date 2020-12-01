package com.example.piller.activities

import android.app.AlertDialog
import android.content.Intent
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
import com.example.piller.models.User
import com.example.piller.utilities.DbConstants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response


class ManageAccountActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var loggedUserEmail: String
    private lateinit var loggedUserName: String
    private lateinit var emailLayout: ConstraintLayout
    private lateinit var currentEmailTV: TextView
    private lateinit var passwordLayout: ConstraintLayout
    private lateinit var deleteLayout: ConstraintLayout
    private val retrofit = ServiceBuilder.buildService(UserAPI::class.java)

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
                if (loggedUserEmail != emailInput.text.toString()
                    && newEmail.isNotEmpty()
                    && password.isNotEmpty()
                ) {
                    val updatedUser = User(newEmail, loggedUserName, password)
                    sendRetrofitUpdateEmail(updatedUser, newEmail)
                }
            })
    }

    private fun sendRetrofitUpdateEmail(updatedUser: User, newEmail: String) {
        retrofit.updateUser(loggedUserEmail, updatedUser).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    SnackBar.showToastBar(
                        this@ManageAccountActivity,
                        "Could not update user."
                    )
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        SnackBar.showToastBar(
                            this@ManageAccountActivity,
                            "Error updating account. Please try again later."
                        )
                    } else {
                        currentEmailTV.text = newEmail
                        loggedUserEmail = newEmail
                        SnackBar.showToastBar(
                            this@ManageAccountActivity,
                            "User email updated."
                        )
                    }
                }
            }
        )
    }

    private fun initListeners() {
        emailLayout.setOnClickListener {
            setUpdateEmailDialog()
        }

        passwordLayout.setOnClickListener {
            setUpdatePasswordDialog()
        }

        deleteLayout.setOnClickListener {
            setDeleteAccountDialog()
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
            callback = {
                retrofit.deleteUser(loggedUserEmail).enqueue(
                    object : retrofit2.Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            SnackBar.showToastBar(
                                this@ManageAccountActivity,
                                "Could not delete user."
                            )
                        }

                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.raw().code() == 200) {
                                //  go back to login activity
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                //  close all previous activities
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            } else {
                                SnackBar.showToastBar(
                                    this@ManageAccountActivity,
                                    "Error deleting account. Please try again later."
                                )
                            }
                        }
                    }
                )
                SnackBar.showToastBar(
                    this@ManageAccountActivity,
                    "TODO delete account"
                )
            })
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
            updatedUser.put("email", loggedUserEmail)
            updatedUser.put("password", newPassword)
            updatedUser.put("oldPassword", oldPassword)
            sendRetrofitUpdatePassword(updatedUser)
        }
    }

    private fun sendRetrofitUpdatePassword(updatedUser: JSONObject) {
        retrofit.updatePassword(loggedUserEmail, updatedUser).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    SnackBar.showToastBar(
                        this@ManageAccountActivity,
                        "Could not update password."
                    )
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        SnackBar.showToastBar(
                            this@ManageAccountActivity,
                            jObjError["message"] as String
                        )
                    } else {
                        SnackBar.showToastBar(
                            this@ManageAccountActivity,
                            "User password updated."
                        )
                    }
                }
            }
        )
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
    }

    private fun setCurrentUserProperties() {
        currentEmailTV.text = loggedUserEmail
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}