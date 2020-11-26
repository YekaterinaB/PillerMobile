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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.EventInterpreter
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.fragments.ProfileFragment
import com.example.piller.fragments.WeeklyCalendarFragment
import com.example.piller.listAdapters.EliAdapter
import com.example.piller.models.CalendarEvent
import com.example.piller.utilities.DbConstants
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response


class CalendarActivity : AppCompatActivity() {
    private lateinit var loggedUserEmail: String
    private lateinit var currentProfile: String
    private lateinit var currentProfileTV: TextView
    lateinit var toolbar: Toolbar
    lateinit var toolbarBottom: ActionBar
    private val eventInterpreter = EventInterpreter()
    private var weekEvents = Array(7) { mutableListOf<CalendarEvent>() }
    private var eliAdapters = mutableListOf<EliAdapter>()
    private var eliRecycles = mutableListOf<RecyclerView>()


    override fun onCreate(savedInstanceState: Bundle?) {
        //  todo: disable going back to login
        super.onCreate(savedInstanceState)
        loggedUserEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        currentProfile = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!

        CoroutineScope(Dispatchers.IO).launch {
            getCalendarByUser(loggedUserEmail, currentProfile)
        }

        setContentView(R.layout.activity_calendar)

        // upper navigation
        toolbar = findViewById(R.id.calendar_toolbar)
        toolbar.title = "Piller"
        setSupportActionBar(toolbar)

        // fragments container
        if (savedInstanceState == null) {
            val f1 = WeeklyCalendarFragment()
            val fragmentTransaction: FragmentTransaction =
                supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.container, f1)
            fragmentTransaction.commit()
        }

        currentProfileTV=findViewById(R.id.calendar_current_profile)
        currentProfileTV.text=currentProfile

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
                    openFragment(weeklyCalendarFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_profile -> {
                    toolbar.title = "Profiles"
                    val profileFragment = ProfileFragment.newInstance()
                    openFragment(profileFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_drugs -> {

                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_supervisors -> {

                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_full_view -> {

                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun initRecyclersAndAdapters() {
        eliRecycles.add(findViewById(R.id.calendar_sunday_list))
        eliRecycles.add(findViewById(R.id.calendar_monday_list))
        eliRecycles.add(findViewById(R.id.calendar_tuesday_list))
        eliRecycles.add(findViewById(R.id.calendar_wednesday_list))
        eliRecycles.add(findViewById(R.id.calendar_thursday_list))
        eliRecycles.add(findViewById(R.id.calendar_friday_list))
        eliRecycles.add(findViewById(R.id.calendar_saturday_list))

        for (i in 0 until 7) {
            eliRecycles[i].layoutManager = LinearLayoutManager(this)
            eliAdapters.add(EliAdapter(weekEvents[i]))
            eliRecycles[i].setAdapter(eliAdapters[i])
        }


    private fun initRecyclersAndAdapters() {
        eliRecycles.add(findViewById(R.id.calendar_sunday_list))
        eliRecycles.add(findViewById(R.id.calendar_monday_list))
        eliRecycles.add(findViewById(R.id.calendar_tuesday_list))
        eliRecycles.add(findViewById(R.id.calendar_wednesday_list))
        eliRecycles.add(findViewById(R.id.calendar_thursday_list))
        eliRecycles.add(findViewById(R.id.calendar_friday_list))
        eliRecycles.add(findViewById(R.id.calendar_saturday_list))

        for (i in 0 until 7) {
            eliRecycles[i].layoutManager = LinearLayoutManager(this)
            eliAdapters.add(EliAdapter(weekEvents[i]))
            eliRecycles[i].setAdapter(eliAdapters[i])
        }
    }

    private fun getCalendarByUser(email: String, name: String) {
        val retrofit = ServiceBuilder.buildService(CalendarAPI::class.java)
        retrofit.getCalendarByUser(email, name).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //todo
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() == 200) {
                        initCalenderView(response)
                    }
                }
            }
        )

    }

    private fun goToAccountManagement() {
        val intent = Intent(
            this@CalendarActivity,
            ManageAccountActivity::class.java
        )
        intent.putExtra(DbConstants.LOGGED_USER_EMAIL, loggedUserEmail)
        intent.putExtra(DbConstants.LOGGED_USER_NAME, loggedUserName)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_manage_account -> {
                goToAccountManagement()
                true
            }
            R.id.menu_help -> {
                SnackBar.showSnackBar(
                    this@CalendarActivity,
                    "Help"
                )
                true
            }
            R.id.menu_logout -> {
                SnackBar.showSnackBar(
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


    private fun initCalenderView(calendarInfo: Response<ResponseBody>) {
        var jObject = JSONObject(calendarInfo.body()!!.string())
        var drugInfoList = jObject.get("drug_info_list")

        val startDate = eventInterpreter.getFirstDayOfWeek()
        val endDate = eventInterpreter.getLastDayOfWeek()
        weekEvents = eventInterpreter.getEventsForCalendarByDate(
            startDate, endDate,
            drugInfoList as JSONArray
        )

        initRecyclersAndAdapters()
    }
}

