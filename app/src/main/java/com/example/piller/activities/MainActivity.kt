package com.example.piller.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.fragments.AddDrugFragments.AddDrugOptionsFragment
import com.example.piller.fragments.CalendarFragment.CalendarFragment
import com.example.piller.fragments.FullViewFragment
import com.example.piller.fragments.ProfileFragments.ProfileFragment
import com.example.piller.intakeReminders.NotificationService
import com.example.piller.notif.NotificationHelper
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.ProfileViewModel
import com.example.piller.viewModels.WeeklyCalendarViewModel


class MainActivity : ActivityWithUserObject() {
    private lateinit var _profileViewModel: ProfileViewModel
    private lateinit var _weeklyCalendarViewModel: WeeklyCalendarViewModel
    private lateinit var _calendarNav: ImageView
    private lateinit var _profileNav: ImageView
    private lateinit var _addDrugNav: ImageView

    private lateinit var _currentProfileTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUserObject(intent)
        setContentView(R.layout.main_layout)
        startNotificationsService()
        initViews()
        navigationListeners()

        //initiate view model
        initializeViewModels()
        initObservers()
        initializeFragment(savedInstanceState)
    }


    private fun initViews() {
        _currentProfileTV = findViewById(R.id.calendar_current_profile)
        _calendarNav= findViewById(R.id.calendar_navigation)
        _profileNav =findViewById(R.id.profile_navigation)
        _addDrugNav =findViewById(R.id.add_drug_navigation)

    }

    private fun initObservers(){
        _profileViewModel.mutableCurrentProfile.observe(this, Observer { profile ->
            //  update current profile
            profile?.let {
                _loggedUserObject.currentProfile = it
                _currentProfileTV.text = it.name
            }
        })
    }

    private fun startNotificationsService() {
        val intent = Intent(this, NotificationService::class.java)
        putLoggedUserObjectInIntent(intent)
        startService(intent)

    }

    private fun initializeViewModels() {
        _weeklyCalendarViewModel = ViewModelProvider(this).get(WeeklyCalendarViewModel::class.java)

        _profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        _profileViewModel.setCurrentProfileAndEmail(
            _loggedUserObject.currentProfile!!,
            _loggedUserObject.email
        )
        _profileViewModel.getProfileListFromDB(_loggedUserObject)

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



    private fun navigationListeners(){
        _calendarNav.setOnClickListener{
            goToCalendarLayout()
        }
        _profileNav.setOnClickListener{
            goToProfileLayout()


        }
        _addDrugNav.setOnClickListener{
            goToAddDrugLayout()
        }

    }

    private fun goToAddDrugLayout(){
        val addDrugFragment = AddDrugOptionsFragment.newInstance(_loggedUserObject)
        openFragment(addDrugFragment, DbConstants.ADD_DRUG_FRAGMENT_ID)
        _calendarNav.setImageResource(R.drawable.pill_dark_blue)
        _profileNav.setImageResource(R.drawable.ic_profile_blue)
        _addDrugNav.setImageResource(R.drawable.edit_plus_light_blue)
    }

    private fun goToProfileLayout(){
        val profileFragment = ProfileFragment.newInstance(_loggedUserObject)
        openFragment(profileFragment, DbConstants.PROFILES_FRAGMENT_ID)
        _calendarNav.setImageResource(R.drawable.pill_dark_blue)
        _profileNav.setImageResource(R.drawable.ic_profile_light_blue)
        _addDrugNav.setImageResource(R.drawable.edit_plus_dark)
    }

    private fun goToCalendarLayout(){
        val weeklyCalendarFragment =
            CalendarFragment.newInstance(_loggedUserObject)
        openFragment(weeklyCalendarFragment, DbConstants.CALENDAR_FRAGMENT_ID)
        _calendarNav.setImageResource(R.drawable.ic_pill_light_blue)
        _profileNav.setImageResource(R.drawable.ic_profile_blue)
        _addDrugNav.setImageResource(R.drawable.edit_plus_dark)
    }


    private fun openFragment(fragment: Fragment, id_fragment: String) {
        val fragmentTransaction: FragmentTransaction =
            this.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.calender_weekly_container_fragment, fragment, id_fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }



    override fun onBackPressed() {
        //  todo logout user when he presses back?
        //do not log out, just finish all
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        //AppPreferences.init(this)
        //AppPreferences.stayLoggedIn = false
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
