package com.example.piller.refillReminders

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
import com.example.piller.models.CalendarEvent
import com.example.piller.models.DrugObject
import com.example.piller.notif.NotificationHelper
import com.example.piller.utilities.DbConstants
import java.util.*

object RefillReminderHelper {

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
        context: Context, drug: DrugObject,
        currentProfile: String,
        email: String
    ) {
        val id = drug.refill.refillId.hashCode()
        val notificationBuilder = buildNotification(context, drug, currentProfile, email)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(
            id,
            notificationBuilder.build()
        ) //id for each notification

    }

    private fun buildNotification(
        context: Context, drug: DrugObject, currentProfile: String,
        email: String
    ): NotificationCompat.Builder {
        val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.pill)
            setContentTitle("$currentProfile, It's time to refill your medication!")
            setAutoCancel(true)
            // 2
            val drawable = R.drawable.pillbox
            // 3
            setLargeIcon(BitmapFactory.decodeResource(context.resources, drawable))
            setContentText("It's refill to take ${drug.drugName}.")
            // 4
//            setGroup(reminderData.type.name)
//            if (reminderData.note != null) {
//                setStyle(NotificationCompat.BigTextStyle().bigText(reminderData.note))
//            }

            val calEvent = NotificationHelper.createCalenderEventWithDrug(drug)
            val bundleDrugObject = Bundle()
            bundleDrugObject.putParcelable(DbConstants.CALENDAR_EVENT, calEvent)

            val intent = Intent(context, DrugInfoActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                action = context.getString(R.string.action_notify_medication)

                putExtra(DbConstants.CALENDAR_EVENT_BUNDLE, bundleDrugObject)
                putExtra(DbConstants.LOGGED_USER_NAME, currentProfile)
                putExtra(DbConstants.LOGGED_USER_EMAIL, email)
            }
            val pendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            setContentIntent(pendingIntent)
        }
    }

}