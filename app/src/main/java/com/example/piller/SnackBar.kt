package com.example.piller

import android.app.Activity
import android.content.Context
import android.widget.Toast
import de.mateware.snacky.Snacky

object SnackBar {
    fun showToastBar(activityContext: Context?, message: String) {
        Toast.makeText(
            activityContext,
            message,
            Toast.LENGTH_SHORT
        ).show()

    }

    fun showSnackBar(activity: Activity, message: String) {
        Snacky.builder()
            .setActivity(activity)
            .setText(message)
            .setDuration(Snacky.LENGTH_SHORT)
            .build()
            .show()
    }
}