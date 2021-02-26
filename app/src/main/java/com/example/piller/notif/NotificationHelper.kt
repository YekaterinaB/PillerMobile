package com.example.piller.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.piller.R
import com.example.piller.activities.DrugInfoActivity
import com.example.piller.models.CalendarEvent
import com.example.piller.utilities.DbConstants

object NotificationHelper {

    fun createNotificationChannel(
        context: Context,
        importance: Int,
        showBadge: Boolean,
        name: String,
        description: String
    ) {
        // 1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 2
            val channelId = "${context.packageName}-$name"
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            // 3
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        context: Context,
        calendarEvent: CalendarEvent,
        currentProfile: String,
        email: String
    ) {

        val notificationBuilder = buildNotification(context, calendarEvent, currentProfile, email)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(
            calendarEvent.id,
            notificationBuilder.build()
        ) //id for each notification

    }

    private fun buildNotification(
        context: Context, calendarEvent: CalendarEvent, currentProfile: String,
        email: String
    ): NotificationCompat.Builder {
        val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_user)
            setContentTitle(currentProfile + ", It's time to take youer medicine!")
            setAutoCancel(true)
            // 2
            val drawable = R.drawable.ic_home
            // 3
            setLargeIcon(BitmapFactory.decodeResource(context.resources, drawable))
            setContentText("It's time to take ${calendarEvent.drug_name}.")
            // 4
//            setGroup(reminderData.type.name)
//            if (reminderData.note != null) {
//                setStyle(NotificationCompat.BigTextStyle().bigText(reminderData.note))
//            }
            val intent = Intent(context, DrugInfoActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(DbConstants.LOGGED_USER_NAME, currentProfile)
                putExtra(DbConstants.LOGGED_USER_EMAIL,email)
                putExtra(DbConstants.CALENDAR_EVENT,calendarEvent)
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            setContentIntent(pendingIntent)
        }
    }
}