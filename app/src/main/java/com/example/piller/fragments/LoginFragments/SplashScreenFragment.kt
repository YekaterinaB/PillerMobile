package com.example.piller.fragments.LoginFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.piller.R
import kotlinx.android.synthetic.main.splash_screen_layout.*
import kotlinx.android.synthetic.main.splash_screen_layout.view.*

class SplashScreenFragment: Fragment() {
    private lateinit var _fragmentView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.splash_screen_layout, container, false)
        setOnClickListeners()
        return _fragmentView
    }

    private fun setOnClickListeners() {
        _fragmentView.login_button_splash_screen.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            if (transaction != null) {
                transaction.replace(R.id.login_fragment, LoginFragment())
                transaction.disallowAddToBackStack()
                transaction.commit()
            }
        }

        _fragmentView.sign_up_button_splash_screen.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            if (transaction != null) {
                transaction.replace(R.id.login_fragment, SignUpFragment())
                transaction.disallowAddToBackStack()
                transaction.commit()
            }

        }
    }
        companion object {
        fun newInstance(): SplashScreenFragment = SplashScreenFragment()
    }
}