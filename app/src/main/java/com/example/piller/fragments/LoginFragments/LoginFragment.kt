package com.example.piller.fragments.LoginFragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.activities.CalendarActivity
import com.example.piller.activities.LoginActivity
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login_layout.view.*
import kotlinx.android.synthetic.main.sign_up_layout.view.*
import org.json.JSONObject

class LoginFragment : Fragment() {
    private lateinit var _fragmentView: View
    private lateinit var _loginButton: Button
    private lateinit var _emailEdt: EditText
    private lateinit var _passwordEdt: EditText
    lateinit var _loadingScreen: RelativeLayout

    private val _viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.login_layout, container, false)
        _loadingScreen = (getActivity() as LoginActivity)._loadingScreen
        initView()
        initObservers()
        setClickListeners()

        return _fragmentView
    }


    private fun initObservers() {
        _viewModel.mutableActivityLoginChangeResponse.observe(
            viewLifecycleOwner,
            Observer { response ->
                response?.let {
                    //go to calendar activity with response body given
                    val jObject = JSONObject(response.body()!!.string())
                    //go to the next activity
                    val intent = Intent(
                        context,
                        CalendarActivity::class.java
                    )
                    val userName = jObject.get("name").toString()
                    intent.putExtra(
                        DbConstants.LOGGED_USER_EMAIL,
                        jObject.get("email").toString()
                    )
                    intent.putExtra(
                        DbConstants.LOGGED_USER_NAME,
                        userName
                    )
                    //  hide loading screen

                    _loadingScreen.visibility = View.GONE
                    startActivity(intent)
                }
            })
    }

    private fun initView() {
        _loginButton = _fragmentView.findViewById<View>(R.id.login_button_login_screen) as Button
        _emailEdt = _fragmentView.findViewById<View>(R.id.edt_email_login) as EditText
        _passwordEdt = _fragmentView.findViewById<View>(R.id.edt_password_login) as EditText
    }

    private fun setClickListeners() {

        _fragmentView.login_button_login_screen.setOnClickListener {
            loginButtonListener()
        }

        _fragmentView.login_reset_password.setOnClickListener {
            forgotPassword()
        }
    }

    private fun loginButtonListener(){
        val emailInput = _emailEdt.text.toString().trim()
        val passwordInput = _passwordEdt.text.toString().trim()

        if (!emailInput.isEmpty() && !passwordInput.isEmpty()) {
            //  show loading screen
            _loadingScreen.visibility = View.VISIBLE
            //  remember email and password if the user wants to
            updateAppPreferences(true, emailInput, passwordInput)
            _viewModel.loginUser(emailInput, passwordInput)
        } else {
            SnackBar.showToastBar(context,
                "Email or Password cannot be null or empty."
            )
        }
    }

    private fun forgotPassword(){
        val lp = LinearLayout.LayoutParams(700, 200)
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.HORIZONTAL
        val emailLabel = TextView(context)
        emailLabel.text = "Enter your email:"
        emailLabel.setPadding(20, 0, 0, 0)
        val emailInput = EditText(context)
        emailInput.layoutParams = lp
        emailInput.maxLines = 1
        layout.addView(emailLabel)
        layout.addView(emailInput)
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setTitle("Reset Password")
        alertDialog.setView(layout)

        alertDialog.setPositiveButton("Send") { _, _ ->
            val email = emailInput.text.toString()
            if (email.isNotEmpty()) {
                _viewModel.sendEmailToResetPassword(email)
            }
        }
        alertDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        alertDialog.show()
    }

    private fun updateAppPreferences(stayLogged: Boolean, email: String, password: String) {
        AppPreferences.stayLoggedIn = stayLogged
        AppPreferences.email = email
        AppPreferences.password = password
    }

}