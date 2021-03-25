package com.example.piller.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.piller.R
import com.example.piller.activities.DrugInfoActivity
import com.example.piller.intakeReminders.IntakeReminderScheduler
import com.example.piller.models.CalendarEvent
import com.example.piller.models.DrugObject
import com.example.piller.refillReminders.RefillReminderHelper
import com.example.piller.utilities.DbConstants
import java.util.*
import java.util.Calendar.*

object NotificationHelper {


    fun createNotificationChannel(
        context: Context,
        showBadge: Boolean,
        name: String,
        importance:Int
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
        cal.set(Calendar.HOUR_OF_DAY, intakeCal[Calendar.HOUR_OF_DAY])
        cal.set(Calendar.MINUTE, intakeCal[Calendar.MINUTE])
        cal.set(Calendar.SECOND, intakeCal[Calendar.SECOND])
        cal.set(Calendar.MILLISECOND, intakeCal[Calendar.MILLISECOND])
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