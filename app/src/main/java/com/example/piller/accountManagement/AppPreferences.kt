package com.example.piller.accountManagement

import android.content.Context
import android.content.SharedPreferences
import com.example.piller.utilities.DbConstants

object AppPreferences {
    private const val NAME = DbConstants.loginPreferences
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    //  SharedPreferences variables
    private val IS_LOGIN = Pair(DbConstants.isLogin, false)
    private val EMAIL = Pair(DbConstants.email, DbConstants.defaultStringValue)
    private val PASSWORD = Pair(DbConstants.password, DbConstants.defaultStringValue)
    private val SHOW_NOTIFICATIONS = Pair(DbConstants.showNotifications, true)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    //an inline function to put variable and save it
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    //SharedPreferences variables getters/setters
    var stayLoggedIn: Boolean
        get() = preferences.getBoolean(IS_LOGIN.first, IS_LOGIN.second)
        set(value) = preferences.edit {
            it.putBoolean(IS_LOGIN.first, value)
        }

    var email: String
        get() = preferences.getString(EMAIL.first, EMAIL.second) ?: DbConstants.defaultStringValue
        set(value) = preferences.edit {
            it.putString(EMAIL.first, value)
        }

    var password: String
        get() = preferences.getString(PASSWORD.first, PASSWORD.second)
            ?: DbConstants.defaultStringValue
        set(value) = preferences.edit {
            it.putString(PASSWORD.first, value)
        }

    var showNotifications: Boolean
        get() = preferences.getBoolean(SHOW_NOTIFICATIONS.first, SHOW_NOTIFICATIONS.second)
        set(value) = preferences.edit {
            it.putBoolean(SHOW_NOTIFICATIONS.first, value)
        }
}