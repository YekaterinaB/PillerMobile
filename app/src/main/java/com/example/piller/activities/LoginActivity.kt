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
    lateinit var loadingScreen: RelativeLayout
    private var _compositeDisposable = CompositeDisposable()
    private lateinit var _viewModel: LoginActivityViewModel

    override fun onStop() {
        _compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
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
                    loadingScreen.visibility = View.GONE
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
        val intent = Intent(this, MainActivity::class.java)
        val jObject = JSONObject(response.body()!!.string())
        val userObject = createUserObject(
            jObject.get(getString(R.string.authenticationId)).toString(),
            jObject.get(getString(R.string.authenticationEmail)).toString(),
            jObject.get(getString(R.string.authenticationProfileName)).toString(),
            jObject.get(getString(R.string.authenticationProfileId)).toString()
//            jObject.get(getString(R.string.authenticationGoogleUser)).toString().toBoolean()
        )
        val userBundle = Bundle()
        userBundle.putParcelable(DbConstants.LOGGED_USER_OBJECT, userObject)
        intent.putExtra(DbConstants.LOGGED_USER_BUNDLE, userBundle)

        //  hide loading screen
        loadingScreen.visibility = View.GONE
        startActivity(intent)
    }

    private fun createUserObject(
        userId: String,
        email: String,
        mainProfileName: String,
        profileId: String
    ): UserObject {
        val profile = Profile(profileId, mainProfileName, getString(R.string.mainUserRelation))
        //  current profile = main profile
        return UserObject(userId, email, profile, profile)
    }

    private fun stayLoggedIn() {
        if (AppPreferences.stayLoggedIn) {
            //  run the background service (it has to run from the application for one time so it'll
            //  be able to tun when the device reboots
            AlarmScheduler.runBackgroundService(this)
            loadingScreen.visibility = View.VISIBLE
            _viewModel.loginUser(AppPreferences.email, AppPreferences.password)
        }
    }

    private fun createChannelForNotification() {
        NotificationHelper.createNotificationChannel(
            this, true, getString(R.string.app_name), NotificationManagerCompat.IMPORTANCE_HIGH
        )
    }

    private fun initViews() {
        loadingScreen = findViewById(R.id.loading_screen)
    }


    private fun initializeFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val fragmentTransaction: FragmentTransaction =
                supportFragmentManager.beginTransaction()
//            fragmentTransaction.setCustomAnimations(
//                R.animator.slide_in_left,
//                R.animator.slide_out_right
//            )
            fragmentTransaction.add(
                R.id.login_fragment,
                SplashScreenFragment(),
                DbConstants.SPLASH_FRAGMENT_ID
            )
            fragmentTransaction.addToBackStack(DbConstants.SPLASH_FRAGMENT_ID)
            fragmentTransaction.commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > DbConstants.backStackEntryCountMin) {
//            val tag = backEntry.name
//            val fragment = supportFragmentManager.findFragmentByTag(tag)
            supportFragmentManager.popBackStack(null, DbConstants.activityStackNoFlags)
            val index =
                supportFragmentManager.backStackEntryCount - DbConstants.previousActivityPositionInStack
            val backEntry = supportFragmentManager.getBackStackEntryAt(index)
//            backEntry.name?.let { updateNavBarIcons(it) }
        } else {
            finish()
            super.onBackPressed()
        }
    }
}

