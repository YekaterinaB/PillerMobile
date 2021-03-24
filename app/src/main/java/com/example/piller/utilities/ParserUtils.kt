package com.example.piller.utilities

import com.example.piller.models.Dose
import com.example.piller.models.DrugObject
import com.example.piller.models.Occurrence
import com.example.piller.models.Refill
import org.json.JSONArray
import org.json.JSONObject

class parserUtils {
    companion object {
        fun parsedDrugObject(
            drug: JSONObject,
            intakeDates: JSONObject,
            calendarId: String
        ): DrugObject {
            val takenId = intakeDates.get("taken_id").toString()
            val drugOccur = parseOccurrenceObject(drug.get("occurrence") as JSONObject)
            val drugDose = parseDoseObject(drug.get("dose") as JSONObject)
            val drugRefill = parseRefillObject(drug.get("refill") as JSONObject)

            return DrugObject(
                calendarId,
                drug.get("name") as String,
                drug.get("rxcui").toString().toInt(),
                takenId, drugOccur, drugDose,drugRefill
            )
        }

        private fun parseRefillObject(refillObject: JSONObject): Refill {
            val refill = Refill()
            refill.refillId = refillObject.get("refill_id") as String
            val refillInfo = refillObject.get("refill_info") as JSONObject
            refill.reminderTime = refillInfo.get("reminder_time") as String
            refill.isToNotify = refillInfo.get("is_to_notify") as Boolean
            refill.pillsLeft = refillInfo.get("pills_left") as Int
            refill.pillsBeforeReminder = refillInfo.get("pills_before_reminder") as Int
            return refill
        }

        private fun parseDoseObject(doseObject: JSONObject): Dose {
            val dose = Dose()
            dose.doseId = doseObject.get("dose_id") as String
            val doseInfo = doseObject.get("dose_info") as JSONObject
            dose.measurementType = doseInfo.get("measurement_type") as String
            dose.totalDose = doseInfo.get("total_dose") as Int
            return dose
        }

        private fun parseOccurrenceObject(occurrenceObject: JSONObject): Occurrence {
            val occurrence = Occurrence()
            occurrence.eventId = occurrenceObject.get("event_id") as String
            val drugInfo = occurrenceObject.get("drug_info") as JSONObject
            occurrence.repeatStart = (drugInfo.get("repeat_start") as String).toLong()
            occurrence.repeatEnd = (drugInfo.get("repeat_end") as String).toLong()
            occurrence.repeatYear = drugInfo.get("repeat_year") as Int
            occurrence.repeatMonth = drugInfo.get("repeat_month") as Int
            occurrence.repeatDay = drugInfo.get("repeat_day") as Int
            occurrence.repeatWeek = drugInfo.get("repeat_week") as Int
            occurrence.repeatWeekday =
                parseRepeatWeekdayArray(drugInfo.get("repeat_weekday") as JSONArray)

            return occurrence
        }

        private fun parseRepeatWeekdayArray(weekdayJson: JSONArray): List<Int> {
            val intList = mutableListOf<Int>()
            for (i in 0 until weekdayJson.length()) {
                intList.add(weekdayJson.optInt(i))
            }
            return intList
        }
    }
}