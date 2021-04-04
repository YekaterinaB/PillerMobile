package com.example.piller.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.piller.models.CalendarEvent
import com.example.piller.models.DrugObject
import com.example.piller.utilities.DateUtils
import java.util.*

object NotificationHelper {


    fun createNotificationChannel(
        context: Context,
        showBadge: Boolean,
        name: String,
        importance: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 2
            val channelId = "${context.packageName}-$name"
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = "Piller Notifications"
            channel.setShowBadge(showBadge)

            // 3
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

    }


    fun createCalenderEventWithDrug(drug: DrugObject): CalendarEvent {
        val cal = Calendar.getInstance()
        val intakeCal = Calendar.getInstance()
        intakeCal.timeInMillis = drug.occurrence.repeatStart
        DateUtils.setCalendarTime(
            cal,
            intakeCal.get(Calendar.HOUR_OF_DAY),
            intakeCal.get(Calendar.MINUTE)
        )
        return CalendarEvent(
            drug.calendarId,
            drug.drugId,
            cal[Calendar.DAY_OF_WEEK],
            cal.time,
            cal.time,// end_repeat for calender event
            isTaken = false
        )
    }

}