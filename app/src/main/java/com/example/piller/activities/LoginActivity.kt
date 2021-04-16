package com.example.piller.activities

import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.ServiceBuilder
import com.example.piller.fragments.LoginFragments.SplashScreenFragment
import com.example.piller.notif.NotificationHelper
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.MainActivityViewModel
import io.reactivex.disposables.CompositeDisposable

class LoginActivity: AppCompatActivity() {
    private var _compositeDisposable = CompositeDisposable()
    lateinit var _loadingScreen: RelativeLayout
    private lateinit var _viewModel: MainActivityViewModel


    override fun onStop() {
        _compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        supportActionBar?.hide()
        initViews()
        _viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        AppPreferences.init(this)
        ServiceBuilder.updateRetrofit(DbConstants.SERVER_URL)
        createChannelForNotification()
        initializeFragment(savedInstanceState)
    }



    private fun createChannelForNotification() {
        NotificationHelper.createNotificationChannel(
            this, true, getString(R.string.app_name), NotificationManagerCompat.IMPORTANCE_HIGH
        )
    }

    private fun initViews() {
        _loadingScreen = findViewById(R.id.loading_screen)
    }


    private fun initializeFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val fragmentTransaction: FragmentTransaction =
                supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.login_fragment, SplashScreenFragment())
            fragmentTransaction.commit()
        }
    }


}

