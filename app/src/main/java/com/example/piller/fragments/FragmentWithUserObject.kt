package com.example.piller.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.piller.models.UserObject
import com.example.piller.utilities.DbConstants

abstract class FragmentWithUserObject : Fragment() {
    protected lateinit var _loggedUserObject: UserObject

    protected fun putLoggedUserObjectInIntent(intent: Intent) {
        val userBundle = Bundle()
        userBundle.putParcelable(DbConstants.LOGGED_USER_OBJECT, _loggedUserObject)
        intent.putExtra(DbConstants.LOGGED_USER_BUNDLE, userBundle)
    }
}