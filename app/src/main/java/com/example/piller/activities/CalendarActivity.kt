package com.example.piller.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.piller.EventInterpreter
import com.example.piller.R
import com.example.piller.api.CalendarAPI
import com.example.piller.api.ServiceBuilder
import com.example.piller.models.CalendarEvent
import com.example.piller.utilities.DbConstants
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class CalendarActivity : AppCompatActivity() {
    private lateinit var loggedUserEmail: String
    private lateinit var loggedUserName: String
    lateinit var toolbar: Toolbar
    private val eventInterpreter = EventInterpreter()
    private lateinit var weekEvents: Array<List<CalendarEvent>>


    override fun onCreate(savedInstanceState: Bundle?) {
        //  todo: disable going back to login
        super.onCreate(savedInstanceState)
        loggedUserEmail = intent.getStringExtra(DbConstants.LOGGED_USER_EMAIL)!!
        loggedUserName = intent.getStringExtra(DbConstants.LOGGED_USER_NAME)!!
        getCalendarByUser(loggedUserEmail, loggedUserName)
        setContentView(R.layout.activity_calendar)

        toolbar = findViewById(R.id.calendar_toolbar)

        toolbar.title = loggedUserName
        setSupportActionBar(toolbar)
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
                        updateCalenderView(response)
                    }
                }
            }
        )

    }


    private fun updateCalenderView(calendarInfo: Response<ResponseBody>) {
        var jObject = JSONObject(calendarInfo.body()!!.string())
        var drugInfoList = jObject.get("drug_info_list")

        val startDate = eventInterpreter.getLastDayOfWeek()
        val endDate = eventInterpreter.getLastDayOfWeek()
        weekEvents = eventInterpreter.getEventsForCalendarByDate(startDate, endDate,
            drugInfoList as JSONArray
        )

    }
}

