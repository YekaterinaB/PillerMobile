package com.example.piller.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.piller.models.UserObject
import com.example.piller.utilities.DbConstants

abstract class ActivityWithUserObject : AppCompatActivity() {
    protected lateinit var loggedUserObject: UserObject

    protected fun initUserObject(intent: Intent) {
        val bundle = intent.extras!!.getBundle(DbConstants.LOGGED_USER_BUNDLE)
        loggedUserObject = bundle?.getParcelable(DbConstants.LOGGED_USER_OBJECT)!!
    }

    protected fun putLoggedUserObjectInIntent(intent: Intent) {
        val userBundle = Bundle()
        userBundle.putParcelable(DbConstants.LOGGED_USER_OBJECT, loggedUserObject)
        intent.putExtra(DbConstants.LOGGED_USER_BUNDLE, userBundle)
    }
}