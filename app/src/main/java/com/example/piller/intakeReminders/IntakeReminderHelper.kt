package com.example.piller.intakeReminders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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

object IntakeReminderHelper {


    fun createNotification(
        context: Context, drug: DrugObject,
        currentProfile: String,
        email: String
    ) {
        val id = drug.occurrence.eventId.hashCode()
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
            setContentTitle("$currentProfile, It's time to take your medicine!")
            setAutoCancel(true)
            // 2
            val drawable = R.drawable.pill
            // 3
            setLargeIcon(BitmapFactory.decodeResource(context.resources, drawable))
            setContentText("It's time to take ${drug.drugName}.")
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