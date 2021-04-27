package com.example.piller.fragments.LoginFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.piller.R
import com.example.piller.utilities.DbConstants
import kotlinx.android.synthetic.main.login_splash_screen_layout.view.*

class SplashScreenFragment : Fragment() {
    private lateinit var _fragmentView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.login_splash_screen_layout, container, false)
        setOnClickListeners()
        return _fragmentView
    }

    private fun switchFragment(fragment: Fragment, fragmentId: String) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(R.id.login_fragment, fragment, fragmentId)
            transaction.addToBackStack(fragmentId)
            transaction.commit()
        }
    }

    private fun setOnClickListeners() {
        _fragmentView.login_button_splash_screen.setOnClickListener {
            switchFragment(LoginFragment(), DbConstants.LOGIN_FRAGMENT_ID)
        }

        _fragmentView.sign_up_button_splash_screen.setOnClickListener {
            switchFragment(SignUpFragment(), DbConstants.SIGN_IN_FRAGMENT_ID)
        }
    }

    companion object {
        fun newInstance(): SplashScreenFragment = SplashScreenFragment()
    }
}