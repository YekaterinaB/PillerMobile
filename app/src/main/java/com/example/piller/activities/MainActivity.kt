package com.example.piller.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.Profile
import com.example.piller.models.UserObject
import com.example.piller.notif.AlarmScheduler
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.MainActivityViewModel
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {
    private var compositeDisposable = CompositeDisposable()
    private lateinit var forgotPassword: TextView
    private lateinit var loadingScreen: RelativeLayout
    private lateinit var viewModel: MainActivityViewModel


    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        initViews()
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        initObservers()
        AppPreferences.init(this)
        ServiceBuilder.updateRetrofit(DbConstants.SERVER_URL)
        //  update fields if user chose to remember email and password, and auto login
        if (AppPreferences.stayLoggedIn && !AppPreferences.loggedOut) {
            login_remember.isChecked = true
            edt_email_login.setText(AppPreferences.email)
            edt_password_login.setText(AppPreferences.password)
            //  run the background service (it has to run from the application for one time so it'll
            //  be able to tun when the device reboots
            AlarmScheduler.runBackgroundService(this)
            loginUserWindow(edt_email.text.toString(), edt_password.text.toString())
        }

        setOnClickListeners()
    }

    private fun initObservers() {
        viewModel.mutableToastError.observe(
            this,
            Observer { toastMessage ->
                toastMessage?.let {
                    //  hide loading screen
                    loadingScreen.visibility = View.GONE
                    SnackBar.showToastBar(this, toastMessage)
                }
            })

        viewModel.mutableActivityLoginChangeResponse.observe(
            this,
            Observer { response ->
                response?.let {
                    //go to calendar activity with response body given
                    val jObject = JSONObject(response.body()!!.string())
                    val userObject = createUserObject(
                        jObject.get("id").toString(),
                        jObject.get("email").toString(),
                        jObject.get("mainProfileName").toString()
                    )
                    val userBundle = Bundle()
                    userBundle.putParcelable(DbConstants.LOGGED_USER_OBJECT, userObject)
                    //go to the next activity
                    val intent = Intent(this@MainActivity, CalendarActivity::class.java)
                    intent.putExtra(DbConstants.LOGGED_USER_BUNDLE, userBundle)
                    //  hide loading screen
                    loadingScreen.visibility = View.GONE
                    startActivity(intent)
                }
            })
    }

    private fun createUserObject(
        userId: String,
        email: String,
        mainProfileName: String
    ): UserObject {
        val profile = Profile(userId, mainProfileName)
        return UserObject(userId, email, mainProfileName, profile)
    }

    private fun initViews() {
        forgotPassword = findViewById(R.id.login_reset_password)
        loadingScreen = findViewById(R.id.loading_screen)
    }

    private fun setOnClickListeners() {
        btn_login.setOnClickListener {
            loginUserWindow(edt_email_login.text.toString(), edt_password_login.text.toString())
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
                val edtEmail = itemView.findViewById<View>(R.id.edt_email_login) as MaterialEditText
                val edtName = itemView.findViewById<View>(R.id.edt_name) as MaterialEditText
                val edtPassword =
                    itemView.findViewById<View>(R.id.edt_password_login) as MaterialEditText

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
                AppPreferences.showNotifications = true
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
                //  show loading screen
                loadingScreen.visibility = View.VISIBLE
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
        AppPreferences.stayLoggedIn = stayLogged
        AppPreferences.email = email
        AppPreferences.password = password
        AppPreferences.loggedOut = false
    }
}