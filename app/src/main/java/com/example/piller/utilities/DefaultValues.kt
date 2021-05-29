package com.example.piller.utilities

object DbConstants {
    const val LOGGED_USER_BUNDLE = "LOGGED_USER_BUNDLE"
    const val LOGGED_USER_OBJECT = "LOGGED_USER_OBJECT"
    const val LOGGED_USER_EMAIL = "LOGGED_USER_EMAIL"
    const val LOGGED_USER_NAME = "LOGGED_USER_NAME"

    const val CALENDAR_ID = "calendar_id"

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

    //  app preferences
    const val loginPreferences = "LoginPreferences"
    const val isLogin = "is_login"
    const val email = "email"
    const val password = "password"
    const val defaultStringValue = ""
    const val showNotifications = "showNotifications"

    //  add new drug
    const val defaultRxcui = 0

    //  drug info
    const val windowHeight = 1500

    //  drug occurrence
    const val repeatOnDefaultValue = 0
    const val defaultIntakeTime = -1L
    const val minRefill = 1
    const val maxRefill = 101
    const val noDaysChosenError = "You must choose at least one day to repeat on!"

    //  login
    const val backStackEntryCountMin = 1
    const val activityStackNoFlags = 0

    //  general
    const val previousActivityPositionInStack = 2

    //  number picker
    const val defaultNumberPickerValue = 0
    const val minNumberPickerValue = 0
    const val stringArgsName = "values"
    const val valueAttribute = "value"
    const val minAttribute = "min"
    const val maxAttribute = "max"
    const val selectorDeclaredField = "mSelectorWheelPaint"

    //  add drug
    const val REQUEST_CAPTURE_IMAGE = 100
    const val PERMISSION_REQUEST_CODE = 102
    const val HIDE_KEYBOARD_FLAGS = 0
    const val drugFrequencyDialogFragmentTag = "DrugFrequencyDialogFragment"
    const val drugPickerDialogFragmentTag = "DrugPickerDialogFragment"
    const val drugFrequencyRepeatDialogFragmentTag = "DrugFrequencyRepeatDialogFragment"
    const val drugFrequencyWeeklyDialogFragmentTag = "DrugFrequencyWeeklyDialogFragment"
    const val drugStartRepeatDialogFragmentTag = "DrugStartRepeatDialogFragment"
    const val invalidFrequencyValue = -1
    const val defaultFrequencyValue = 0
    const val weeksRepeat = "weeks"
    const val dayEnumValue = 0
    const val weekEnumValue = 1
    const val monthEnumValue = 2
    const val yearEnumValue = 3
    const val noRepeatEnumValue = 0
    const val maxStartRepeats = 5
    const val interactionWindowWidthFactor = 0.85
    const val drugInteractionsIntentTag = "drug_interactions"
    const val calendarChosenViewPosition = 0
    const val DRUG_INFO_DELETE_CODE = 1
    const val eventDayBitMapWidth = 256
    const val eventDayBitMapWidthFactor = 2
    const val eventDayBitMapHeight = 128
    const val eventDayBitMapHeightFactor = 4

    //  login
    const val forgotPasswordWidth = 700
    const val forgotPasswordHeight = 200

    //  profile
    const val popupX = 0
    const val popupY = 0

    //  APIs
    const val calendarURL = "calendar/"
    const val drugApiCallsURL = "drugApiCalls/"
    const val drugIntakesURL = "drugIntakes/"
    const val profileURL = "profile/"
    const val supervisorsURL = "supervisors/"
    const val userURL = "user/"
    const val connectionTimeout = 100L

    //  supervisors
    const val noMissedDaysCountStr = "0"
    const val noMissedDaysCount = 0
    const val noMissedDaysText = "No"
    const val popupElevation = 5.0f

    //  bottom dialog
    const val drugBottomDialogFragmentTag = "DrugBottomDialogFragment"

    //  notifications
    const val pendingIntentRequestCode = 0
    const val noDayOfWeekStr = "0"
    const val numberOfDaysAWeekInCalendar = 7.0

    //  adapters
    const val timeFormat = "HH:mm"

    //  dose
    const val initialTotalDose = 0F

    //  notif
    const val backgroundServiceTriggerAt = 10L

    //  parser utils
    const val drugId = "drug_id"
    const val takenId = "taken_id"
    const val occurrences = "occurrence"
    const val dose = "dose"
    const val refill = "refill"
    const val drugName = "name"
    const val rxcui = "rxcui"
    const val refillId = "refill_id"
    const val refillInfo = "refill_info"
    const val reminderTime = "reminder_time"
    const val isToNotify = "is_to_notify"
    const val pillsLeft = "pills_left"
    const val pillsBeforeReminder = "pills_before_reminder"
    const val doseId = "dose_id"
    const val doseInfo = "dose_info"
    const val measurementType = "measurement_type"
    const val totalDose = "total_dose"
    const val eventId = "event_id"
    const val drugInfo = "drug_info"
    const val repeatStart = "repeat_start"
    const val repeatEnd = "repeat_end"
    const val repeatYear = "repeat_year"
    const val repeatMonth = "repeat_month"
    const val repeatWeek = "repeat_week"
    const val repeatWeekDay = "repeat_weekday"
    const val repeatDay = "repeat_day"

    //  refill
    const val defaultRefillTime = "00:00"
    const val defaultPillsBeforeReminder = 1
    const val defaultPillsLeft = 0

    //  occurrence
    const val defaultRepeatYear = 0
    const val defaultRepeatMonth = 0
    const val defaultRepeatWeek = 0
    const val defaultRepeatDay = 0
    val defaultRepeatWeekDay = listOf(0)
    const val defaultRepeatStart = 0L
    const val defaultRepeatEnd = 0L
    const val defaultRefillReminder = 20
    const val defaultRefillReminderTime = "11:00"
    const val defaultTotalDose = 1.0F
    const val dailyString = "Daily"
    const val weeklyString = "Weekly"
    const val monthlyString = "Monthly"
    const val yearlyString = "Yearly"
    const val repeatOnceString = "Repeat once"
    const val couldNotAddDrugError = "Could not add drug."

    //  drug search
    const val multipartHeader = "multipart/form-data"
    const val multipartFileName = "file"
    const val convertingToBase64Error = "Could not convert image to base64."
    const val invalidDrugNameError = "Please enter a valid drug name"
    const val unableToSearchInteractionsError = "Could not search interactions."
    const val noDrugsFoundMessage = "No drugs found!"
    const val interaction = "interaction"
    const val description = "description"

    //  drug info
    const val couldNotConnectServerError = "Could not connect to server."
    const val couldNotDeleteDrugError = "Could not delete drug."
    const val OKCode = 200

    //  login
    const val existingUserEmailError = "A user with this email already exists."
    const val successfulRegistrationMessage = "User was successfully created."
    const val userDoesNotExistError = "User does not exist, check your login information."
    const val problemWithUserError = "There is a problem with user."
    const val couldNotResetPasswordError = "Could not reset password."
    const val resetEmailSent = "Reset email sent!"

    //  manage account
    const val couldNotUpdateUserError = "Could not update user."
    const val couldNotDeleteUserError = "Could not delete user."

    const val updateUserSuccessfulMessage = "User was updated successfully."

    //  profile
    const val profileList = "profile_list"
    const val profileName = "name"
    const val profileId = "id"
    const val profileRelation = "relation"
    const val couldNotDeleteProfileError = "Could not delete profile."
    const val couldNotInitProfileListError = "Could not init profile list."
    const val profileExistsError = "Profile name already exists"

    //  supervisors
    const val supervisorsList = "supervisorsList"
    const val supervisorName = "supervisorName"
    const val supervisorEmail = "supervisorEmail"
    const val isConfirmed = "isConfirmed"
    const val cantGetThresholdError = "Could not get threshold for supervisors."
    const val cantUpdateThresholdError = "Could not update threshold for supervisors."
    const val threshold = "threshold"
    const val noThreshold = "None"

    //  weekly
    const val cantGetWeeklyDataError = "Could not get weekly calendar view."

    //  event interpreter
    const val intakeDates = "intake_dates"
    const val intakes = "intakes"
    const val hourOfDay = "hourOfDay"
    const val minuteOfDay = "minuteOfDay"
    const val calendarRepeatEnd = "calendarRepeatEnd"
    const val calendarCurrent = "calendarCurrent"
    const val calendarStartRepeat = "calendarStartRepeat"
    const val calendarEnd = "calendarEnd"
    const val intakeDate = "date"
    const val isTaken = "isTaken"

    //  api
    const val userId = "userId"
    const val profileIdStr = "profileId"
    const val contentHeaders = "Content-Type: application/json"
    const val addDrug = "addDrug"
    const val updateDrug = "updateDrug"
    const val deleteDrug = "deleteDrug"
    const val deleteFutureOccurrencesOfDrugByUser = "deleteFutureOccurrencesOfDrugByUser"
    const val DELETEMethod = "DELETE"
    const val drugNameStr = "drugName"
    const val drugByName = "drugByName"
    const val findInteractions = "findInteractions"
    const val newRxcui = "newRxcui"
    const val getDrugImage = "getDrugImage"
    const val findDrugByImage = "findDrugByImage"
    const val findDrugByBoxImage = "findDrugByBoxImage"
    const val setIntakeTaken = "setIntakeTaken"
    const val setIntakeNotTaken = "setIntakeNotTaken"
    const val getAllIntakes = "getAllIntakes"
    const val register = "register"
    const val authenticate = "authenticate"
    const val resetPassword = "resetPassword"
}