package com.example.piller.utilities

import com.example.piller.models.Dose
import com.example.piller.models.DrugObject
import com.example.piller.models.Occurrence
import com.example.piller.models.Refill
import org.json.JSONArray
import org.json.JSONObject

class ParserUtils {
    companion object {
        fun parsedDrugObject(
            drug: JSONObject,
            intakeDates: JSONObject,
            calendarId: String
        ): DrugObject {
            val drugId = drug.get(DbConstants.drugId).toString()
            val takenId = intakeDates.get(DbConstants.takenId).toString()
            val drugOccur = parseOccurrenceObject(drug.get(DbConstants.occurrences) as JSONObject)
            val drugDose = parseDoseObject(drug.get(DbConstants.dose) as JSONObject)
            val drugRefill = parseRefillObject(drug.get(DbConstants.refill) as JSONObject)

            return DrugObject(
                drugId,
                calendarId,
                drug.get(DbConstants.drugName) as String,
                drug.get(DbConstants.rxcui).toString().toInt(),
                takenId, drugOccur, drugDose, drugRefill
            )
        }

        private fun parseRefillObject(refillObject: JSONObject): Refill {
            val refill = Refill()
            refill.refillId = refillObject.get(DbConstants.refillId) as String
            val refillInfo = refillObject.get(DbConstants.refillInfo) as JSONObject
            refill.reminderTime = refillInfo.get(DbConstants.reminderTime) as String
            refill.isToNotify = refillInfo.get(DbConstants.isToNotify) as Boolean
            refill.pillsLeft = refillInfo.get(DbConstants.pillsLeft) as Int
            refill.pillsBeforeReminder = refillInfo.get(DbConstants.pillsBeforeReminder) as Int
            return refill
        }

        private fun parseDoseObject(doseObject: JSONObject): Dose {
            val dose = Dose()
            dose.doseId = doseObject.get(DbConstants.doseId) as String
            val doseInfo = doseObject.get(DbConstants.doseInfo) as JSONObject
            dose.measurementType = doseInfo.get(DbConstants.measurementType) as String
            dose.totalDose = doseInfo.get(DbConstants.totalDose).toString().toFloat()
            return dose
        }

        private fun parseOccurrenceObject(occurrenceObject: JSONObject): Occurrence {
            val occurrence = Occurrence()
            occurrence.eventId = occurrenceObject.get(DbConstants.eventId) as String
            val drugInfo = occurrenceObject.get(DbConstants.drugInfo) as JSONObject
            occurrence.repeatStart = (drugInfo.get(DbConstants.repeatStart) as String).toLong()
            occurrence.repeatEnd = (drugInfo.get(DbConstants.repeatEnd) as String).toLong()
            occurrence.repeatYear = drugInfo.get(DbConstants.repeatYear) as Int
            occurrence.repeatMonth = drugInfo.get(DbConstants.repeatMonth) as Int
            occurrence.repeatDay = drugInfo.get(DbConstants.repeatDay) as Int
            occurrence.repeatWeek = drugInfo.get(DbConstants.repeatWeek) as Int
            occurrence.repeatWeekday =
                parseRepeatWeekdayArray(drugInfo.get(DbConstants.repeatWeekDay) as JSONArray)

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