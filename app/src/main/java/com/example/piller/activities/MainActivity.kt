package com.example.piller.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.ServiceBuilder
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.MainActivityViewModel
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private var compositeDisposable = CompositeDisposable()
    private lateinit var forgotPassword: TextView
    private lateinit var viewModel: MainActivityViewModel


    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        initObservers()
        AppPreferences.init(this)
        ServiceBuilder.updateRetrofit("http://10.0.2.2:3000")

        //  update fields if user chose to remember email and password
        if (AppPreferences.isLogin) {
            login_remember.isChecked = true
            edt_email.setText(AppPreferences.email)
            edt_password.setText(AppPreferences.password)
        }

        initiateViews()
        setOnClickListeners()
    }

    private fun initObservers() {
        viewModel.mutableToastError.observe(
            this,
            Observer { toastMessage ->
                toastMessage?.let {
                    SnackBar.showToastBar(this, toastMessage)
                }
            })

        viewModel.mutableActivityChangeResponse.observe(
            this,
            Observer { response ->
                response?.let {
                    //go to calendar activity with response body given
                    val jObject = JSONObject(response.body()!!.string())
                    //go to the next activity
                    val intent = Intent(
                        this@MainActivity,
                        CalendarActivity::class.java
                    )
                    intent.putExtra(
                        DbConstants.LOGGED_USER_EMAIL,
                        jObject.get("email").toString()
                    )
                    intent.putExtra(
                        DbConstants.LOGGED_USER_NAME,
                        jObject.get("name").toString()
                    )
                    startActivity(intent)

                }
            })
    }

    private fun initiateViews() {
        forgotPassword = findViewById(R.id.login_reset_password)
    }

    private fun setOnClickListeners() {
        btn_login.setOnClickListener {
            loginUserWindow(edt_email.text.toString(), edt_password.text.toString())
        }

        txt_create_account.setOnClickListener {
            showRegistrationWindow()
        }

        forgotPassword.setOnClickListener {
            val lp = LinearLayout.LayoutParams(700, 200)
            val layout = LinearLayout(this@MainActivity)
            layout.orientation = LinearLayout.HORIZONTAL
            val emailLabel = TextView(this)
            emailLabel.text = "Enter your email:"
            emailLabel.setPadding(20, 0, 0, 0)
            val emailInput = EditText(this)
            emailInput.layoutParams = lp
            emailInput.maxLines = 1
            layout.addView(emailLabel)
            layout.addView(emailInput)
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            alertDialog.setTitle("Reset Password")
            alertDialog.setView(layout)

            alertDialog.setPositiveButton("Send") { _, _ ->
                val email = emailInput.text.toString()
                if (email.isNotEmpty()) {
                    viewModel.sendEmailToResetPassword(email)
                }
            }

            alertDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            alertDialog.show()
        }
    }


    private fun showRegistrationWindow() {
        val itemView = LayoutInflater.from(this@MainActivity)
            .inflate(R.layout.register_layout, null)

        MaterialStyledDialog.Builder(this@MainActivity)
            .setIcon(R.drawable.ic_user)
            .setTitle("REGISTRATION")
            .setDescription("Please fill all fields")
            .setCustomView(itemView)
            .setNegativeText("CANCEL")
            .onNegative { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveText("REGISTER")
            .onPositive(MaterialDialog.SingleButtonCallback { _, _ ->
                val edtEmail = itemView.findViewById<View>(R.id.edt_email) as MaterialEditText
                val edtName = itemView.findViewById<View>(R.id.edt_name) as MaterialEditText
                val edtPassword =
                    itemView.findViewById<View>(R.id.edt_password) as MaterialEditText

                when {
                    TextUtils.isEmpty(edtEmail.text.toString()) -> {
                        SnackBar.showToastBar(
                            this@MainActivity,
                            "Email cannot be null or empty"
                        )
                        return@SingleButtonCallback
                    }
                    TextUtils.isEmpty(edtName.text.toString()) -> {
                        SnackBar.showToastBar(
                            this@MainActivity,
                            "Name cannot be null or empty"
                        )
                        return@SingleButtonCallback
                    }
                    TextUtils.isEmpty(edtPassword.text.toString()) -> {
                        SnackBar.showToastBar(
                            this@MainActivity,
                            "Password cannot be null or empty"
                        )
                        return@SingleButtonCallback
                    }
                }
                viewModel.registerUser(
                    edtEmail.text.toString(),
                    edtName.text.toString(),
                    edtPassword.text.toString()
                )

            })
            .build()
            .show()
    }


    private fun loginUserWindow(email: String, password: String) {
        //  Check if empty
        when {
            TextUtils.isEmpty(email) -> {
                SnackBar.showToastBar(

                    this@MainActivity,
                    "Email cannot be null or empty"
                )
            }
            TextUtils.isEmpty(password) -> {
                SnackBar.showToastBar(
                    this@MainActivity,
                    "Password cannot be null or empty"
                )
            }
            else -> {
                //  remember email and password if the user wants to
                if (login_remember.isChecked) {
                    updateAppPreferences(true, email, password)
                } else {
                    updateAppPreferences(false, "", "")
                }
                viewModel.loginUser(email, password)

            }
        }
    }


    private fun updateAppPreferences(stayLogged: Boolean, email: String, password: String) {
        AppPreferences.isLogin = stayLogged
        AppPreferences.email = email
        AppPreferences.password = password
    }


}