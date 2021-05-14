package com.example.piller.intakeReminders

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.piller.models.UserObject
import com.example.piller.notif.BackgroundNotificationScheduler
import com.example.piller.utilities.DbConstants


class NotificationService : Service() {
    private lateinit var loggedUserObject: UserObject

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val bundle = intent!!.extras!!.getBundle(DbConstants.LOGGED_USER_BUNDLE)
        loggedUserObject = bundle?.getParcelable(DbConstants.LOGGED_USER_OBJECT)!!
        //  set notification
        BackgroundNotificationScheduler.createNotificationChannel(this)
        BackgroundNotificationScheduler.scheduleNotificationsForAllProfiles(
            this,
            loggedUserObject
        )
        return START_STICKY
    }
}