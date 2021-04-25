package com.example.piller.fragments.LoginFragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.activities.LoginActivity
import com.example.piller.viewModels.LoginActivityViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


const val RC_SIGN_IN = 123

class LoginFragment : Fragment() {
    private lateinit var _fragmentView: View
    private lateinit var _loginButton: Button
    private lateinit var _emailEdt: EditText
    private lateinit var _passwordEdt: EditText
    lateinit var _loadingScreen: RelativeLayout
    private lateinit var _signInButton: SignInButton
    private lateinit var _mGoogleSignInClient: GoogleSignInClient

    private val _viewModel: LoginActivityViewModel by activityViewModels()
    private lateinit var _auth: FirebaseAuth
    private val TAG = "LOGIN_TAG"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.login_layout, container, false)
        _loadingScreen = (activity as LoginActivity)._loadingScreen
        initView()
        setClickListeners()
        googleLogin()

        return _fragmentView
    }

    private fun googleLogin() {
        _auth = Firebase.auth
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        _mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

//        val currentUser = _auth.currentUser
//        updateUI(currentUser)
    }

    private fun updateUI(account: FirebaseUser?) {
        if (account != null) {
            val personName = account.displayName
            val personEmail = account.email
            _viewModel.getGoogleUser(personEmail!!,personName!!)

        } else {
            SnackBar.showToastBar(context, "Could not log in via Google.")
        }

    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _auth.signInWithCredential(credential)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = _auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun initView() {
        _loginButton = _fragmentView.findViewById<View>(R.id.login_button_login_screen) as Button
        _emailEdt = _fragmentView.findViewById<View>(R.id.edt_email_login) as EditText
        _passwordEdt = _fragmentView.findViewById<View>(R.id.edt_password_login) as EditText

        // Set the dimensions of the sign-in button.
        _signInButton = _fragmentView.findViewById(R.id.sign_in_button)
        _signInButton.setSize(SignInButton.SIZE_WIDE)
    }

    private fun setClickListeners() {
        _loginButton.setOnClickListener {
            loginButtonListener()
        }

        _fragmentView.findViewById<TextView>(R.id.login_reset_password).setOnClickListener {
            forgotPassword()
        }

        _signInButton.setOnClickListener {
            val signInIntent: Intent = _mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun loginButtonListener() {
        val emailInput = _emailEdt.text.toString().trim()
        val passwordInput = _passwordEdt.text.toString().trim()

        if (emailInput.isNotEmpty() && passwordInput.isNotEmpty()) {
            //  show loading screen
            _loadingScreen.visibility = View.VISIBLE
            _viewModel.loginUser(emailInput, passwordInput)
        } else {
            SnackBar.showToastBar(
                context,
                "Email or Password cannot be null or empty."
            )
        }
    }

    private fun forgotPassword() {
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
}