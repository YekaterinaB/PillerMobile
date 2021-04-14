package com.example.piller.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.fragments.FullViewFragment
import com.example.piller.fragments.ProfileFragment
import com.example.piller.fragments.WeeklyCalendarFragment
import com.example.piller.intakeReminders.NotificationService
import com.example.piller.models.CalendarEvent
import com.example.piller.models.Profile
import com.example.piller.notif.NotificationHelper
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.ProfileViewModel
import com.example.piller.viewModels.WeeklyCalendarViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class CalendarActivity : AppCompatActivity() {
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var weeklyCalendarViewModel: WeeklyCalendarViewModel

    private lateinit var loggedUserEmail: String
    private lateinit var currentProfile: String
    private lateinit var currentProfileTV: TextView
    private lateinit var toolbarBottom: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loggedUserEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        currentProfile = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        setContentView(R.layout.activity_calendar)
        startNotificationsService()
        currentProfileTV = findViewById(R.id.calendar_current_profile)
        initializeNavigations()

        //initiate view model
        initializeViewModels()
        initializeFragment(savedInstanceState)
    }

    private fun startNotificationsService() {
        startService(Intent(this, NotificationService::class.java))

//        NotificationHelper.createNotificationChannel(
//            this, true, getString(R.string.app_name), NotificationManagerCompat.IMPORTANCE_HIGH
//        )
//        BackgroundNotificationScheduler.scheduleNotificationsForAllProfiles(this)
    }

    private fun initializeViewModels() {
        weeklyCalendarViewModel = ViewModelProvider(this).get(WeeklyCalendarViewModel::class.java)
        weeklyCalendarViewModel.mutableCurrentWeeklyCalendar.value =
            Array(7) { mutableListOf<CalendarEvent>() }

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel.setCurrentProfileAndEmail(currentProfile, loggedUserEmail)
        profileViewModel.mutableListOfProfiles.value = mutableListOf<Profile>()

        profileViewModel.initProfileListFromDB(currentProfile)
        profileViewModel.mutableCurrentProfileName.observe(this, Observer { profile ->
            //update current profile
            profile?.let {
                currentProfileTV.text = it
            }
        })
    }

    private fun initializeFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val f1 = WeeklyCalendarFragment()
            val fragmentTransaction: FragmentTransaction =
                supportFragmentManager.beginTransaction()
            fragmentTransaction.add(
                R.id.calender_weekly_container_fragment,
                f1,
                DbConstants.WEEKLY_CALENDAR_FRAGMENT_ID
            )
            fragmentTransaction.commit()
        }
    }

    private fun initializeNavigations() {
        // upper navigation
        supportActionBar?.title = "Piller"

        //bottom navigation
        toolbarBottom = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.BottomNavigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }


    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    supportActionBar?.title = "Piller"
                    val weeklyCalendarFragment = WeeklyCalendarFragment.newInstance()
                    openFragment(weeklyCalendarFragment, DbConstants.WEEKLY_CALENDAR_FRAGMENT_ID)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_profile -> {
                    supportActionBar?.title = "Profiles"
                    val profileFragment = ProfileFragment.newInstance()
                    openFragment(profileFragment, DbConstants.PROFILES_FRAGMENT_ID)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.navigation_full_view -> {
                    // todo don't create new one if already in full view!
                    supportActionBar?.title = "Full View"
                    val fullViewFragment = FullViewFragment.newInstance()
                    openFragment(fullViewFragment, DbConstants.FULL_VIEW_FRAGMENT_ID)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private fun openFragment(fragment: Fragment, id_fragment: String) {
        val fragmentTransaction: FragmentTransaction =
            this.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.calender_weekly_container_fragment, fragment, id_fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }


    private fun goToAccountManagement() {
        val intent = Intent(this@CalendarActivity, ManageAccountActivity::class.java)
        intent.putExtra(DbConstants.LOGGED_USER_EMAIL, loggedUserEmail)
        intent.putExtra(DbConstants.LOGGED_USER_NAME, currentProfile)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_manage_account -> {
                goToAccountManagement()
                true
            }
            R.id.menu_help -> {
                val intent = Intent(this@CalendarActivity, HelpActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_logout -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        AppPreferences.init(this)
        val intent = Intent(this@CalendarActivity, MainActivity::class.java)
        AppPreferences.loggedOut = true
        startActivity(intent)
        finish()
//        super.onBackPressed()
    }

    override fun onDestroy() {
        //  if the user chose to logout (by pressing back button or by pressing logout) -
        //  then we should not set notifications
        if (AppPreferences.loggedOut) {
            stopService(Intent(this, NotificationService::class.java))
            NotificationHelper.closeNotificationChannel(this, getString(R.string.app_name))
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.mainmenu, menu)
        return true
    }
}
