package com.example.piller.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.ServiceBuilder
import com.example.piller.fragments.LoginFragments.SplashScreenFragment
import com.example.piller.models.Profile
import com.example.piller.models.UserObject
import com.example.piller.notif.AlarmScheduler
import com.example.piller.notif.NotificationHelper
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.LoginActivityViewModel
import io.reactivex.disposables.CompositeDisposable
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private var _compositeDisposable = CompositeDisposable()
    lateinit var _loadingScreen: RelativeLayout
    private lateinit var _viewModel: LoginActivityViewModel

    override fun onStop() {
        _compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        supportActionBar?.hide()
        initViews()
        _viewModel = ViewModelProvider(this).get(LoginActivityViewModel::class.java)
        AppPreferences.init(this)
        ServiceBuilder.updateRetrofit(DbConstants.SERVER_URL)
        createChannelForNotification()
        stayLoggedIn()
        initializeFragment(savedInstanceState)
        initObservers()
    }

    private fun initObservers() {
        _viewModel.mutableToastError.observe(
            this,
            Observer { toastMessage ->
                toastMessage?.let {
                    //  hide loading screen
                    _loadingScreen.visibility = View.GONE
                    SnackBar.showToastBar(this, toastMessage)
                }
            })

        _viewModel.mutableActivityLoginChangeResponse.observe(
            this,
            Observer { response ->
                response?.let {
                    userAuthenticated(response)
                }
            })
    }

    private fun userAuthenticated(response: Response<ResponseBody>) {
        //go to calendar activity with response body given
        //go to the next activity
        val intent = Intent(this, CalendarActivity::class.java)
        val jObject = JSONObject(response.body()!!.string())
        val userObject = createUserObject(
            jObject.get("id").toString(),
            jObject.get("email").toString(),
            jObject.get("profileName").toString(),
            jObject.get("profileId").toString()
        )
        val userBundle = Bundle()
        userBundle.putParcelable(DbConstants.LOGGED_USER_OBJECT, userObject)
        intent.putExtra(DbConstants.LOGGED_USER_BUNDLE, userBundle)
        //  hide loading screen
        _loadingScreen.visibility = View.GONE
        startActivity(intent)
    }

    private fun createUserObject(
        userId: String,
        email: String,
        mainProfileName: String,
        profileId: String
    ): UserObject {
        val profile = Profile(profileId, mainProfileName,"main-user")
        //  current profile = main profile
        return UserObject(userId, email, profile, profile)
    }

    private fun stayLoggedIn() {
        if (AppPreferences.stayLoggedIn) {
            //  run the background service (it has to run from the application for one time so it'll
            //  be able to tun when the device reboots
            AlarmScheduler.runBackgroundService(this)
            _loadingScreen.visibility = View.VISIBLE
            _viewModel.loginUser(AppPreferences.email, AppPreferences.password)
        }
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
//            fragmentTransaction.setCustomAnimations(
//                R.animator.slide_in_left,
//                R.animator.slide_out_right
//            )
            fragmentTransaction.add(R.id.login_fragment, SplashScreenFragment())
            fragmentTransaction.commit()
        }
    }
}

