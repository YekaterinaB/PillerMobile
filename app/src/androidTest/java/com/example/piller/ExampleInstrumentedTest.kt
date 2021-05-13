package com.example.piller

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.piller.utilities.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.piller", appContext.packageName)
    }

    @Test
    fun dailyEvent() {
        val eventInterpreter = EventInterpreter()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -7)
        val jObject = JSONObject(
            "{\"drug_id\":\"60967c2fff675a052081a762\",\"name\":\"ibuprofen 200 MG Oral Capsule [Advil]\",\"rxcui\":\"731533\",\"occurrence\":{\"event_id\":\"60967c2fff675a052081a75e\",\"drug_info\":{\"repeat_year\":0,\"repeat_month\":0,\"repeat_day\":1,\"repeat_week\":0,\"repeat_weekday\":[0],\"repeat_end\":\"0\",\"repeat_start\":\"${calendar.timeInMillis}\",\"id\":\"60967c2fff675a052081a75e\"}},\"intake_dates\":{\"taken_id\":\"60967c2fff675a052081a75f\",\"intakes\":[{\"_id\":\"60967c33ff675a052081a763\",\"date\":1620508260000,\"isTaken\":false},{\"date\":1620508760000,\"isTaken\":false,\"_id\":\"609bc85a589fb54928ac75d0\"}]},\"dose\":{\"dose_id\":\"60967c2fff675a052081a760\",\"dose_info\":{\"measurement_type\":\"pills\",\"total_dose\":2,\"id\":\"60967c2fff675a052081a760\"}},\"refill\":{\"refill_id\":\"60967c2fff675a052081a761\",\"refill_info\":{\"is_to_notify\":false,\"pills_left\":0,\"pills_before_reminder\":1,\"reminder_time\":\"11:00\",\"id\":\"60967c2fff675a052081a761\"}}},{\"drug_id\":\"609b89e6ddc2473f40b2904b\",\"name\":\"abacavir 600 MG \\\\\\/ dolutegravir 50 MG \\\\\\/ lamivudine 300 MG Oral Tablet\",\"rxcui\":\"1546888\",\"occurrence\":{\"event_id\":\"609b89e2ddc2473f40b29047\",\"drug_info\":{\"repeat_year\":0,\"repeat_month\":0,\"repeat_day\":2,\"repeat_week\":0,\"repeat_weekday\":[0],\"repeat_end\":\"0\",\"repeat_start\":\"1620543600000\",\"id\":\"609b89e2ddc2473f40b29047\"}},\"intake_dates\":{\"taken_id\":\"609b89e3ddc2473f40b29048\",\"intakes\":[]},\"dose\":{\"dose_id\":\"609b89e3ddc2473f40b29049\",\"dose_info\":{\"measurement_type\":\"pills\",\"total_dose\":1,\"id\":\"609b89e3ddc2473f40b29049\"}},\"refill\":{\"refill_id\":\"609b89e3ddc2473f40b2904a\",\"refill_info\":{\"is_to_notify\":false,\"pills_left\":0,\"pills_before_reminder\":1,\"reminder_time\":\"11:00\",\"id\":\"609b89e3ddc2473f40b2904a\"}}}"
        )
        val jArray = JSONArray()
        jArray.put(jObject)

        val events = eventInterpreter.getEventsForCalendarByDate(
            DateUtils.getFirstDayOfWeek(),
            DateUtils.getLastDayOfWeek(),
            jArray,
            "calendarId"
        )

        events.forEach { dailyEvents ->
            assertEquals(1, dailyEvents.size)
        }
    }

    @Test
    fun weeklyEvent() {
        val eventInterpreter = EventInterpreter()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -14)
        val jObject = JSONObject(
            "{\"drug_id\":\"60967c2fff675a052081a762\",\"name\":\"ibuprofen 200 MG Oral Capsule [Advil]\",\"rxcui\":\"731533\",\"occurrence\":{\"event_id\":\"60967c2fff675a052081a75e\",\"drug_info\":{\"repeat_year\":0,\"repeat_month\":0,\"repeat_day\":0,\"repeat_week\":1,\"repeat_weekday\":[1,2],\"repeat_end\":\"0\",\"repeat_start\":\"${calendar.timeInMillis}\",\"id\":\"60967c2fff675a052081a75e\"}},\"intake_dates\":{\"taken_id\":\"60967c2fff675a052081a75f\",\"intakes\":[{\"_id\":\"60967c33ff675a052081a763\",\"date\":1620508260000,\"isTaken\":false},{\"date\":1620508760000,\"isTaken\":false,\"_id\":\"609bc85a589fb54928ac75d0\"}]},\"dose\":{\"dose_id\":\"60967c2fff675a052081a760\",\"dose_info\":{\"measurement_type\":\"pills\",\"total_dose\":2,\"id\":\"60967c2fff675a052081a760\"}},\"refill\":{\"refill_id\":\"60967c2fff675a052081a761\",\"refill_info\":{\"is_to_notify\":false,\"pills_left\":0,\"pills_before_reminder\":1,\"reminder_time\":\"11:00\",\"id\":\"60967c2fff675a052081a761\"}}},{\"drug_id\":\"609b89e6ddc2473f40b2904b\",\"name\":\"abacavir 600 MG \\\\\\/ dolutegravir 50 MG \\\\\\/ lamivudine 300 MG Oral Tablet\",\"rxcui\":\"1546888\",\"occurrence\":{\"event_id\":\"609b89e2ddc2473f40b29047\",\"drug_info\":{\"repeat_year\":0,\"repeat_month\":0,\"repeat_day\":2,\"repeat_week\":0,\"repeat_weekday\":[0],\"repeat_end\":\"0\",\"repeat_start\":\"1620543600000\",\"id\":\"609b89e2ddc2473f40b29047\"}},\"intake_dates\":{\"taken_id\":\"609b89e3ddc2473f40b29048\",\"intakes\":[]},\"dose\":{\"dose_id\":\"609b89e3ddc2473f40b29049\",\"dose_info\":{\"measurement_type\":\"pills\",\"total_dose\":1,\"id\":\"609b89e3ddc2473f40b29049\"}},\"refill\":{\"refill_id\":\"609b89e3ddc2473f40b2904a\",\"refill_info\":{\"is_to_notify\":false,\"pills_left\":0,\"pills_before_reminder\":1,\"reminder_time\":\"11:00\",\"id\":\"609b89e3ddc2473f40b2904a\"}}}"
        )
        val jArray = JSONArray()
        jArray.put(jObject)

        val events = eventInterpreter.getEventsForCalendarByDate(
            DateUtils.getFirstDayOfWeek(),
            DateUtils.getLastDayOfWeek(),
            jArray,
            "calendarId"
        )

        events.slice(0..1).forEach { dailyEvents ->
            assertEquals(1, dailyEvents.size)
        }

        events.slice(2 until events.size).forEach { dailyEvents ->
            assertEquals(0, dailyEvents.size)
        }
    }

    @Test
    fun monthlyEvent() {
        val eventInterpreter = EventInterpreter()
        val jObject = JSONObject(
            "{\"drug_id\":\"60967c2fff675a052081a762\",\"name\":\"ibuprofen 200 MG Oral Capsule [Advil]\",\"rxcui\":\"731533\",\"occurrence\":{\"event_id\":\"60967c2fff675a052081a75e\",\"drug_info\":{\"repeat_year\":0,\"repeat_month\":1,\"repeat_day\":0,\"repeat_week\":0,\"repeat_weekday\":[0],\"repeat_end\":\"0\",\"repeat_start\":\"${Date().time}\",\"id\":\"60967c2fff675a052081a75e\"}},\"intake_dates\":{\"taken_id\":\"60967c2fff675a052081a75f\",\"intakes\":[{\"_id\":\"60967c33ff675a052081a763\",\"date\":1620508260000,\"isTaken\":false},{\"date\":1620508760000,\"isTaken\":false,\"_id\":\"609bc85a589fb54928ac75d0\"}]},\"dose\":{\"dose_id\":\"60967c2fff675a052081a760\",\"dose_info\":{\"measurement_type\":\"pills\",\"total_dose\":2,\"id\":\"60967c2fff675a052081a760\"}},\"refill\":{\"refill_id\":\"60967c2fff675a052081a761\",\"refill_info\":{\"is_to_notify\":false,\"pills_left\":0,\"pills_before_reminder\":1,\"reminder_time\":\"11:00\",\"id\":\"60967c2fff675a052081a761\"}}},{\"drug_id\":\"609b89e6ddc2473f40b2904b\",\"name\":\"abacavir 600 MG \\\\\\/ dolutegravir 50 MG \\\\\\/ lamivudine 300 MG Oral Tablet\",\"rxcui\":\"1546888\",\"occurrence\":{\"event_id\":\"609b89e2ddc2473f40b29047\",\"drug_info\":{\"repeat_year\":0,\"repeat_month\":0,\"repeat_day\":2,\"repeat_week\":0,\"repeat_weekday\":[0],\"repeat_end\":\"0\",\"repeat_start\":\"1620543600000\",\"id\":\"609b89e2ddc2473f40b29047\"}},\"intake_dates\":{\"taken_id\":\"609b89e3ddc2473f40b29048\",\"intakes\":[]},\"dose\":{\"dose_id\":\"609b89e3ddc2473f40b29049\",\"dose_info\":{\"measurement_type\":\"pills\",\"total_dose\":1,\"id\":\"609b89e3ddc2473f40b29049\"}},\"refill\":{\"refill_id\":\"609b89e3ddc2473f40b2904a\",\"refill_info\":{\"is_to_notify\":false,\"pills_left\":0,\"pills_before_reminder\":1,\"reminder_time\":\"11:00\",\"id\":\"609b89e3ddc2473f40b2904a\"}}}"
        )
        val jArray = JSONArray()
        jArray.put(jObject)

        val events = eventInterpreter.getEventsForCalendarByDate(
            DateUtils.getFirstDayOfWeek(),
            DateUtils.getLastDayOfWeek(),
            jArray,
            "calendarId"
        )

        val calendar = Calendar.getInstance()
        events.forEachIndexed { index, dailyEvents ->
            if (index == calendar[Calendar.DAY_OF_WEEK] - 1) {
                assertEquals(1, dailyEvents.size)
            } else {
                assertEquals(0, dailyEvents.size)
            }
        }
    }

    @Test
    fun yearlyEvent() {
        val eventInterpreter = EventInterpreter()
        val jObject = JSONObject(
            "{\"drug_id\":\"60967c2fff675a052081a762\",\"name\":\"ibuprofen 200 MG Oral Capsule [Advil]\",\"rxcui\":\"731533\",\"occurrence\":{\"event_id\":\"60967c2fff675a052081a75e\",\"drug_info\":{\"repeat_year\":1,\"repeat_month\":0,\"repeat_day\":0,\"repeat_week\":0,\"repeat_weekday\":[0],\"repeat_end\":\"0\",\"repeat_start\":\"${Date().time}\",\"id\":\"60967c2fff675a052081a75e\"}},\"intake_dates\":{\"taken_id\":\"60967c2fff675a052081a75f\",\"intakes\":[{\"_id\":\"60967c33ff675a052081a763\",\"date\":1620508260000,\"isTaken\":false},{\"date\":1620508760000,\"isTaken\":false,\"_id\":\"609bc85a589fb54928ac75d0\"}]},\"dose\":{\"dose_id\":\"60967c2fff675a052081a760\",\"dose_info\":{\"measurement_type\":\"pills\",\"total_dose\":2,\"id\":\"60967c2fff675a052081a760\"}},\"refill\":{\"refill_id\":\"60967c2fff675a052081a761\",\"refill_info\":{\"is_to_notify\":false,\"pills_left\":0,\"pills_before_reminder\":1,\"reminder_time\":\"11:00\",\"id\":\"60967c2fff675a052081a761\"}}},{\"drug_id\":\"609b89e6ddc2473f40b2904b\",\"name\":\"abacavir 600 MG \\\\\\/ dolutegravir 50 MG \\\\\\/ lamivudine 300 MG Oral Tablet\",\"rxcui\":\"1546888\",\"occurrence\":{\"event_id\":\"609b89e2ddc2473f40b29047\",\"drug_info\":{\"repeat_year\":0,\"repeat_month\":0,\"repeat_day\":2,\"repeat_week\":0,\"repeat_weekday\":[0],\"repeat_end\":\"0\",\"repeat_start\":\"1620543600000\",\"id\":\"609b89e2ddc2473f40b29047\"}},\"intake_dates\":{\"taken_id\":\"609b89e3ddc2473f40b29048\",\"intakes\":[]},\"dose\":{\"dose_id\":\"609b89e3ddc2473f40b29049\",\"dose_info\":{\"measurement_type\":\"pills\",\"total_dose\":1,\"id\":\"609b89e3ddc2473f40b29049\"}},\"refill\":{\"refill_id\":\"609b89e3ddc2473f40b2904a\",\"refill_info\":{\"is_to_notify\":false,\"pills_left\":0,\"pills_before_reminder\":1,\"reminder_time\":\"11:00\",\"id\":\"609b89e3ddc2473f40b2904a\"}}}"
        )
        val jArray = JSONArray()
        jArray.put(jObject)

        val events = eventInterpreter.getEventsForCalendarByDate(
            DateUtils.getFirstDayOfWeek(),
            DateUtils.getLastDayOfWeek(),
            jArray,
            "calendarId"
        )

        val calendar = Calendar.getInstance()
        events.forEachIndexed { index, dailyEvents ->
            if (index == calendar[Calendar.DAY_OF_WEEK] - 1) {
                assertEquals(1, dailyEvents.size)
            } else {
                assertEquals(0, dailyEvents.size)
            }
        }
    }

    @Test
    fun equalDates() {
        val calendar = Calendar.getInstance()
        assertEquals(true, DateUtils.areDatesEqual(calendar, calendar))
    }

    @Test
    fun notEqualDates() {
        val calendar = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        calendar2.add(Calendar.MINUTE, 5)
        assertEquals(false, DateUtils.areDatesEqual(calendar, calendar2))
    }

    @Test
    fun daysBetweenDates() {
        val daysDiff = 5
        val calendar = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()

        calendar2.add(Calendar.DATE, daysDiff)
        assertEquals(daysDiff, DateUtils.getDaysBetween(calendar.time, calendar2.time))
    }

    @Test
    fun dateAfter() {
        val calendar = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()

        calendar2.add(Calendar.DATE, 1)
        assertEquals(true, DateUtils.isDateAfter(calendar2.time, calendar.time))
    }

    @Test
    fun dateNotAfter() {
        val calendar = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()

        calendar2.add(Calendar.DATE, 1)
        assertEquals(false, DateUtils.isDateAfter(calendar.time, calendar2.time))
    }
}