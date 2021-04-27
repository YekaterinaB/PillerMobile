package com.example.piller.utilities

object DbConstants {
    const val LOGGED_USER_BUNDLE = "LOGGED_USER_BUNDLE"
    const val LOGGED_USER_OBJECT = "LOGGED_USER_OBJECT"
    const val LOGGED_USER_EMAIL = "LOGGED_USER_EMAIL"
    const val LOGGED_USER_NAME = "LOGGED_USER_NAME"

    const val CALENDAR_ID = "calendar_id"
    const val EVENT_ID = "event_id"

    //  fullview fragment
    const val EVENTDAY_DRAWABLE_CIRCLE_RADIUS = 30F
    const val DRUGSLIST = "drug_list"
    const val FUTURE_DRUGSLIST = "future_drug_list"
    const val DRUG_DELETE_POPUP = 1
    const val DRUG_SHOULD_REFRESH_DATA = 2
    const val DRUG_DELETES = "drug_deletes"

    //  drugs - general
    const val DRUG_INFO_LIST = "drug_info_list"
    const val FULL_DRUG_NAME = "full_drug_name"
    const val INTAKE_DATE = "intake_time"
    const val DRUG_RXCUI = "drug_rxcui"
    const val DRUG_OBJECT = "drug_object"
    const val RXCUI_CURRENT_LIST = "rxcui_current_list"
    const val INITIALIZED_DRUG = "has_been_initialized"

    //  calendar
    const val CALENDAR_FRAGMENT_ID = "calendar_fragment"
    const val WEEKLY_CALENDAR_FRAGMENT_ID = "weekly_calendar"
    const val FULL_VIEW_FRAGMENT_ID = "full_view_calendar"
    const val PROFILES_FRAGMENT_ID = "profiles"
    const val DRUGS_FRAGMENT_ID = "drugs"
    const val ADD_DRUG_FRAGMENT_ID = "add_drug_options"

    // calendar events
    const val CALENDAR_EVENT = "Calendar_event"
    const val CALENDAR_EVENT_BUNDLE = "Calendar_event_bundle"

    //  supervisors
    const val DEFAULT_SUPERVISOR_THRESHOLD = 3
    const val SUPERVISOR_FRAGMENT_ID = "supervisor_fragment_id"
    const val SETTINGS_FRAGMENT_ID = "settings_fragment_id"

    //  adding drugs
    const val DRUG_BY_NAME = "add_drug_by_name"
    const val DRUG_BY_PILL = "add_drug_by_pill"
    const val DRUG_BY_BOX = "add_drug_by_box"
    const val DRUG_OCCURRENCE = "add_drug_occurrence"
    const val DRUG_RESULT_NAME = "add_drug_result_name"
    const val ADD_DRUG_TYPE = "add_drug_type"
    const val NEW_DRUG_ADDED = "new_drug_added"

    //  drug information
    const val REMOVE_DRUG_FUTURE = 999
    const val TAKEN_STATUS_UPDATE = 888
    const val TAKEN_NEW_VALUE = "taken_status_update"
    const val SHOULD_REFRESH_DATA = "should_update_data"
    const val EDIT_ONLY_FUTURE_OCCURRENCES = "edit_only_future_occurrences"
    const val FROM_NOTIFICATION = "from_notification"

    // server
    const val SERVER_URL = "http://10.0.2.2:3000"

    //  Login
    const val LOGIN_FRAGMENT_ID = "login_fragment"
    const val SIGN_IN_FRAGMENT_ID = "sign_in_fragment"
    const val REGISTER_FRAGMENT_ID = "register_fragment"
    const val SPLASH_FRAGMENT_ID = "splash_fragment"
}