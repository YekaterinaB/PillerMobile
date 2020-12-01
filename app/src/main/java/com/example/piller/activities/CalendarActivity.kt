package com.example.piller.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.fragments.FullViewFragment
import com.example.piller.fragments.ProfileFragment
import com.example.piller.fragments.WeeklyCalendarFragment
import com.example.piller.models.CalendarEvent
import com.example.piller.models.Profile
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.ProfileViewModel
import com.example.piller.viewModels.WeeklyCalendarViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class CalendarActivity : AppCompatActivity() {
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var weeklyCalendarViewModel: WeeklyCalendarViewModel

    private lateinit var loggedUserEmail: String
    private lateinit var mainProfile: String
    private lateinit var currentProfileTV: TextView
    lateinit var toolbar: Toolbar
    lateinit var toolbarBottom: ActionBar


    override fun onCreate(savedInstanceState: Bundle?) {
        //  todo: disable going back to login
        super.onCreate(savedInstanceState)
        loggedUserEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        mainProfile = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!

        setContentView(R.layout.activity_calendar)
        currentProfileTV = findViewById(R.id.calendar_current_profile)
        initializeNavigations()

        //initiate view model
        initializeViewModels()
        initializeFragment(savedInstanceState)

    }


    private fun initializeViewModels() {
        weeklyCalendarViewModel = ViewModelProvider(this).get(WeeklyCalendarViewModel::class.java)
        weeklyCalendarViewModel.mutableCurrentWeeklyCalendar.value= Array(7) { mutableListOf<CalendarEvent>() }

        weeklyCalendarViewModel.mutableCurrentWeeklyCalendar.observe(
            this,
            Observer { calendar ->
                calendar?.let {
                    val myFragment =
                        supportFragmentManager.findFragmentByTag(DbConstants.WEEKLY_CALENDAR_FRAGMENT_ID) as WeeklyCalendarFragment
                    myFragment.changeCurrentCalendar(it)
                    myFragment.updateRecyclersAndAdapters()
                    //update current profile calendar
                    profileViewModel.changeProfileCalendar(it)
                }
            })

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel.setMainProfileAndEmail(mainProfile, loggedUserEmail)
        profileViewModel.mutableListOfProfiles.value = mutableListOf<Profile>()
        profileViewModel.getProfileListFromDB(mainProfile)
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
            fragmentTransaction.add(R.id.container, f1, DbConstants.WEEKLY_CALENDAR_FRAGMENT_ID)
            fragmentTransaction.commit()
        }
    }

    private fun initializeNavigations() {
        // upper navigation
        toolbar = findViewById(R.id.calendar_toolbar)
        toolbar.title = "Piller"
        setSupportActionBar(toolbar)


        //bottom navigation
        toolbarBottom = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.BottomNavigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }


    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    toolbar.title = "Piller"
                    val weeklyCalendarFragment = WeeklyCalendarFragment.newInstance()
                    openFragment(weeklyCalendarFragment, DbConstants.WEEKLY_CALENDAR_FRAGMENT_ID)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_profile -> {
                    toolbar.title = "Profiles"
                    val profileFragment = ProfileFragment.newInstance()
                    openFragment(profileFragment, DbConstants.PROFILES_FRAGMENT_ID)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_drugs -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_supervisors -> {

                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_full_view -> {
                    toolbar.title = "Full View"
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
        fragmentTransaction.replace(R.id.container, fragment, id_fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }


    private fun goToAccountManagement() {
        val intent = Intent(
            this@CalendarActivity,
            ManageAccountActivity::class.java
        )
        intent.putExtra(DbConstants.LOGGED_USER_EMAIL, loggedUserEmail)
        intent.putExtra(DbConstants.LOGGED_USER_NAME, mainProfile)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_manage_account -> {
                goToAccountManagement()
                true
            }
            R.id.menu_help -> {
                SnackBar.showToastBar(
                    this@CalendarActivity,
                    "Help"
                )
                true
            }
            R.id.menu_logout -> {
                SnackBar.showToastBar(
                    this@CalendarActivity,
                    "Logout"
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.mainmenu, menu)
        return true
    }


}
