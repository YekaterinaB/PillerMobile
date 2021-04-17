package com.example.piller.fragments.LoginFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.piller.R
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.viewModels.LoginActivityViewModel
import kotlinx.android.synthetic.main.sign_up_layout.view.*

class SignUpFragment : Fragment() {
    private lateinit var _fragmentView: View
    private lateinit var _signUpButton: Button
    private lateinit var _fullNameEdt: EditText
    private lateinit var _emailEdt: EditText
    private lateinit var _passwordEdt: EditText
    private val _viewModel: LoginActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.sign_up_layout, container, false)
        initView()
        setClickListeners()
        initObservers()
        return _fragmentView
    }

    private fun initView() {
        _signUpButton = _fragmentView.findViewById<View>(R.id.sign_up_button_sign_up) as Button
        _emailEdt = _fragmentView.findViewById<View>(R.id.edt_email_sign_up) as EditText
        _fullNameEdt = _fragmentView.findViewById<View>(R.id.edt_fullname_sign_up) as EditText
        _passwordEdt = _fragmentView.findViewById<View>(R.id.edt_password_sign_up) as EditText
    }

    private fun initObservers() {
        _viewModel.mutableActivitySignUpChangeResponse.observe(
            viewLifecycleOwner,
            Observer { response ->
                response?.let {
                    if (response) {
                        // sign up succeeded
                        goToSplashScreen()
                    }
                }
            })
    }

    private fun goToSplashScreen() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(R.id.login_fragment, SplashScreenFragment())
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }

    private fun setClickListeners() {

        _fragmentView.sign_up_button_sign_up.setOnClickListener {
            AppPreferences.showNotifications = true
            _viewModel.registerUser(
                _emailEdt.text.toString().trim(),
                _fullNameEdt.text.toString().trim(),
                _passwordEdt.text.toString().trim()
            )
        }

        _fragmentView.cancel_sign_up.setOnClickListener {
            goToSplashScreen()
        }

        _emailEdt.addTextChangedListener(signUpTextWatcher)
        _fullNameEdt.addTextChangedListener(signUpTextWatcher)
        _passwordEdt.addTextChangedListener(signUpTextWatcher)

    }

    private val signUpTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val emailInput = _emailEdt.text.toString().trim()
            val nameInput = _fullNameEdt.text.toString().trim()
            val passwordInput = _passwordEdt.text.toString().trim()

            if (!emailInput.isEmpty() && !nameInput.isEmpty() && !passwordInput.isEmpty()) {
                _signUpButton.isEnabled = true
                _signUpButton.setBackgroundResource(R.drawable.rounded_shape_primary_color_full)
            } else {
                _signUpButton.isEnabled = false
                _signUpButton.setBackgroundResource(R.drawable.rounded_shape_not_complete_full)
            }
        }
    }

}