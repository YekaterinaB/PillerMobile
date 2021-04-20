package com.example.piller.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.fragments.CalendarFragment.CalendarFragment
import com.example.piller.fragments.FullViewFragment
import com.example.piller.fragments.ProfileFragments.ProfileFragment
import com.example.piller.intakeReminders.NotificationService
import com.example.piller.models.CalendarEvent
import com.example.piller.models.CalendarProfile
import com.example.piller.notif.NotificationHelper
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.ProfileViewModel
import com.example.piller.viewModels.WeeklyCalendarViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class CalendarActivity : ActivityWithUserObject() {
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var weeklyCalendarViewModel: WeeklyCalendarViewModel

    private lateinit var currentProfileTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUserObject(intent)
        setContentView(R.layout.activity_calendar)
        startNotificationsService()
        initViews()
        initializeNavigations()

        //initiate view model
        initializeViewModels()
        initializeFragment(savedInstanceState)
    }

    private fun initViews() {
        currentProfileTV = findViewById(R.id.calendar_current_profile)
    }

    private fun startNotificationsService() {
        val intent = Intent(this, NotificationService::class.java)
        putLoggedUserObjectInIntent(intent)
        startService(intent)

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
        profileViewModel.setCurrentProfileAndEmail(
            _loggedUserObject.currentProfile!!,
            _loggedUserObject.email
        )
        profileViewModel.mutableListOfProfiles.value = mutableListOf<CalendarProfile>()

        profileViewModel.getProfileListFromDB(_loggedUserObject)
        profileViewModel.mutableCurrentProfile.observe(this, Observer { profile ->
            //  update current profile
            profile?.let {
                _loggedUserObject.currentProfile = it
                currentProfileTV.text = it.name
            }
        })
    }

    private fun initializeFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val f1 = CalendarFragment.newInstance(_loggedUserObject)
            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(
                R.id.calender_weekly_container_fragment,
                f1,
                DbConstants.CALENDAR_FRAGMENT_ID
            )
            fragmentTransaction.commit()
        }
    }

    private fun initializeNavigations() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.BottomNavigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }


    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val weeklyCalendarFragment =
                        CalendarFragment.newInstance(_loggedUserObject)
                    openFragment(weeklyCalendarFragment, DbConstants.CALENDAR_FRAGMENT_ID)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_profile -> {
                    val profileFragment = ProfileFragment.newInstance(_loggedUserObject)
                    openFragment(profileFragment, DbConstants.PROFILES_FRAGMENT_ID)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.navigation_full_view -> {
                    val fullViewFragment = FullViewFragment.newInstance(_loggedUserObject)
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


//    private fun goToAccountManagement() {
//        val intent = Intent(this@CalendarActivity, ManageAccountActivity::class.java)
//        putLoggedUserObjectInIntent(intent)
//        startActivity(intent)
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.menu_manage_account -> {
//                goToAccountManagement()
//                true
//            }
//            R.id.menu_help -> {
//                val intent = Intent(this@CalendarActivity, HelpActivity::class.java)
//                startActivity(intent)
//                true
//            }
            R.id.menu_logout -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        AppPreferences.init(this)
        val intent = Intent(this@CalendarActivity, LoginActivity::class.java)
        AppPreferences.stayLoggedIn = false
        startActivity(intent)
        finish()
//        super.onBackPressed()
    }

    override fun onDestroy() {
        //  if the user chose to logout (by pressing back button or by pressing logout) -
        //  then we should not set notifications
        if (!AppPreferences.stayLoggedIn) {
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
