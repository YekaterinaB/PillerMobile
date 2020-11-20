package com.example.piller

import android.app.Activity
import de.mateware.snacky.Snacky

object SnackBar {
    fun showSnackBar(activity: Activity, message: String) {
        Snacky.builder()
            .setActivity(activity)
            .setText(message)
            .setDuration(Snacky.LENGTH_SHORT)
            .build()
            .show()
    }
}