package com.example.piller.refillReminders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.piller.R
import com.example.piller.activities.DrugInfoActivity
import com.example.piller.models.DrugObject
import com.example.piller.models.UserObject
import com.example.piller.notif.NotificationHelper
import com.example.piller.utilities.DbConstants

object RefillReminderHelper {

    fun createNotification(
        context: Context, drug: DrugObject, loggedUserObject: UserObject
    ) {
        val id = drug.refill.refillId.hashCode()
        val notificationBuilder =
            buildNotification(context, drug, loggedUserObject.currentProfile!!.name, loggedUserObject)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(
            id,
            notificationBuilder.build()
        ) //id for each notification

    }

    private fun buildNotification(
        context: Context, drug: DrugObject, currentProfile: String, loggedUserObject: UserObject
    ): NotificationCompat.Builder {
        val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.pill_dark_blue)
            setContentTitle("$currentProfile, It's time to refill your medication!")
            setAutoCancel(true)
            // 2
            val drawable = R.drawable.pillbox
            // 3
            setLargeIcon(BitmapFactory.decodeResource(context.resources, drawable))
            setContentText("It's time to refill ${drug.drugName}.")
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
                val userBundle = Bundle()
                userBundle.putParcelable(DbConstants.LOGGED_USER_OBJECT, loggedUserObject)
                putExtra(DbConstants.LOGGED_USER_BUNDLE, userBundle)
            }
            val pendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            setContentIntent(pendingIntent)
        }
    }

}