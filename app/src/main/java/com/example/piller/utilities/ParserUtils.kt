package com.example.piller.utilities

import com.example.piller.models.Dose
import com.example.piller.models.DrugObject
import com.example.piller.models.Occurrence
import org.json.JSONArray
import org.json.JSONObject

class ParserUtils {
    companion object {
        fun parsedDrugObject(
            drug: JSONObject,
            intakeDates: JSONObject,
            calendarId: String
        ): DrugObject {
            val drugId = drug.get("drug_id").toString()
            val takenId = intakeDates.get("taken_id").toString()
            val occurrenceObject = drug.get("occurrence") as JSONObject
            val doseObject = drug.get("dose") as JSONObject
            val drugOccur = parseOccurrenceObject(occurrenceObject)
            val drugDose = parseDoseObject(doseObject)
            return DrugObject(
                drugId,
                calendarId,
                drug.get("name") as String,
                drug.get("rxcui").toString().toInt(),
                takenId, drugOccur, drugDose
            )
        }

        private fun parseDoseObject(doseObject: JSONObject): Dose {
            val dose = Dose()
            dose.doseId = doseObject.get("dose_id") as String
            val doseInfo = doseObject.get("dose_info") as JSONObject
            dose.measurementType = doseInfo.get("measurement_type") as String
            dose.totalDose = doseInfo.get("total_dose").toString().toFloat()
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