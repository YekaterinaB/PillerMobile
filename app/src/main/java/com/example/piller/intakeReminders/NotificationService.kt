package com.example.piller.intakeReminders

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.notif.BackgroundNotificationScheduler


class NotificationService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        //  set notification
        BackgroundNotificationScheduler.createNotificationChannel(this)
        BackgroundNotificationScheduler.scheduleNotificationsForAllProfiles(
            this,
            AppPreferences.email
        )
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        //  todo - remove if not used (doesn't work currently?)
        BackgroundNotificationScheduler.createNotificationChannel(this)
        BackgroundNotificationScheduler.scheduleNotificationsForAllProfiles(
            this,
            AppPreferences.email
        )
        super.onTaskRemoved(rootIntent)
    }
}